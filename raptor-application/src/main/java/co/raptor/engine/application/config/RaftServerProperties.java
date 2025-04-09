package co.raptor.engine.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;


@Data
@Configuration
@ConfigurationProperties(prefix = "raptor.engine.raft.server")
public class RaftServerProperties {

    private String host;

    private String peerId;

    private UUID groupId;

    private Integer port;
}
