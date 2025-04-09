package co.raptor.engine.raft;

import java.util.List;
import java.util.UUID;

/**
 * Immutable configuration class representing the configuration of a Raft group.
 * <p>
 * This record encapsulates the Raft group identifier and the configurations of the group's peers.
 * It is designed to be lightweight and thread-safe, leveraging Java's {@code record} for immutability.
 *
 * @param groupId     the unique identifier of the Raft group
 * @param peersConfig the list of {@link RaftPeerConfig} objects representing the configuration of each peer in the group
 */
public record RaftGroupConfig(UUID groupId, List<RaftPeerConfig> peersConfig) {
}

