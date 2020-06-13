package com.cc1500.read;

import com.cc1500.client.ClientExampleRunner;
import com.cc1500.read.conf.NodesConfiguration;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class StartRunner implements CommandLineRunner {
   public  CompletableFuture<OpcUaClient> isfuture = new CompletableFuture<>();
    @Autowired
    private NodesConfiguration configuration;

    @GetMapping("/run")
    public void run() throws Exception {
        System.out.println("StartRunner :"+configuration);

    }
    @GetMapping("/stop")
    public void stop() throws Exception {
        System.out.println("StartRunner :"+configuration);
        run();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("@au："+configuration);
        Read example = new Read(configuration);

        new ClientExampleRunner(example).run();
    }
}
