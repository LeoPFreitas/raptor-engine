package co.raptor.engine.raft.internal;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.StateMachine;

import java.io.IOException;

class RaptorEngineServer implements RaftServerNode {
    private final RaftServer server;

    public RaptorEngineServer(RaftGroup raftGroup, RaftPeer raftPeer, StateMachine stateMachine, RaftProperties properties) {
        try {
            this.server = RaftServer.newBuilder()
                    .setGroup(raftGroup)
                    .setProperties(properties)
                    .setStateMachine(stateMachine)
                    .setServerId(raftPeer.getId())
                    .setOption(RaftStorage.StartupOption.RECOVER)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws IOException {
        server.start();
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    @Override
    public String getId() {
        return server.getId().toString();
    }
}
