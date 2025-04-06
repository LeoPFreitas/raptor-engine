package co.raptor.engine.raft.api;

import co.raptor.engine.raft.RaftGroupConfig;
import co.raptor.engine.raft.RaftPeerConfig;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Factory interface for managing the creation of {@link RaftClusterNode} instances.
 * <p>
 * This interface defines methods for instantiating Raft cluster nodes, including standalone nodes
 * or nodes integrated into larger, distributed Raft clusters.
 * The factory encapsulates the node creation logic,
 * minimizing direct dependencies on low-level implementation details of the Raft.
 * <p>
 * Implementations of this interface use frameworks like Apache Ratis to facilitate
 * consensus and replication between Raft nodes.
 */

public interface RaftClusterNodeFactory {
    /**
     * Creates a standalone Raft cluster node.
     * <p>
     * This method builds a single-node Raft cluster, which can be used for testing purposes
     * or as a local simulation environment. It is not part of any larger distributed group.
     * <p>
     * A standalone node operates independently and uses the configuration defined in
     * {@link RaftPeerConfig}, which specifies details like network address and peer ID.
     *
     * @param raftPeerConfig the configuration for the standalone Raft node. It should include
     *                       unique identifiers and necessary network settings for the node.
     * @return an instance of {@link RaftClusterNode} initialized as a standalone node.
     * @throws IOException if an error occurs during node instantiation or configuration.
     */
    RaftClusterNode createStandaloneNode(RaftPeerConfig raftPeerConfig) throws IOException;


    /**
     * Creates a Raft cluster node and integrates it into a specified group.
     * <p>
     * This method configures and initializes a Raft server, allowing it to participate in a
     * Raft cluster.
     * The cluster's configuration is defined via {@link RaftGroupConfig}, which
     * includes the group ID and details of all participating peers.
     * Additionally, the unique
     * identifier and network details for the joining node are specified via {@link RaftPeerConfig}.
     * <p>
     * Dynamic memberships or new node additions to existing Raft clusters are made possible
     * through this API.
     * <p>
     * <b>Configuration Highlights:</b>
     * <ul>
     *     <li>The provided {@code groupUUID} represents the unique identifier for the cluster.</li>
     *     <li>{@link RaftPeerConfig} provides details for the joining peer, such as its peer ID and network settings.</li>
     *     <li>Existing peers in the cluster are defined by {@link java.util.List} of {@code RaftPeerConfig}.</li>
     * </ul>
     *
     * @param groupUUID           a universally unique identifier (UUID) for the Raft group.
     * @param existingPeersConfig the configuration of existing peers in the Raft group.
     *                            This defines
     *                            the current members of the cluster.
     * @param joiningPeerConfig   the configuration for the joining node, including its unique identifier
     *                            and network address.
     * @return an initialized {@link RaftClusterNode} instance corresponding to the newly added member.
     * @throws IOException if an error occurs during the setup of the Raft server or group configuration.
     */
    RaftClusterNode createNode(UUID groupUUID, List<RaftPeerConfig> existingPeersConfig, RaftPeerConfig joiningPeerConfig) throws IOException;


    /**
     * Creates a Raft cluster node
     * and initializes it as the first member of a new Raft group with no other mapped peers.
     * <p>
     * This method is intended for scenarios where a Raft group is being created, and the node
     * being added is the initial (and sole) member of the group.
     * By providing the unique
     * identifier of the new group and the configuration for the joining peer, this method
     * enables the creation and setup of a functional standalone Raft group.
     * <p>
     * The implementation retrieves or derives any additional details required for the new
     * group from internal state or helper mechanisms, eliminating the need for existing
     * cluster information such as peers or configurations.
     *
     * @param groupUUID         a universally unique identifier (UUID) for the new Raft group.
     * @param joiningPeerConfig the configuration of the peer that is initializing the group,
     *                          including its network address and unique identifier.
     * @return an instance of {@link RaftClusterNode} representing the first node in the new group.
     * @throws IOException if an error occurs during server initialization or group configuration.
     */
    RaftClusterNode createNode(UUID groupUUID, RaftPeerConfig joiningPeerConfig) throws IOException;
}
