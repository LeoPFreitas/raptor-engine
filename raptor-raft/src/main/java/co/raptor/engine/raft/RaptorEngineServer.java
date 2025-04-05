package co.raptor.engine.raft;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.util.TimeDuration;

import java.io.Closeable;
import java.io.IOException;

public class RaptorEngineServer implements Closeable {
    private final RaftServer server;

    public RaptorEngineServer() {
        final RaftProperties properties = new RaftProperties();
        RaftServerConfigKeys.Read.setOption(properties, RaftServerConfigKeys.Read.Option.LINEARIZABLE);
        RaftServerConfigKeys.Read.setTimeout(properties, TimeDuration.ONE_MINUTE);

        BaseStateMachine baseStateMachine = new BaseStateMachine();

        RaftGroup raftGroup = RaftGroup.emptyGroup();

        try {
            this.server = RaftServer.newBuilder()
                    .setGroup(raftGroup)
                    .setProperties(properties)
                    .setStateMachine(baseStateMachine)
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
