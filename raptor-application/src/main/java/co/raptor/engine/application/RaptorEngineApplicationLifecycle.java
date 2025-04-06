package co.raptor.engine.application;

import co.raptor.engine.application.config.RaftServerProperties;
import co.raptor.engine.raft.RaftPeerConfig;
import co.raptor.engine.raft.RaptorNetUtils;
import co.raptor.engine.raft.api.RaftClusterNode;
import co.raptor.engine.raft.internal.RaftClusterNodeFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;


@Component
public class RaptorEngineApplicationLifecycle implements SmartLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(RaptorEngineApplicationLifecycle.class);

    private final RaftClusterNode standaloneNode;

    private volatile boolean running = false;

    public RaptorEngineApplicationLifecycle(RaptorEngineInitializationService initializationService,
                                            RaftServerProperties raftServerProperties) {

        initializationService.syncConfiguration();
        initializationService.performHealthChecks();
        initializationService.prepareStorage();
        initializationService.registerToCluster();

        RaftClusterNodeFactoryImpl raftClusterNodeFactory = new RaftClusterNodeFactoryImpl();

        String peerID = raftServerProperties.getPeerId();
        InetSocketAddress socketAddress = RaptorNetUtils.createSocketAddress(raftServerProperties.getHost());
        RaftPeerConfig raftPeerConfig = new RaftPeerConfig(peerID, socketAddress);

        try {
            standaloneNode = raftClusterNodeFactory.createStandaloneNode(raftPeerConfig);
        } catch (IOException e) {
            logger.error("Error creating Raft server node.", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void start() {
        try {
            standaloneNode.start();
            logger.info("RaptorEngineServer started successfully.");

            this.running = true;
        } catch (Exception e) {
            logger.error("Error during RaptorEngineServer startup in application lifecycle.", e);
            throw new RuntimeException("Error during RaptorEngineServer startup in application lifecycle.", e);
        }
    }

    @Override
    public void stop() {
        try {
            logger.info("==== Stopping RaptorEngineServer ====");
            standaloneNode.close();
            this.running = false;
        } catch (Exception e) {
            logger.error("Error during lifecycle stop of RaptorEngineApplication: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}