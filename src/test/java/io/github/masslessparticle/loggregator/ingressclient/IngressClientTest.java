package io.github.masslessparticle.loggregator.ingressclient;

import io.github.masslessparticle.loggregator.ingressserver.TestIngressServer;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.masslessparticle.loggregator.Util.waitForResult;
import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngressClientTest {
    private static TestIngressServer server;

    @BeforeAll
    static void setup() {
        server = buildIngressServer();
        server.start();
    }

    @AfterAll
    static void cleanup() {
        server.stop();
    }

    @Test
    void clientDoesNotAcceptLessThan0ForMaxBatchSize() {
        IngressClient client = buildIngressClient(server.address(), of(10, SECONDS));
        assertThrows(IllegalArgumentException.class, () -> client.setBatchMaxSize(-1));
    }

    @Test
    void sendsSynchronously() {
        CompletableFuture<List<Envelope>> callback = new CompletableFuture<>();
        server.setResultCallback(callback);

        IngressClient client = buildIngressClient(server.address(), of(10, SECONDS));
        SyncEnvelope env = new SyncEnvelope("sync-envelope");

        client.emit(env);

        waitForResult(callback.thenAccept((received) -> {
            assertEquals(1, received.size());
            assertTrue(env.called);
        }));
    }

    @Test
    void sendsBatches() {
        CompletableFuture<List<Envelope>> callback = new CompletableFuture<>();
        server.setResultCallback(callback);

        IngressClient client = buildIngressClient(server.address(), of(10, SECONDS));
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
        CompletableFuture<List<Envelope>> callback = new CompletableFuture<>();
        server.setResultCallback(callback);

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