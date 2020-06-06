package org.eclipse.milo.examples.client.demo;

import lombok.Data;

@Data
public class ServerConfiguration {
    EndpointUrl endpointUrl;
    public String get(){
        return endpointUrl.getHost()+""+endpointUrl.getPort()+""+endpointUrl.getUri();
    }
}
