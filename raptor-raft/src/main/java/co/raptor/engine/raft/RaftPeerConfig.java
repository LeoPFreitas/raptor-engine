package co.raptor.engine.raft;

import java.net.InetSocketAddress;

/**
 * Configuration for a Raft peer.
 * <p>
 * This record encapsulates the necessary details about a Raft peer, including
 * its unique identifier and the network address it uses to communicate with
 * other peers in the Raft cluster.
 *
 * @param peerID            the unique identifier of the Raft peer
 * @param inetSocketAddress the network address of the Raft peer, represented as an {@link InetSocketAddress}
 */
public record RaftPeerConfig(String peerID, InetSocketAddress inetSocketAddress) {
}
