package org.eclipse.milo.examples.client.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@PropertySource("classpath:config.properties")
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "client")
@Data
public class Configuration {

    private String ddd;
    private ServerConfiguration server;
    private List<String> nodelist;

}
