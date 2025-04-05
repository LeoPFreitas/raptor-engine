package co.raptor.engine.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RaptorEngineApplication {

    public static void main(String[] args) {
        System.out.println("Starting Raptor Engine Application...");
        SpringApplication.run(RaptorEngineApplication.class, args);
    }
}
