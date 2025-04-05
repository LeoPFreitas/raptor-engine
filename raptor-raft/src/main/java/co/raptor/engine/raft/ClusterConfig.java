package co.raptor.engine.raft;

import java.util.List;
import java.util.UUID;

public record ClusterConfig(UUID groupId, List<RaftPeerConfig> raftPeerConfigs) {
}

