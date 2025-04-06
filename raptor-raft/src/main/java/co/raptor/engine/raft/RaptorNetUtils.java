package co.raptor.engine.raft;

import org.apache.ratis.util.NetUtils;

import java.net.InetSocketAddress;

/**
 * Utility class for network-related operations specific to the Raptor engine.
 * <p>
 * This class provides a helper method for creating socket addresses, leveraging
 * functionalities from the Apache Ratis {@link NetUtils} library.
 */
public class RaptorNetUtils {

    /**
     * Creates a new {@link InetSocketAddress} based on the provided host, with an available port.
     * <p>
     * This method uses the Apache Ratis {@link NetUtils#getFreePort()} to dynamically allocate
     * a free port, and the {@link NetUtils#createSocketAddr(String, int)}
     * to construct the resulting socket address.
     *
     * @param host the hostname or IP address for the socket
     * @return an {@code InetSocketAddress} representing the specified host with a dynamically assigned port
     */
    public static InetSocketAddress createSocketAddress(String host) {
        return NetUtils.createSocketAddr(host, NetUtils.getFreePort());
    }

}
