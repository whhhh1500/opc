package com.cc1500.read.conf;

import lombok.Data;

@Data
public class ServerConfiguration {
   private EndpointUrl endpointUrl;

   public String get(){
        return endpointUrl.getHost()+""+endpointUrl.getPort()+""+endpointUrl.getUri();
    }
}
