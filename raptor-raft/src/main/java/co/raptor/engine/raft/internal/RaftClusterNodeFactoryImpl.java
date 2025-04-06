package co.raptor.engine.raft.internal;

import co.raptor.engine.raft.RaftPeerConfig;
import co.raptor.engine.raft.api.RaftClusterNode;
import co.raptor.engine.raft.api.RaftClusterNodeFactory;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link RaftClusterNodeFactory} interface.
 * <p>
 * This class is responsible for creating instances of {@link RaftClusterNode}
 * by configuring and building the underlying Raft server with the appropriate
 * parameters. It uses the Ratis framework to facilitate Raft consensus.
 */
public class RaftClusterNodeFactoryImpl implements RaftClusterNodeFactory {
    private static final Logger logger = LoggerFactory.getLogger(RaftClusterNodeFactoryImpl.class);

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

    /**
     * Creates a Raft cluster node and initializes it as the first member of a new Raft group.
     * <p>
     * This method is designed for scenarios where a new Raft group is being created, and the joining
     * node becomes its first and only member.
     * The Raft group is identified by a unique {@code groupUUID},
     * and the joining peer is configured using {@link RaftPeerConfig}.
     * <p>
     * The method sets up a new Raft group internally, without requiring details of other peers,
     * as there are no existing members in the group.
     * This is particularly useful when bootstrapping a
     * new Raft cluster from scratch.
     * <p>
     * <b>Key Configuration:</b>
     * <ul>
     *     <li>The {@code groupUUID} uniquely identifies the new Raft group that the node belongs to.</li>
     *     <li>The {@code joiningPeerConfig} specifies the network and identifier details of the newly created node.</li>
     * </ul>
     *
     * @param groupUUID         a universally unique identifier (UUID) for the new Raft group.
     * @param joiningPeerConfig the configuration of the first/initial node in the group, including its
     *                          unique identifier and network details.
     * @return an instance of {@link RaftClusterNode} representing the first member of the new Raft group.
     * @throws IOException if an error occurs during the creation or initialization of the Raft server.
     */
    public RaftClusterNode createNode(UUID groupUUID, RaftPeerConfig joiningPeerConfig) throws IOException {
        RaftProperties raftProperties = new RaftProperties();

        RaftGroupId raftGroupId = RaftGroupId.valueOf(groupUUID);
        RaftPeer joiningPeer = RaftPeerMapper.toRatisPeer(joiningPeerConfig);


        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, joiningPeer);
        RaftPeerId raftPeerId = RaftPeerId.valueOf(joiningPeerConfig.peerID());
        RaftServer server = RaftServer.newBuilder()
                .setGroup(raftGroup)
                .setProperties(raftProperties)
                .setStateMachine(new BaseStateMachine())
                .setOption(RaftStorage.StartupOption.RECOVER)
                .setServerId(raftPeerId)
                .build();

        return new RaftClusterNodeImpl(server);
    }

    /**
     * Creates a Raft cluster node and joins it to an existing Raft group.
     * <p>
     * This method configures and initializes a Raft server for a node, allowing it to participate
     * in an already-established Raft group.
     * The group to be joined is identified by its {@code groupUUID},
     * and the configuration of the existing members and the joining peer are provided via
     * {@link RaftPeerConfig}.
     * <p>
     * The method validates that the necessary configurations are provided (e.g., the existing peers'
     * configuration must not be empty), as the operation assumes the presence of an operational Raft group.
     * The provided joining peer is added to the group, and the node is prepared to participate in
     * the consensus process.
     * <p>
     * <b>Key Configuration:</b>
     * <ul>
     *     <li>The {@code groupUUID} uniquely identifies the target Raft group.</li>
     *     <li>The {@code existingPeersConfig} provides information about the current members of the group.</li>
     *     <li>The {@code joiningPeerConfig} specifies the network and identifier details of the peer
     *     that is being added to the group.</li>
     * </ul>
     *
     * @param groupUUID           a universally unique identifier (UUID) for the Raft group to be joined.
     * @param existingPeersConfig the configuration details for the current Raft group members.
     *                            This includes
     *                            peer identifiers and their respective network addresses.
     * @param joiningPeerConfig   the configuration for the peer that is joining the group,
     *                            including its unique identifier and network settings.
     * @return an instance of {@link RaftClusterNode} representing the newly added node in the Raft group.
     * @throws IllegalArgumentException if {@code groupUUID}, {@code existingPeersConfig}, or
     *                                  {@code joiningPeerConfig} is null or invalid (e.g., empty).
     * @throws IOException              if an error occurs during the creation or initialization of the Raft server.
     */
    @Override
    public RaftClusterNode createNode(UUID groupUUID, List<RaftPeerConfig> existingPeersConfig, RaftPeerConfig joiningPeerConfig) throws IOException {
        if (existingPeersConfig.isEmpty()) {
            logger.error("existingPeersConfig must not be empty");
            throw new IllegalArgumentException("existingPeersConfig must not be empty");
        }
        if (joiningPeerConfig == null) {
            logger.error("joiningPeerConfig must not be null");
            throw new IllegalArgumentException("joiningPeerConfig must not be null");
        }
        if (groupUUID == null) {
            logger.error("groupUUID must not be null");
            throw new IllegalArgumentException("groupUUID must not be null");
        }

        RaftProperties raftProperties = new RaftProperties();

        RaftGroupId raftGroupId = RaftGroupId.valueOf(groupUUID);

        Set<RaftPeer> ratisPeers = existingPeersConfig.stream()
                .map(RaftPeerMapper::toRatisPeer)
                .collect(Collectors.toSet());

        RaftPeer joiningPeer = RaftPeerMapper.toRatisPeer(joiningPeerConfig);
        ratisPeers.add(joiningPeer);

        RaftGroup raftGroup = RaftGroup.valueOf(raftGroupId, ratisPeers);
        RaftPeerId raftPeerId = RaftPeerId.valueOf(joiningPeerConfig.peerID());
        RaftServer server = RaftServer.newBuilder()
                .setGroup(raftGroup)
                .setProperties(raftProperties)
                .setStateMachine(new BaseStateMachine())
                .setOption(RaftStorage.StartupOption.RECOVER)
                .setServerId(raftPeerId)
                .build();

        return new RaftClusterNodeImpl(server);
    }
}
