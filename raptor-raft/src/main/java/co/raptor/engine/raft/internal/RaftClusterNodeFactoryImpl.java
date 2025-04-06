package co.raptor.engine.raft.internal;

import co.raptor.engine.raft.RaftPeerConfig;
import co.raptor.engine.raft.api.RaftClusterNode;
import co.raptor.engine.raft.api.RaftClusterNodeFactory;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.impl.BaseStateMachine;

import java.io.IOException;

/**
 * Implementation of the {@link RaftClusterNodeFactory} interface.
 * <p>
 * This class is responsible for creating instances of {@link RaftClusterNode}
 * by configuring and building the underlying Raft server with the appropriate
 * parameters. It uses the Ratis framework to facilitate Raft consensus.
 */
public class RaftClusterNodeFactoryImpl implements RaftClusterNodeFactory {

    /**
     * Creates a standalone Raft cluster node based on the provided configuration.
     * <p>
     * This method builds a Raft server with a minimal configuration
     * required for a standalone node. The Raft server is initialized with:
     * <ul>
     *     <li>An empty Raft group using {@link RaftGroup#emptyGroup()}</li>
     *     <li>Default {@link RaftProperties} for the Raft server</li>
     *     <li>A simple state machine implementation ({@link BaseStateMachine})</li>
     *     <li>Startup option {@code RECOVER} for handling recovery scenarios</li>
     *     <li>The server's unique identifier generated from the provided configuration</li>
     * </ul>
     *
     * @param raftPeerConfig the configuration information for the Raft node,
     *                       including peer identifiers and other required settings.
     * @return an implementation of {@link RaftClusterNode} wrapping the constructed Raft server.
     * @throws IOException if an error occurs while building the Raft server or initializing resources.
     */
    @Override
    public RaftClusterNode createStandaloneNode(RaftPeerConfig raftPeerConfig) throws IOException {

        RaftProperties raftProperties = new RaftProperties();

        RaftPeerId raftPeerId = RaftPeerId.valueOf(raftPeerConfig.peerID());

        RaftServer server = RaftServer.newBuilder()
                .setGroup(RaftGroup.emptyGroup())
                .setProperties(raftProperties)
                .setStateMachine(new BaseStateMachine())
                .setOption(RaftStorage.StartupOption.RECOVER)
                .setServerId(raftPeerId)
                .build();


        return new RaftClusterNodeImpl(server);
    }
}
