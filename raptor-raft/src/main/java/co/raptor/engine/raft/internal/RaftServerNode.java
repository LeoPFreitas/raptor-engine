package co.raptor.engine.raft.internal;

import java.io.IOException;

/**
 * Represents a single node in a Raft cluster.
 *
 * <p>This interface abstracts the operations of a Raft server node, including
 * starting, stopping, and retrieving the node's unique identifier. It is part
 * of the internal implementation for managing Raft server instances.</p>
 *
 * <h2>Default Implementation</h2>
 * <p>The default implementation of this interface uses the Apache Ratis library, which
 * provides a high-performance, fault-tolerant implementation of the Raft consensus
 * algorithm. This allows for robust leader election, log replication, and state machine
 * management in a distributed system.</p>
 *
 * <p>While this abstraction currently relies on Ratis, it is designed to allow
 * flexibility for future replacement with another Raft framework if needed.</p>
 *
 * <h2>Usage</h2>
 * <p>This interface is intended to be implemented by classes that manage the lifecycle
 * of a Raft server. Implementations must provide mechanisms to:</p>
 * <ul>
 *   <li>Start the server and participate in the Raft group communication.</li>
 *   <li>Stop the server and release resources gracefully.</li>
 *   <li>Retrieve the unique identifier of the Raft server node.</li>
 * </ul>
 *
 * @see co.raptor.engine.raft.internal.RaptorEngineServer
 */
public interface RaftServerNode {
    /**
     * Starts the Raft server node, initializing its participation in the Raft cluster.
     *
     * @throws IOException if an error occurs while starting the server.
     */
    void start() throws IOException;

    /**
     * Closes the Raft server node, gracefully shutting it down and releasing resources.
     *
     * @throws IOException if an error occurs while closing the server.
     */
    void close() throws IOException;

    /**
     * Retrieves the unique identifier of the Raft server node.
     *
     * <p>The identifier is typically a string representation of the node's ID,
     * which is used for communication within the Raft cluster.</p>
     *
     * @return the unique identifier of the server node.
     */
    String getId();
}