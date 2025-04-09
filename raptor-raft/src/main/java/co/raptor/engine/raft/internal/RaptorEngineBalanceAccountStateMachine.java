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
        ByteString logData = entry.getStateMachineLogEntry().getLogData();
        String command = logData.toStringUtf8();
        String[] parts = command.split(":"); // Example: CREDIT,100
        RaptorEngineCommand cmd = RaptorEngineCommand.valueOf(parts[0]);
        double amount = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0;

        CompletableFuture<Message> result = new CompletableFuture<>();
        switch (cmd) {
            case CREDIT:
                balance.addAndGet(amount);
                result.complete(() -> ByteString.fromHex("CREDIT applied"));
                break;

            case DEBIT:
                boolean success = balance.accumulateAndGet(amount, (current, delta) -> {
                    if (current >= delta) {
                        return current - delta;
                    } else {
                        logger.error("Insufficient funds: current={}, delta={}", current, delta);
                        return current;
                    }
                }) >= 0;

                if (success) {
                    result.complete(() -> ByteString.fromHex("DEBIT applied successfully"));
                } else {
                    result.completeExceptionally(new IllegalStateException("Insufficient funds"));
                }
                break;

            case GET_BALANCE:
                result.complete(() -> ByteString.fromHex("Current Balance: " + balance.get()));
                break;

            default:
                result.completeExceptionally(new UnsupportedOperationException("Invalid Command"));
        }
        return result;
    }

    static ByteString toByteString(int n) {
        final byte[] array = new byte[4];
        ByteBuffer.wrap(array).putInt(n);
        return UnsafeByteOperations.unsafeWrap(array);
    }
}