/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.examples.client.client;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.examples.client.demo.Configuration;
import org.eclipse.milo.examples.server.ExampleServer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class ClientExampleRunner {
    @Autowired
    private Configuration configuration;
    static {
        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

    private ExampleServer exampleServer;

    private final ClientExample clientExample;
    private final boolean serverRequired;

    public ClientExampleRunner(ClientExample clientExample) throws Exception {
        this(clientExample, false);
    }

    public ClientExampleRunner(ClientExample clientExample, boolean serverRequired) throws Exception {
        this.clientExample = clientExample;
        this.serverRequired = serverRequired;

        if (serverRequired) {
            exampleServer = new ExampleServer();
            exampleServer.startup().get();
        }
    }

    private OpcUaClient createClient() throws Exception {
        Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
        Files.createDirectories(securityTempDir);
        if (!Files.exists(securityTempDir)) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }

        LoggerFactory.getLogger(getClass())
            .info("security temp dir: {}", securityTempDir.toAbsolutePath());

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        return OpcUaClient.create(
            clientExample.getEndpointUrl(),
              //  configuration.getServer().get() ,
            endpoints ->
                endpoints.stream()
                    .filter(clientExample.endpointFilter())
                    .findFirst(),
            configBuilder ->
                configBuilder//SimulationServer
                    .setApplicationName(LocalizedText.english("Simulation"))
                    .setApplicationUri("urn:zhangs:53530/OPCUA/SimulationServer")
                    .setCertificate(loader.getClientCertificate())
                    .setKeyPair(loader.getClientKeyPair())
                    .setIdentityProvider(clientExample.getIdentityProvider())
                    .setRequestTimeout(uint(5000))
                    .build()
        );
    }

    public void run() {
        try {
            OpcUaClient client = createClient();
            //
            future.whenCompleteAsync((c, ex) -> {
                if (ex != null) {
                    logger.error("Error running example: {}", ex.getMessage(), ex);
                }

                try {
                    client.disconnect().get();
                    if (serverRequired && exampleServer != null) {
                        exampleServer.shutdown().get();
                    }
                    Stack.releaseSharedResources();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error disconnecting: {}", e.getMessage(), e);
                }

                try {
                    Thread.sleep(1000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            try {
                clientExample.run(client, future);
                //future.get(15, TimeUnit.SECONDS);
            } catch (Throwable t) {
                logger.error("Error running client example: {}", t.getMessage(), t);
                future.completeExceptionally(t);
            }
        } catch (Throwable t) {
            logger.error("Error getting client: {}", t.getMessage(), t);

            future.completeExceptionally(t);

            try {
                Thread.sleep(1000);
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}