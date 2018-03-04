package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.*;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.IngressStub;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.newStub;


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

    public Boolean connected() {
        return client != null;
    }

    public void setTag(String name, String value) {
        tags.put("name", value);
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

    }
}
