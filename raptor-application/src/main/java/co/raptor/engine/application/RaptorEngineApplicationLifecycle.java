package co.raptor.engine.application;

import co.raptor.engine.raft.RaptorEngineServer;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class RaptorEngineApplicationLifecycle implements SmartLifecycle {


    private final RaptorEngineInitializationService initializationService;
    private boolean running = false;

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

            System.out.println("==== Initialization Complete ====");


            RaptorEngineServer raptorEngineServer = new RaptorEngineServer();
            raptorEngineServer.start();

            this.running = true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
