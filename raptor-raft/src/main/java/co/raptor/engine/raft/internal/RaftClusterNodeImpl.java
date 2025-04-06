package co.raptor.engine.raft.internal;

import co.raptor.engine.raft.api.RaftClusterNode;
import org.apache.ratis.server.RaftServer;

import java.io.IOException;

/**
 * Implementation of the {@link RaftClusterNode} interface.
 * <p>
 * This class provides a concrete implementation of a Raft cluster node
 * by wrapping the {@link RaftServer} from the Apache Ratis framework.
 * It delegates core operations, such as starting and closing the node,
 * and retrieving the peer ID, to the underlying Raft server.
 */
public class RaftClusterNodeImpl implements RaftClusterNode {
    private final RaftServer server;

    /**
     * Constructs a new {@code RaftClusterNodeImpl} with the specified Raft server.
     *
     * @param server the underlying {@link RaftServer} that this class wraps
     */
    public RaftClusterNodeImpl(RaftServer server) {
        this.server = server;
    }

    /**
     * Starts the Raft cluster node.
     * <p>
     * This invokes the {@link RaftServer#start()} method to initialize and
     * start the Raft server, enabling it to participate in the Raft consensus
     * algorithm.
     *
     * @throws IOException if the Raft server fails to start, such as when network
     *                     resources can't be initialized.
     */
    @Override
    public void start() throws IOException {
        server.start();
    }

    /**
     * Shuts down the Raft cluster node.
     * <p>
     * This method safely stops the Raft server by invoking {@link RaftServer#close()},
     * releasing any resources, and removing the node from participation in the Raft
     * consensus algorithm.
     *
     * @throws IOException if an error occurs during closing, such as issues with
     *                     releasing resources.
     */
    @Override
    public void close() throws IOException {
        server.close();
    }

    /**
     * Retrieves the unique identifier of this Raft cluster node.
     * <p>
     * The peer ID is obtained from the underlying {@link RaftServer}'s configuration
     * and represents the unique identity of this node within the cluster.
     *
     * @return the unique peer ID of this Raft cluster node as a {@code String}
     */
    @Override
    public String getPeerId() {
        return server.getId().toString();
    }
}
