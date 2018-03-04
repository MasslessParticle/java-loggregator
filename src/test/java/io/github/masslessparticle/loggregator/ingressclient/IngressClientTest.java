package io.github.masslessparticle.loggregator.ingressclient;

import io.github.masslessparticle.loggregator.ingressserver.TestIngressServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngressClientTest {
    TestIngressServer server;

    @BeforeEach
    void setup() {
         server  = buildIngressServer();
         server.start();
    }

    @AfterEach
    void cleanup() {
        server.stop();
    }

    @Test
    void clientDoesNotAcceptLessThan0ForMaxBatchSize() {
        IngressClient client = buildIngressClient(server.address());
        assertThrows(IllegalArgumentException.class, () -> client.setBatchMaxSize(-1));
    }

    @Test
    void clientCanConnectToAServer() {
        IngressClient client = buildIngressClient(server.address());
        assertTrue(client.connected());
    }

    private TestIngressServer buildIngressServer() {
        return new TestIngressServer(
                fixturePath("server.crt"),
                fixturePath("server.key"),
                fixturePath("CA.crt")
        );
    }

    private IngressClient buildIngressClient(String address) {
        return new IngressClient(
                address,
                tlsConfig()
        );
    }

    private TlsConfig tlsConfig() {
        return new TlsConfig(
                fixturePath("CA.crt"),
                fixturePath("client.crt"),
                fixturePath("client.key"));
    }

    private String fixturePath(String name) {
        File f = new File("src/test/resources/fixtures/" + name);
        assertTrue(f.exists());

        return f.getAbsolutePath();
    }
}