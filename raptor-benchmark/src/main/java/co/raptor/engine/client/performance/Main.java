package co.raptor.engine.client.performance;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.*;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main {

    // Constants for configurations
    private static final String ADDRESS = "localhost:9091";
    private static final String SERVER_ID = "n0";
    private static final String RAFT_GROUP_UUID = "2112095a-27fc-4732-" +
            "9b57-797a3be0f728";

    public static void main(String[] args) {
        // Initialize Raft peer group and properties
        RaftPeer peer = RaftPeer.newBuilder()
                .setId(SERVER_ID)
                .setAddress(ADDRESS)
                .build();

        RaftGroupId raftGroupId = RaftGroupId.valueOf(UUID.fromString(RAFT_GROUP_UUID));
        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, Collections.singletonList(peer));

        // Create the Raft client
        try (RaftClient client = RaftClient.newBuilder()
                .setRaftGroup(raftGroup)
                .setProperties(new RaftProperties())
                .build()) {

            // Print leader information
            System.out.println("Connecting to Raft group...");
            RaftPeerId leaderId = client.getLeaderId();
            System.out.println("Leader identified: " + leaderId);

            // Send asynchronous requests to Raft group
            int numberOfRequests = 100; // The number of async messages to send
            CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

            for (int i = 0; i < numberOfRequests; i++) {
                String messageContent = "Message #" + i;
                Message message = Message.valueOf(messageContent);

                // Send asynchronously and handle the response
                CompletableFuture<RaftClientReply> futureReply = client.async().send(message);
                futures[i] = futureReply.thenAccept(reply -> {
                    if (reply != null && reply.isSuccess()) {
                        System.out.println("Command executed successfully: " + messageContent);
                    } else {
                        System.err.println("Command failed for: " + messageContent);
                    }
                }).exceptionally(ex -> {
                    System.err.println("Error occurred: " + ex.getMessage());
                    return null;
                });
            }

            // Wait for all messages to finish
            CompletableFuture.allOf(futures).join();
            System.out.println("All messages sent and processed.");

        } catch (IOException e) {
            // Handle exceptions related to resources
            System.err.println("Failed to initialize Raft client: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle unexpected exceptions
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}