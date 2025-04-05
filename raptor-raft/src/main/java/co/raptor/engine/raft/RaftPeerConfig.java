package co.raptor.engine.raft;

import java.net.InetSocketAddress;

public record RaftPeerConfig(String peerID, InetSocketAddress inetSocketAddress) {
}
