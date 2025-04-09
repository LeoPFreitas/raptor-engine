package co.raptor.engine.client.performance;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.*;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

public class Main {

    private static final String ADDRESS = "localhost:9091";
    private static final String SERVER_ID = "n0";
    private static final String RAFT_GROUP_UUID = "2112095a-27fc-4732-9b57-797a3be0f728";

    public static void main(String[] args) {
        // Define the Raft group and peer
        RaftPeer peer = RaftPeer.newBuilder()
                .setId(SERVER_ID)
                .setAddress(ADDRESS)
                .build();

        RaftGroupId raftGroupId = RaftGroupId.valueOf(UUID.fromString(RAFT_GROUP_UUID));
        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, Collections.singletonList(peer));

        // Create the Raft client
        RaftProperties properties = new RaftProperties();
        GrpcConfigKeys.Server.setPort(properties, 9091);

        try (RaftClient client = RaftClient.newBuilder()
                .setRaftGroup(raftGroup)
                .setProperties(properties)
                .build()) {

            // Print leader information
            System.out.println("Connecting to Raft group...");
            RaftPeerId leaderId = client.getLeaderId();
            System.out.println("Leader identified: " + leaderId);

            // Send proper commands to the Raft group synchronously
            int numberOfRequests = 100; // Number of operations to send
            for (int i = 0; i < numberOfRequests; i++) {
                // Alternate between Credit, Debit, and GetBalance commands
                Message message;
                if (i % 3 == 0) {
                    // Credit example: "CREDIT:100.0"
                    byte[] commandBytes = CommandUtils.encodeCreditCommand(100.0 + i);
                    message = Message.valueOf(new String(commandBytes));
                } else if (i % 3 == 1) {
                    // Debit example: "DEBIT:50.0"
                    byte[] commandBytes = CommandUtils.encodeDebitCommand(50.0 + i);
                    message = Message.valueOf(new String(commandBytes));
                } else {
                    // GetBalance example: "GET_BALANCE"
                    byte[] commandBytes = CommandUtils.encodeGetBalanceCommand();
                    message = Message.valueOf(new String(commandBytes));
                }

                try {
                    RaftClientReply reply = client.io().send(message); // Blocking call (synchronous)
                    if (reply != null && reply.isSuccess()) {
                        System.out.println("Command executed successfully: " + message.getContent());
                    } else {
                        System.err.println("Command failed: " + message.getContent());
                    }
                } catch (Exception e) {
                    System.err.println("Error occurred while sending command: " + e.getMessage());
                }
            }

            System.out.println("All commands sent and processed successfully.");

        } catch (IOException e) {
            // Handle exceptions related to resource initialization
            System.err.println("Failed to initialize Raft client: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle other unexpected exceptions
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}