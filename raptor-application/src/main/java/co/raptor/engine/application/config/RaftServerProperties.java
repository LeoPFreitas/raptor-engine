package co.raptor.engine.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "raptor.engine.raft.server")
public class RaftServerProperties {

    private String host;

    private String id;
}
