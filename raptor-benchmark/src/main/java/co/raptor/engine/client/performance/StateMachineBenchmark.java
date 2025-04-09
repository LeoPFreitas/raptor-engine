package co.raptor.engine.client.performance;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 30, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
@Threads(32)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StateMachineBenchmark {
    public static final String ADDRESS = "localhost:9091";
    public static final Integer PORT = 9091;
    public static final String SERVER_ID = "n0";
    public static final String RAFT_GROUP_UUID = "2112095a-27fc-4732-9b57-797a3be0f728";

    private RaftClient client;

    private final ThreadLocal<Integer> commandCounter = ThreadLocal.withInitial(() -> 0);

    @Setup
    public void setup() throws IOException {
        // Create a Raft client as done in the provided example
        RaftPeer peer = RaftPeer.newBuilder()
                .setId(SERVER_ID)
                .setAddress(ADDRESS)
                .build();

        // Assign a predefined group ID
        RaftGroupId raftGroupId = RaftGroupId.valueOf(UUID.fromString(RAFT_GROUP_UUID));
        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, Collections.singletonList(peer));

        RaftProperties properties = new RaftProperties();
        GrpcConfigKeys.Server.setPort(properties, PORT);

        // Initialize the raft client for benchmarking
        client = RaftClient.newBuilder()
                .setRaftGroup(raftGroup)
                .setProperties(properties)
                .build();
    }

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

    @TearDown
    public void tearDown() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close Raft client: " + e.getMessage());
        }
    }

    @Benchmark
    public void benchmarkCreditTransaction() throws Exception {
        String creditCommand = generateCommand("CREDIT", 100.0);
        executeCommand(creditCommand);
    }

    @Benchmark
    public void benchmarkDebitTransaction() throws Exception {
        String debitCommand = generateCommand("DEBIT", 50.0);
        executeCommand(debitCommand);
    }

    @Benchmark
    public void benchmarkGetBalanceOperation() throws Exception {
        executeCommand("GET_BALANCE");
    }

    // Helper method to generate dynamic credit/debit commands
    private String generateCommand(String operation, double amount) {
        int counter = commandCounter.get();
        commandCounter.set(counter + 1); // Increment the thread-local counter
        return operation + ":" + amount + ":" + counter;
    }

    // Generic method for executing a command
    private void executeCommand(String command) throws Exception {
        Message message = Message.valueOf(command);

        try {
            // Perform synchronous send operation
            RaftClientReply reply = client.io().send(message);

            if (!reply.isSuccess()) {
                throw new RuntimeException("Command failed: " + command + ", Error: " + reply.getException());
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + command + ", Details: " + e.getMessage());
            throw e;
        }
    }
}
