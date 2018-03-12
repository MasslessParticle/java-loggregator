package io.github.masslessparticle.loggregator.ingressclient;

import io.github.masslessparticle.loggregator.ingressserver.TestIngressServer;
import io.github.masslessparticle.loggregator.message.Event;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class IngressClientTest {
    TestIngressServer server;

    @BeforeEach
    void setup() {
         server  = buildIngressServer();
         new Thread(() -> server.start()).start();
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
    void receivesAnEmittedEvent() {
        IngressClient client = buildIngressClient(server.address());
        Event event = new Event("event-1", "event occurred");

        client.emit(event);

        await().atMost(10, SECONDS).until(() -> server.received().size() == 1);

        LoggregatorEnvelope.Event received = server.received().get(0).getEvent();
        assertEquals(received.getTitle(), "event-1");
        assertEquals(received.getBody(), "event occurred");
    }

    private TestIngressServer buildIngressServer() {
        try {
            return new TestIngressServer(
                    fixturePath("server.crt"),
                    fixturePath("server.pem"),
                    fixturePath("ca.crt")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IngressClient buildIngressClient(String address) {
        return new IngressClient(
                address,
                tlsConfig()
        );
    }

    private TlsConfig tlsConfig() {
        return new TlsConfig(
                fixturePath("ca.crt"),
                fixturePath("client.crt"),
                fixturePath("client.pem"));
    }

    private String fixturePath(String name) {
        File f = new File("src/test/resources/fixtures/" + name);
        assertTrue(f.exists());

        return f.getAbsolutePath();
    }
}