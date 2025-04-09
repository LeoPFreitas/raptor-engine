package co.raptor.engine.raft.internal;

import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientRequest;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.server.raftlog.RaftLog;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.SnapshotInfo;
import org.apache.ratis.statemachine.StateMachine;
import org.apache.ratis.statemachine.StateMachineStorage;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.SimpleStateMachineStorage;
import org.apache.ratis.util.JavaUtils;
import org.apache.ratis.util.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RaptorEngineBalanceAccountStateMachineV2 implements StateMachine {
    private static final Logger logger = LoggerFactory.getLogger(RaptorEngineBalanceAccountStateMachineV2.class);


    private final SimpleStateMachineStorage storage = new SimpleStateMachineStorage();
    private final LifeCycle lifeCycle = new LifeCycle(JavaUtils.getClassSimpleName(getClass()));

    // Queue for transaction processing
    private final ArrayBlockingQueue<TransactionContext> transactionQueue = new ArrayBlockingQueue<>(10000);

    // Holds the last applied TermIndex
    private final AtomicReference<TermIndex> lastAppliedTermIndex = new AtomicReference<>();

    // Thread to process transactions
    private Thread transactionProcessorThread;

    private RaftServer raftServer;
    private volatile RaftGroupId raftGroupId;

    @Override
    public void initialize(RaftServer raftServer, RaftGroupId raftGroupId, RaftStorage raftStorage) throws IOException {
        // Transition state
        lifeCycle.transition(LifeCycle.State.STARTING);

        // Metadata setup
        this.raftServer = raftServer;
        this.raftGroupId = raftGroupId;

        // Initialize state machine storage
        storage.init(raftStorage);

        // Restore the state from the latest snapshot
        SnapshotInfo latestSnapshot = storage.getLatestSnapshot();
        if (latestSnapshot != null) {
            TermIndex termIndex = latestSnapshot.getTermIndex();
            if (termIndex != null) {
                lastAppliedTermIndex.set(termIndex); // Set the last applied snapshot TermIndex
                logger.info("Successfully restored state from snapshot with TermIndex: {}", termIndex);
            } else {
                logger.warn("Snapshot found, but TermIndex is null. Initial state may be incomplete.");
            }
        } else {
            logger.warn("No snapshot found during initialization. State machine starts from scratch.");
        }

        // Start the transaction processor thread (for queued transactions)
        startTransactionProcessor();

        // Transition to RUNNING
        lifeCycle.transition(LifeCycle.State.RUNNING);
    }

    @Override
    public LifeCycle.State getLifeCycleState() {
        return lifeCycle.getCurrentState();
    }

    @Override
    public void pause() {
        if (lifeCycle.getCurrentState() == LifeCycle.State.RUNNING) {
            lifeCycle.transition(LifeCycle.State.PAUSED);
        }
    }

    @Override
    public void reinitialize() throws IOException {
        throw new UnsupportedOperationException("Reinitialize is not implemented yet.");
    }

    @Override
    public long takeSnapshot() throws IOException {
        return RaftLog.INVALID_LOG_INDEX;
    }

    @Override
    public StateMachineStorage getStateMachineStorage() {
        return this.storage;
    }

    @Override
    public SnapshotInfo getLatestSnapshot() {
        return getStateMachineStorage().getLatestSnapshot();
    }

    @Override
    public CompletableFuture<Message> query(Message request) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Message> queryStale(Message request, long minIndex) {
        return null;
    }

    @Override
    public TransactionContext startTransaction(RaftClientRequest request) throws IOException {
        if (request == null || request.getMessage() == null) {
            logger.error("Invalid RaftClientRequest or message: {}", request);
            throw new IllegalArgumentException("RaftClientRequest or its message cannot be null!");
        }
        logger.info("Starting transaction for request: {}", request);

        return TransactionContext.newBuilder()
                .setStateMachine(this)
                .setClientRequest(request)
                .build();
    }

    @Override
    public TransactionContext preAppendTransaction(TransactionContext trx) throws IOException {
        return trx;
    }

    @Override
    public TransactionContext cancelTransaction(TransactionContext trx) throws IOException {
        return trx;
    }

    @Override
    public TransactionContext applyTransactionSerial(TransactionContext trx) {
        if (trx == null) {
            logger.error("TransactionContext is null during log replay!");
            throw new IllegalStateException("TransactionContext is null during log replay!");
        }
        if (trx.getClientRequest() == null) {
            logger.error("ClientRequest is null in TransactionContext during log replay: {}", trx);
            throw new IllegalStateException("ClientRequest is null in TransactionContext during log replay!");
        }
        if (trx.getClientRequest().getMessage() == null) {
            logger.error("Message is null in ClientRequest during log replay: {}", trx);
            throw new IllegalStateException("Message is null in ClientRequest during log replay!");
        }

        logger.info("Successfully applied transaction serial during log replay: term={}, index={}",
                trx.getLogEntry().getTerm(), trx.getLogEntry().getIndex());

        return trx;
    }

    /**
     * Handles applying transactions asynchronously.
     */
    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        try {
            if (trx == null || trx.getClientRequest() == null || trx.getClientRequest().getMessage() == null) {
                logger.error("Invalid TransactionContext or Message during applyTransaction: {}", trx);
                throw new IllegalStateException("TransactionContext or its clientRequest/message is null!");
            }

            // Offer transaction for processing
            boolean accepted = transactionQueue.offer(trx);
            if (!accepted) {
                logger.error("Transaction queue is full. Cannot process transaction: {}", trx);
                throw new IllegalStateException("Transaction queue is full. Rejecting transaction.");
            }

            return CompletableFuture.completedFuture(trx.getClientRequest().getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while applying transaction: {}", trx, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Returns the last applied TermIndex.
     */
    @Override
    public TermIndex getLastAppliedTermIndex() {
        return lastAppliedTermIndex.get();
    }

    @Override
    public void close() throws IOException {
        lifeCycle.transitionIfValid(LifeCycle.State.CLOSED);
        if (transactionProcessorThread != null && transactionProcessorThread.isAlive()) {
            transactionProcessorThread.interrupt();
        }
    }

    /**
     * Starts the transaction processor thread that processes transactions in strict order.
     */
    private void startTransactionProcessor() {
        transactionProcessorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Poll a transaction
                    TransactionContext trx = transactionQueue.take();

                    // Process the transaction
                    processTransaction(trx);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Handle unexpected errors during transaction processing
                    e.printStackTrace();
                }
            }
        });
        transactionProcessorThread.start();
    }

    /**
     * Processes a transaction and updates the last applied TermIndex.
     */
    private void processTransaction(TransactionContext trx) {
        if (trx == null || trx.getClientRequest() == null || trx.getClientRequest().getMessage() == null) {
            throw new IllegalStateException("TransactionContext or its clientRequest/message is null!");
        }

        long term = trx.getLogEntry().getTerm();
        long index = trx.getLogEntry().getIndex();
        TermIndex newTermIndex = TermIndex.valueOf(term, index);

        TermIndex currentTermIndex = lastAppliedTermIndex.get();
        if (currentTermIndex != null && newTermIndex.getIndex() <= currentTermIndex.getIndex()) {
            throw new IllegalStateException("Transaction index " + newTermIndex.getIndex() +
                    " is less than or equal to current applied index " + currentTermIndex.getIndex());
        }

        logger.info("Processing transaction: term={}, index={}", term, index);
        lastAppliedTermIndex.set(newTermIndex);
    }
}