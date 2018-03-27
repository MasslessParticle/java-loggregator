package io.github.masslessparticle.loggregator.ingressserver;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.cloudfoundry.loggregator.v2.IngressGrpc;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.getBatchSenderMethod;
import static org.cloudfoundry.loggregator.v2.IngressGrpc.getSenderMethod;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.EnvelopeBatch;
import static org.cloudfoundry.loggregator.v2.LoggregatorIngress.*;

public class TestIngressServer extends IngressGrpc.IngressImplBase {

    private Server server;
    private CompletableFuture<List<Envelope>> callback;

    public TestIngressServer(String serverCert, String serverKey, String caCert) throws IOException {
        server = NettyServerBuilder
                .forAddress(new InetSocketAddress("localhost", 0))
                .sslContext(getSslContextBuilder(serverCert, serverKey, caCert).build())
                .addService(this)
                .build();
    }

    private SslContextBuilder getSslContextBuilder(String serverCert, String serverKey, String caCert) {
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(serverCert),
                new File(serverKey));
            sslClientContextBuilder.trustManager(new File(caCert));
            sslClientContextBuilder.clientAuth(ClientAuth.OPTIONAL);

            return GrpcSslContexts.configure(sslClientContextBuilder,
                SslProvider.OPENSSL);
    }

    public void setResultCallback(CompletableFuture<List<Envelope>> callback) {
        this.callback = callback;
    }

    public String address() {
        return String.format("localhost:%d", server.getPort());
    }

    public void stop() {
        if (server != null) {
            try {
                server.shutdownNow();
                server.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (server != null) {
            try {
                server.start();
                System.out.println("**********************************************");
                System.out.println("Server Listening on port: " + server.getPort());
                System.out.println("**********************************************");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public StreamObserver<Envelope> sender(StreamObserver<IngressResponse> responseObserver) {
        return asyncUnimplementedStreamingCall(getSenderMethod(), responseObserver);
    }

    public StreamObserver<EnvelopeBatch> batchSender(StreamObserver<BatchSenderResponse> responseObserver) {
        return asyncUnimplementedStreamingCall(getBatchSenderMethod(), responseObserver);
    }

    public void send(EnvelopeBatch request, StreamObserver<SendResponse> responseObserver) {
        SendResponse response = SendResponse.newBuilder().build();
        responseObserver.onNext(response);

        if (callback != null) {
            callback.complete(request.getBatchList());
        }

        responseObserver.onCompleted();
    }
}
