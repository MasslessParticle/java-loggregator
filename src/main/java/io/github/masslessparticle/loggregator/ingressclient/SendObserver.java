package io.github.masslessparticle.loggregator.ingressclient;

import io.grpc.stub.StreamObserver;

import static io.grpc.Status.Code;
import static io.grpc.Status.fromThrowable;
import static org.cloudfoundry.loggregator.v2.LoggregatorIngress.SendResponse;

public class SendObserver implements StreamObserver<SendResponse> {
    @Override
    public void onNext(SendResponse value) {}

    @Override
    public void onError(Throwable t) {
        if (fromThrowable(t).getCode() != Code.UNAVAILABLE) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onCompleted() {}
}
