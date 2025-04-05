package co.raptor.engine.raft;

import co.raptor.engine.raft.internal.RaftServerNode;
import co.raptor.engine.raft.internal.RaptorEngineServerInternalFactory;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.statemachine.StateMachine;
import org.apache.ratis.statemachine.impl.BaseStateMachine;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class RaptorEngineServerFactory {

    // Thread-safe cache for RaptorEngineServers
    private static final ConcurrentMap<String, RaftServerNode> NODE_CACHE = new ConcurrentHashMap<>();

    private RaptorEngineServerFactory() {

    }

    public static RaftServerNode createNode(RaftPeerConfig raftPeerConfig, ClusterConfig clusterConfig) {
        String nodeId = raftPeerConfig.peerID();

        // Use the cache to ensure thread-safe reusability
        return NODE_CACHE.computeIfAbsent(nodeId, id -> {
            RaftPeer raftPeer = mapToRaftPeer(raftPeerConfig);
            RaftGroup raftGroup = mapToRaftGroup(clusterConfig);
            RaftProperties raftProperties = mapToRaftProperties(clusterConfig);
            StateMachine stateMachine = createStateMachine(clusterConfig);

            // Delegate the creation to the internal factory
            return RaptorEngineServerInternalFactory.createServer(raftGroup, raftPeer, stateMachine, raftProperties);
        });
    }

    public static List<RaftServerNode> createNodesConcurrently(List<RaftPeerConfig> raftPeerConfigs, ClusterConfig clusterConfig) {
        return raftPeerConfigs.parallelStream()
                .map(raftPeerConfig -> createNode(raftPeerConfig, clusterConfig))
                .collect(Collectors.toList());
    }

    public static boolean cleanupNode(String nodeId) {
        RaftServerNode server = NODE_CACHE.remove(nodeId);
        if (server != null) {
            try {
                server.close();
                return true; // Successfully cleaned up the node.
            } catch (Exception e) {
                e.printStackTrace(); // Log cleanup failure.
            }
        }
        return false;
    }

    public static void cleanupCachedNodes() {
        NODE_CACHE.values().parallelStream().forEach(server -> {
            try {
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        NODE_CACHE.clear();
    }


    private static RaftPeer mapToRaftPeer(RaftPeerConfig raftPeerConfig) {
        return RaftPeer.newBuilder()
                .setId(RaftPeerId.valueOf(raftPeerConfig.peerID()))
                .setAddress(raftPeerConfig.inetSocketAddress())
                .build();
    }

    private static RaftGroup mapToRaftGroup(ClusterConfig clusterConfig) {
        List<RaftPeer> peers = clusterConfig.raftPeerConfigs().stream()
                .map(RaptorEngineServerFactory::mapToRaftPeer)
                .collect(Collectors.toList());
        return RaftGroup.valueOf(RaftGroupId.valueOf(clusterConfig.groupId()), peers);
    }

    private static RaftProperties mapToRaftProperties(ClusterConfig clusterConfig) {
        RaftProperties properties = new RaftProperties();
        // Add custom cluster settings here from ClusterConfig
        return properties;
    }

    private static StateMachine createStateMachine(ClusterConfig clusterConfig) {
        // Instantiate and configure your custom StateMachine here
        return new BaseStateMachine();
    }
}