package co.raptor.engine.application;

import co.raptor.engine.raft.RaptorEngineServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;


@Component
public class RaptorEngineApplicationLifecycle implements SmartLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(RaptorEngineApplicationLifecycle.class);

    private final RaptorEngineInitializationService initializationService;
    private volatile boolean running = false;
    private RaptorEngineServer raptorEngineServer;

    public RaptorEngineApplicationLifecycle(RaptorEngineInitializationService initializationService) {
        this.initializationService = initializationService;
    }

    @Override
    public void start() {
        try {
            initializationService.syncConfiguration();
            initializationService.performHealthChecks();
            initializationService.prepareStorage();
            initializationService.registerToCluster();

            logger.info("==== Initialization Complete ====");


            raptorEngineServer = new RaptorEngineServer();
            raptorEngineServer.start();

            logger.info("RaptorEngineServer started successfully.");


            // Mark this lifecycle component as running
            this.running = true;

        } catch (Exception e) {
            logger.error("Error during RaptorEngineServer startup in application lifecycle.", e);
            throw new RuntimeException("Error during RaptorEngineServer startup in application lifecycle.", e);
        }
    }

    @Override
    public void stop() {
        try {
            // Stop the RaptorEngineServer gracefully here
            if (raptorEngineServer != null) {
                logger.info("Stopping RaptorEngineServer...");
                raptorEngineServer.close(); // Ensure RaptorEngineServer has a stop method for clean shutdown
            }

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