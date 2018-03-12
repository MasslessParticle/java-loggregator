package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.IngressStub;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.newStub;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.EnvelopeBatch;


public class IngressClient {

    private String address;
    private IngressStub client;

    private Map<String, String> tags;
    private int maxBatchSize;
    private Duration batchFlushInterval;

    public IngressClient(String address, TlsConfig tlsConfig) {
        this.address = address;
        this.tags = new HashMap<>();
        this.maxBatchSize = 100;
        this.batchFlushInterval = Duration.of(100, MILLIS);

        ManagedChannel channel = NettyChannelBuilder.forAddress(host(), port())
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

    public void setBatchFlushInterval(Duration interval) {
        batchFlushInterval = interval;
    }

    public void emit(Emittable e) {
        Envelope envelope = Envelope.newBuilder().build();
        EnvelopeBatch request = EnvelopeBatch.newBuilder()
                .addBatch(e.envelopeWithMessage(envelope))
                .build();

        client.send(request, new SendObserver());
    }
}
