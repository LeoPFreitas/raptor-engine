package co.raptor.engine.raft;

import java.util.List;
import java.util.UUID;

public record RaftGroupConfig(UUID groupId, List<RaftPeerConfig> peersConfig) {
}

