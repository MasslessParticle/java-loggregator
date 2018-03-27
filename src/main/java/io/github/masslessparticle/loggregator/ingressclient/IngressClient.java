package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.*;
import static java.util.concurrent.TimeUnit.*;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.IngressStub;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.newStub;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.EnvelopeBatch;


public class IngressClient {

    private String address;
    private IngressStub client;
    private ManagedChannel channel;

    private Map<String, String> tags;
    private int maxBatchSize;

    private List<Envelope> envelopes;
    private IngressTicker ticker = new IngressTicker();

    public IngressClient(String address, TlsConfig tlsConfig) {
        this(address, tlsConfig, Duration.of(100, MILLIS));
    }

    public IngressClient(String address, TlsConfig tlsConfig, Duration interval) {
        this.address = address;
        this.tags = new HashMap<>();
        this.maxBatchSize = 100;
        this.envelopes = Collections.synchronizedList(new ArrayList<>());

        this.ticker.schedule(this::flushEnvelopes, interval);

        channel = NettyChannelBuilder.forAddress(host(), port())
                .negotiationType(NegotiationType.TLS)
                .sslContext(tlsConfig.sslContext())
                .build();

        client = newStub(channel);
    }

    public String host() {
        return address.split(":")[0];
    }

    public int port() {
        return Integer.valueOf(address.split(":")[1]);
    }

    public void setTag(String name, String value) {
        tags.put(name, value);
    }

    public void setBatchMaxSize(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Batch size must be >= 0");
        }

        maxBatchSize = maxSize;
    }

    public void emit(Emittable e) {
        Envelope envelope = e.envelopeWithMessage(Envelope.newBuilder().build());

        if (e.shouldBatch()) {
            sendBatch(envelope);
        } else {
            sendOne(envelope);
        }
    }

    public void shutdown() {
        channel.shutdown();

        try {
            channel.awaitTermination(10, SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendBatch(Envelope envelope) {
        envelopes.add(envelope);
        if (envelopes.size() >= maxBatchSize) {
            flushEnvelopes();
            ticker.reset();
        }
    }

    private void flushEnvelopes() {
        EnvelopeBatch request = EnvelopeBatch.newBuilder()
                .addAllBatch(envelopes)
                .build();

        envelopes.clear();

        client.send(request, new SendObserver());

    }

    private void sendOne(Envelope envelope) {
        EnvelopeBatch request = EnvelopeBatch.newBuilder()
                .addBatch(envelope)
                .build();

        client.send(request, new SendObserver());
    }
}
