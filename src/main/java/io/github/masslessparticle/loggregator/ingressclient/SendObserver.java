package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.stub.StreamObserver;
import org.cloudfoundry.loggregator.v2.LoggregatorIngress;

import static org.cloudfoundry.loggregator.v2.LoggregatorIngress.*;

public class SendObserver implements StreamObserver<SendResponse> {
    @Override
    public void onNext(SendResponse value) {}

    @Override
    public void onError(Throwable t) {
        throw new RuntimeException(t);
    }

    @Override
    public void onCompleted() {}
}
