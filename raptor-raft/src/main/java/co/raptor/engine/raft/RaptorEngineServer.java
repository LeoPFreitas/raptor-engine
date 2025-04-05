package co.raptor.engine.raft;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.util.NetUtils;
import org.apache.ratis.util.TimeDuration;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public class RaptorEngineServer implements Closeable {
    private final RaftServer server;

    public RaptorEngineServer() {
        final RaftProperties properties = new RaftProperties();
        RaftServerConfigKeys.Read.setOption(properties, RaftServerConfigKeys.Read.Option.LINEARIZABLE);
        RaftServerConfigKeys.Read.setTimeout(properties, TimeDuration.ONE_MINUTE);

        BaseStateMachine baseStateMachine = new BaseStateMachine();


        RaftPeer raftPeer = RaftPeer.newBuilder()
                .setId("server")
                .setAddress("localhost:9000")
                .build();

        final int port = NetUtils.createSocketAddr(raftPeer.getAddress()).getPort();
        GrpcConfigKeys.Server.setPort(properties, port);

        RaftGroup raftGroup = RaftGroup.valueOf(RaftGroupId.valueOf(UUID.randomUUID()), raftPeer);

        try {
            this.server = RaftServer.newBuilder()
                    .setGroup(raftGroup)
                    .setProperties(properties)
                    .setStateMachine(baseStateMachine)
                    .setServerId(raftPeer.getId())
                    .setOption(RaftStorage.StartupOption.RECOVER)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    public void start() throws IOException {
        server.start();
    }
}
