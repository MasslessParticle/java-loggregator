package io.github.masslessparticle.loggregator.ingressclient;

import io.github.masslessparticle.loggregator.ingressserver.TestIngressServer;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class IngressClientTest {

    @Test
    void clientCanConnectToAServer() {
        TestIngressServer server = buildIngressServer();
        server.start();

        IngressClient client = buildIngressClient(server.address());

        assertTrue(client.connected());
        server.stop();
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
                fixturePath("CA.crt"),
                fixturePath("client.crt"),
                fixturePath("client.key")
        );
    }

    private String fixturePath(String name) {
        File f = new File("src/test/resources/fixtures/" + name);
        assertTrue(f.exists());

        return f.getAbsolutePath();
    }
}