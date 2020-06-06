package com.cc1500.read.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@PropertySource("classpath:application.properties")
@Configuration
@ConfigurationProperties(prefix = "clients")
@Data
public class NodesConfiguration {
    private String ddd;
    private ServerConfiguration server;
    private List<String> nodelist;

}
