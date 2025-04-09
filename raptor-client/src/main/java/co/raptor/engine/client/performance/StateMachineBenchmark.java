package co.raptor.engine.client.performance;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 30, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
@Threads(32)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StateMachineBenchmark {

    private RaftClient client;

    @Setup
    public void setup() throws IOException {
        // Create a Raft client as done in the provided example
        RaftPeer peer = RaftPeer.newBuilder()
                .setId("n0")
                .setAddress("localhost:9091")
                .build();

        // Assign a predefined group ID
        RaftGroupId raftGroupId = RaftGroupId.valueOf(UUID.fromString("2112095a-27fc-4732-9b57-797a3be0f728"));
        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, Collections.singletonList(peer));

        // Initialize the raft client for benchmarking
        client = RaftClient.newBuilder()
                .setRaftGroup(raftGroup)
                .setProperties(new RaftProperties())
                .build();
    }

    @TearDown
    public void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    AtomicInteger pendingFutures = new AtomicInteger();

    @Benchmark
    public void testSyncSendMessage() throws Exception {
        String messageContent = "Benchmark sync test message";
        Message message = Message.valueOf(messageContent);

        try {
            // Perform synchronous send operation (blocks until the response is received)
            RaftClientReply reply = client.io().send(message);

            // Validate the result (optional)
            if (!reply.isSuccess()) {
                throw new RuntimeException("Reply failed: " + reply.getException());
            }
        } catch (Exception e) {
            // Handle exceptions if the request fails
            System.err.println("Message failed: " + e);
            throw e; // Rethrow to ensure errors are not suppressed
        }
    }


}
