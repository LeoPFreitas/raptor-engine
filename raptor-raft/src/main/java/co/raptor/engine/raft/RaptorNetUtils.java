package co.raptor.engine.raft;

import org.apache.ratis.util.NetUtils;

import java.net.InetSocketAddress;

public class RaptorNetUtils {

    public static InetSocketAddress createSocketAddress(String host) {
        return NetUtils.createSocketAddr(host, NetUtils.getFreePort());
    }

}
