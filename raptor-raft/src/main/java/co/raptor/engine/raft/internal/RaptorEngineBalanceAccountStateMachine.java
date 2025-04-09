package co.raptor.engine.raft.internal;

import co.raptor.engine.raft.RaptorEngineCommand;
import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.thirdparty.com.google.common.util.concurrent.AtomicDouble;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.apache.ratis.thirdparty.com.google.protobuf.UnsafeByteOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class RaptorEngineBalanceAccountStateMachine extends BaseStateMachine {
    private static final Logger logger = LoggerFactory.getLogger(RaptorEngineBalanceAccountStateMachine.class);

    private final AtomicDouble balance = new AtomicDouble(0.0);

    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        RaftProtos.LogEntryProto entry = trx.getLogEntry();
        // Extract log data
        ByteString logData = entry.getStateMachineLogEntry().getLogData();
        String command;
        try {
            command = logData.toStringUtf8();
        } catch (Exception e) {
            logger.error("Failed to parse log data from transaction", e);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid log data format"));
        }

        // Split command and handle parsing errors
        String[] parts = command.split(":");
        if (parts.length < 1) {
            logger.error("Invalid command format: {}", command);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid command format"));
        }

        String commandType = parts[0];
        double amount = 0.0;
        if (parts.length > 1) {
            try {
                amount = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                logger.error("Invalid amount format in command: {}", command, e);
                return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid amount format"));
            }
        }

        // Handle commands
        CompletableFuture<Message> result = new CompletableFuture<>();
        try {
            switch (RaptorEngineCommand.valueOf(commandType)) {
                case CREDIT -> {
                    double newBalance = balance.addAndGet(amount);
                    logger.info("Credited amount. New Balance: {}", newBalance);
                    result.complete(Message.valueOf(ByteString.copyFromUtf8("CREDIT applied: New Balance=" + newBalance)));
                }
                case DEBIT -> {
                    if (balance.get() >= amount) {
                        double updatedBalance = balance.addAndGet(-amount);
                        logger.info("Debited amount. New Balance: {}", updatedBalance);
                        result.complete(Message.valueOf(ByteString.copyFromUtf8("DEBIT applied: New Balance=" + updatedBalance)));
                    } else {
                        logger.error("Insufficient funds: balance={}, debit amount={}", balance.get(), amount);
                        result.completeExceptionally(new IllegalStateException("Insufficient funds"));
                    }
                }
                case GET_BALANCE -> {
                    logger.info("Getting balance: {}", balance.get());
                    result.complete(Message.valueOf(ByteString.copyFromUtf8("Current Balance=" + balance.get())));
                }
                default -> {
                    logger.error("Unsupported command type: {}", commandType);
                    result.completeExceptionally(new UnsupportedOperationException("Invalid Command"));
                }
            }
        } catch (Exception e) {
            logger.error("Error processing command: {}", command, e);
            result.completeExceptionally(e);
        }

        return result;
    }

    static ByteString toByteString(int n) {
        final byte[] array = new byte[4];
        ByteBuffer.wrap(array).putInt(n);
        return UnsafeByteOperations.unsafeWrap(array);
    }
}