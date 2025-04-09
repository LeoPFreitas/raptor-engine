package co.raptor.engine.client;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        // Define the Raft group and configure peers
        RaftPeer peer = RaftPeer.newBuilder()
                .setId("n0")
                .setAddress("localhost:9091")
                .build();

        // Assign a predefined group ID (ensure this matches your server configuration)
        RaftGroupId raftGroupId = RaftGroupId.valueOf(UUID.fromString("2112095a-27fc-4732-9b57-797a3be0f728"));

        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, Arrays.asList(peer));

        // Create the Raft client
        try (RaftClient client = RaftClient.newBuilder()
                .setRaftGroup(raftGroup)
                .setProperties(new RaftProperties())
                .build()) {

            // Print out leader information
            System.out.println("Sending commands to the Raft group...");
            RaftPeerId leaderId = client.getLeaderId();
            System.out.println("Leader: " + leaderId);

            // Send asynchronous requests to the Raft group
            CompletableFuture<?>[] futures = new CompletableFuture[1_000_000];
            for (int i = 0; i < 100; i++) {
                // Create a message to send
                String messageContent = "Message #" + i;
                Message message = Message.valueOf(messageContent);

                // Send the message asynchronously
                CompletableFuture<RaftClientReply> futureReply = client.async().send(message);

                // Process and handle the reply in the future
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

            // Wait for all asynchronous operations to complete
            CompletableFuture.allOf(futures).join();
            System.out.println("All commands sent and processed successfully.");

        } catch (IOException e) {
            // Error handling in case of failure to create or use the client
            System.err.println("Error initializing Raft client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}