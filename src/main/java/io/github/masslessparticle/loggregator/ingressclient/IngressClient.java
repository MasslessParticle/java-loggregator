package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

import static org.cloudfoundry.loggregator.v2.IngressGrpc.IngressStub;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.newStub;


public class IngressClient {

    private String address;
    private IngressStub asyncStub;


    public IngressClient(String address, String caCert, String clientCert, String clientKey) {
        this.address = address;

        SslContext sslContext = createSSLContext(caCert, clientCert, clientKey);

        ManagedChannel channel = NettyChannelBuilder.forAddress(host(), port())
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext)
                .build();

        asyncStub = newStub(channel);
    }

    private SslContext createSSLContext(String caCert, String clientCert, String clientKey) {
        SslContextBuilder builder = GrpcSslContexts.forClient();

        builder.trustManager(new File(caCert));
        builder.keyManager(new File(clientCert), new File(clientKey));

        try {
            return builder.build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public String host() {
        return address.split(":")[0];
    }

    public int port() {
        return Integer.valueOf(address.split(":")[1]);
    }

    public Boolean connected() {
        return asyncStub != null;
    }
}
