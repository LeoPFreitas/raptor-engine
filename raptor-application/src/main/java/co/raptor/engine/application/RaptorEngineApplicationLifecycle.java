package co.raptor.engine.application;

import co.raptor.engine.application.config.RaftServerProperties;
import co.raptor.engine.raft.ClusterConfig;
import co.raptor.engine.raft.RaftPeerConfig;
import co.raptor.engine.raft.RaptorEngineServerFactory;
import co.raptor.engine.raft.RaptorNetUtils;
import co.raptor.engine.raft.internal.RaftServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Component
public class RaptorEngineApplicationLifecycle implements SmartLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(RaptorEngineApplicationLifecycle.class);

    private final RaptorEngineInitializationService initializationService;
    private final RaftServerProperties raftServerProperties;

    private volatile boolean running = false;

    public RaptorEngineApplicationLifecycle(RaptorEngineInitializationService initializationService, RaftServerProperties raftServerProperties) {
        this.initializationService = initializationService;
        this.raftServerProperties = raftServerProperties;
    }

    @Override
    public void start() {
        try {
            initializationService.syncConfiguration();
            initializationService.performHealthChecks();
            initializationService.prepareStorage();
            initializationService.registerToCluster();

            logger.info("==== Initialization Complete ====");

            RaftPeerConfig node1 = new RaftPeerConfig(raftServerProperties.getId(), RaptorNetUtils.createSocketAddress(raftServerProperties.getHost()));

            ClusterConfig clusterConfig = new ClusterConfig(UUID.randomUUID(), List.of(node1));

            RaftServerNode raptorEngineServer = RaptorEngineServerFactory.createNode(node1, clusterConfig);
            raptorEngineServer.start();

            logger.info("RaptorEngineServer started successfully.");

            this.running = true;

        } catch (Exception e) {
            logger.error("Error during RaptorEngineServer startup in application lifecycle.", e);
            throw new RuntimeException("Error during RaptorEngineServer startup in application lifecycle.", e);
        }
    }

    // TODO: Implement proper cleanup and ratis server stop
    @Override
    public void stop() {
        try {
            logger.info("==== Stopping RaptorEngineServer ====");

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