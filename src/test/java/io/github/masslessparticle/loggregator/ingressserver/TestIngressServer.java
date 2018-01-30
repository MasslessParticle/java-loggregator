package io.github.masslessparticle.loggregator.ingressserver;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.*;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.EnvelopeBatch;
import static org.cloudfoundry.loggregator.v2.LoggregatorIngress.*;

public class TestIngressServer extends IngressImplBase {

    private Server server;

    public TestIngressServer(String serverCert, String serverKey, String caCert) throws SSLException {
        server = NettyServerBuilder
                .forAddress(new InetSocketAddress("localhost", 0))
                .sslContext(getSslContext(serverCert, serverKey, caCert))
                .addService(this)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(TestIngressServer.this::stop));
    }

    private SslContext getSslContext(String serverCert, String serverKey, String caCert) throws SSLException {
        SslContextBuilder sslClientContextBuilder = SslContextBuilder
                .forServer(new File(serverCert), new File(serverKey))
                .trustManager(new File(caCert))
                .clientAuth(ClientAuth.OPTIONAL);

        return GrpcSslContexts.configure(sslClientContextBuilder,
                SslProvider.OPENSSL).build();
    }

    public String address() {
        return String.format("localhost:%d", server.getPort());
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void start() throws IOException {
        server.start();
    }

    public StreamObserver<Envelope> sender(StreamObserver<IngressResponse> responseObserver) {
        return asyncUnimplementedStreamingCall(getSenderMethod(), responseObserver);
    }

    /**
     */
    public StreamObserver<EnvelopeBatch> batchSender(StreamObserver<BatchSenderResponse> responseObserver) {
        return asyncUnimplementedStreamingCall(getBatchSenderMethod(), responseObserver);
    }

    /**
     */
    public void send(EnvelopeBatch request, StreamObserver<SendResponse> responseObserver) {
        asyncUnimplementedUnaryCall(getSendMethod(), responseObserver);
    }
}
