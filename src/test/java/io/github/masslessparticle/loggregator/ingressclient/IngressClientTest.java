package io.github.masslessparticle.loggregator.ingressclient;

import io.github.masslessparticle.loggregator.ingressserver.TestIngressServer;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.masslessparticle.loggregator.Util.waitForResult;
import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class IngressClientTest {
    private static TestIngressServer server;

    private IngressClient client;
    private CompletableFuture<List<Envelope>> callback;

    @BeforeAll
    static void setupAll() {
        server = buildIngressServer();
        server.start();
    }

    @BeforeEach
    void setup() {
        callback = new CompletableFuture<>();
        server.setResultCallback(callback);

        client = buildIngressClient(server.address(), of(10, SECONDS));
    }

    @AfterEach
    void cleanup() {
        client.shutdown();
    }

    @AfterAll
    static void cleanupAll() {
        server.stop();
    }

    @Test
    void clientDoesNotAcceptLessThan0ForMaxBatchSize() {
        assertThrows(IllegalArgumentException.class, () -> client.setBatchMaxSize(-1));
    }

    @Test
    void setsTagsOnEnvelopes() {
        client.setTag("foo", "bar");

        SyncEnvelope env = new SyncEnvelope("sync-envelope");
        client.emit(env);

        waitForResult(callback.thenAccept((received) -> {
            assertEquals(1, received.size());
            assertEquals(received.get(0).getTagsOrThrow("foo"), "bar");
            assertTrue(env.called);
        }));
    }

    @Test
    void sendsSynchronously() {
        SyncEnvelope env = new SyncEnvelope("sync-envelope");

        client.emit(env);

        waitForResult(callback.thenAccept((received) -> {
            assertEquals(1, received.size());
            assertTrue(env.called);
        }));
    }

    @Test
    void sendsBatches() {
        client.setBatchMaxSize(10);

        List<BatchEnvelope> sent = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            BatchEnvelope e = new BatchEnvelope("batch-env");
            client.emit(e);
            sent.add(e);
        }

        waitForResult(callback.thenAccept((received) -> {
            assertEquals(10, received.size());
            assertTrue(sent.stream().allMatch(e -> e.called));
        }));
    }

    @Test
    void sendsBatchesInIntervals() {
        IngressClient client = buildIngressClient(server.address(), of(100, MILLIS));
        List<BatchEnvelope> envelopes = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            BatchEnvelope e = new BatchEnvelope("batch-env");
            client.emit(e);
            envelopes.add(e);
        }

        waitForResult(callback.thenAccept((received) -> {
            assertEquals(3, received.size());
            assertTrue(envelopes.stream().allMatch(e -> e.called));
        }));
        client.shutdown();
    }

    private static TestIngressServer buildIngressServer() {
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

    private IngressClient buildIngressClient(String address, Duration timeout) {
        return new IngressClient(
                address,
                tlsConfig(),
                timeout
        );
    }

    private TlsConfig tlsConfig() {
        return new TlsConfig(
                fixturePath("ca.crt"),
                fixturePath("client.crt"),
                fixturePath("client.pem"));
    }

    private static String fixturePath(String name) {
        File f = new File("src/test/resources/fixtures/" + name);
        assertTrue(f.exists());

        return f.getAbsolutePath();
    }

    private class SyncEnvelope implements Emittable {
        private String sourceId;
        boolean called;

        SyncEnvelope(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public Envelope envelopeWithMessage(Envelope e) {
            called = true;
            return e.toBuilder()
                    .setSourceId(sourceId)
                    .build();
        }

        @Override
        public boolean shouldBatch() {
            return false;
        }
    }

    private class BatchEnvelope implements Emittable {
        private String sourceId;
        boolean called;

        BatchEnvelope(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public Envelope envelopeWithMessage(Envelope e) {
            called = true;
            return e.toBuilder()
                    .setSourceId(sourceId)
                    .build();
        }
    }
}