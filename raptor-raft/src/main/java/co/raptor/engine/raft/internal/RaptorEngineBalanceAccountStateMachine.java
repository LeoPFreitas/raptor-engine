package co.raptor.engine.raft.internal;

import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.apache.ratis.thirdparty.com.google.protobuf.UnsafeByteOperations;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class RaptorEngineBalanceAccountStateMachine extends BaseStateMachine {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        final RaftProtos.LogEntryProto entry = trx.getLogEntry();
        final TermIndex termIndex = TermIndex.valueOf(entry);
        final int incremented = incrementCounter(termIndex);

        return CompletableFuture.completedFuture(Message.valueOf(toByteString(incremented)));
    }

    private int incrementCounter(TermIndex termIndex) {
        updateLastAppliedTermIndex(termIndex);
        return counter.incrementAndGet();
    }

    static ByteString toByteString(int n) {
        final byte[] array = new byte[4];
        ByteBuffer.wrap(array).putInt(n);
        return UnsafeByteOperations.unsafeWrap(array);
    }
}