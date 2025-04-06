package co.raptor.engine.raft.api;

import co.raptor.engine.raft.RaftPeerConfig;

import java.io.IOException;

/**
 * Factory interface for creating instances of {@link RaftClusterNode}.
 * <p>
 * This interface provides methods for creating different configurations
 * of Raft cluster nodes, such as standalone nodes or nodes participating
 * in a larger cluster. The main goal is to decouple the creation logic from
 * the implementation of the Raft node itself.
 */
public interface RaftClusterNodeFactory {

    /**
     * Creates a standalone Raft cluster node.
     * <p>
     * A standalone Raft node can be used for testing, local simulation,
     * or as part of specific scenarios where a single Raft node is required
     * without being integrated into a larger cluster. The created node will
     * use the configuration provided in the {@link RaftPeerConfig}.
     *
     * @param raftPeerConfig the configuration for the Raft node, including
     *                       details such as network settings, identifiers,
     *                       and roles.
     * @return an instance of {@link RaftClusterNode} configured as a standalone node.
     * @throws IOException if an error occurs during the creation process,
     *                     such as the inability to load configurations or
     *                     initialize required resources.
     */
    RaftClusterNode createStandaloneNode(RaftPeerConfig raftPeerConfig) throws IOException;
}
