package co.raptor.engine.raft.internal;


import co.raptor.engine.raft.RaftPeerConfig;
import org.apache.ratis.protocol.RaftPeer;

public class RaftPeerMapper {
    /**
     * Converts a {@link RaftPeerConfig} to a Ratis-specific {@link RaftPeer}.
     *
     * @param peerConfig the Raft peer configuration object
     * @return a corresponding Ratis RaftPeer
     */
    public static RaftPeer toRatisPeer(RaftPeerConfig peerConfig) {
        return RaftPeer.newBuilder()
                .setId(peerConfig.peerID()) // Peer ID
                .setAddress(peerConfig.inetSocketAddress())
                .build();
    }
}
