package co.raptor.engine.raft.api;

import java.io.IOException;

/**
 * Represents a node in a Raft consensus cluster.
 * <p>
 * Each node in the cluster communicates with other nodes to maintain consensus
 * and ensure reliability and fault tolerance.
 * The interface allows for managing the lifecycle of a node and retrieving its peer identifier.
 */
public interface RaftClusterNode {

    /**
     * Starts the Raft cluster node.
     * <p>
     * This method initializes and starts the necessary resources for
     * the node to participate in the Raft consensus algorithm.
     * The node will become active and start exchanging messages with other nodes in
     * the cluster.
     *
     * @throws IOException if an error occurs while starting the node, such as
     *                     the inability to initialize networking resources.
     */
    void start() throws IOException;

    /**
     * Closes the Raft cluster node.
     * <p>
     * This method shuts down the node and releases any resources allocated during its lifecycle.
     * The node will stop participating in the Raft consensus protocol.
     *
     * @throws IOException if an error occurs while closing the node, such as
     *                     network shutdown failure or resource cleanup issues.
     */
    void close() throws IOException;

    /**
     * Retrieves the peer identifier of this node.
     * <p>
     * The peer ID is a unique identifier for this node in the cluster.
     * It is used by other nodes to recognize and communicate with this node during
     * consensus operations.
     *
     * @return the unique peer identifier of this node.
     */
    String getPeerId();
}