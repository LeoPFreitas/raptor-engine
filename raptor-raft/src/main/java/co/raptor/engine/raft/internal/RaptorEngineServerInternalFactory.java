package co.raptor.engine.raft.internal;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.statemachine.StateMachine;

public class RaptorEngineServerInternalFactory {
    public static RaftServerNode createServer(RaftGroup raftGroup, RaftPeer raftPeer, StateMachine stateMachine, RaftProperties properties) {
        return new RaptorEngineServer(raftGroup, raftPeer, stateMachine, properties);
    }
}

