package co.raptor.engine.raft;

import org.apache.ratis.util.NetUtils;

import java.net.InetSocketAddress;

/**
 * Utility class for network-related operations specific to the Raptor engine.
 * <p>
 * This class provides helper methods for creating socket addresses, leveraging
 * the Apache Ratis {@link NetUtils} library to dynamically allocate free ports
 * and construct socket addresses.
 */
public class RaptorNetUtils {

    /**
     * Creates a new {@link InetSocketAddress} for the specified host with a dynamically allocated free port.
     * <p>
     * This method uses {@link NetUtils#getFreePort()} to find an available port
     * and {@link NetUtils#createSocketAddr(String, int)} to create the resulting socket address.
     *
     * @param host the hostname or IP address for the socket
     * @return an {@code InetSocketAddress} representing the specified host with a dynamically assigned port
     * @throws IllegalArgumentException if the provided host is invalid
     */

    public static InetSocketAddress createSocketAddress(String host) {
        return NetUtils.createSocketAddr(host, NetUtils.getFreePort());
    }

    /**
     * Creates a new {@link InetSocketAddress} for the specified host and port.
     * <p>
     * This method uses {@link NetUtils#createSocketAddrForHost(String, int)} to create the
     * socket address directly using the provided host and port.
     *
     * @param host the hostname or IP address for the socket
     * @param port the port number for the socket
     * @return an {@code InetSocketAddress} representing the specified host and port
     * @throws IllegalArgumentException if the provided host or port is invalid
     */
    public static InetSocketAddress createSocketAddress(String host, int port) {
        return NetUtils.createSocketAddrForHost(host, port);
    }

}
