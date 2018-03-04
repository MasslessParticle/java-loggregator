package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class TlsConfig {

    private final String caCert;
    private final String cert;
    private final String key;

    public TlsConfig(String caCert, String cert, String key) {
        this.caCert = caCert;
        this.cert = cert;
        this.key = key;
    }

    public SslContext sslContext() {
        SslContextBuilder builder = GrpcSslContexts.forClient();

        builder.trustManager(new File(caCert));
        builder.keyManager(new File(cert), new File(key));

        try {
            return builder.build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }
}
