package io.github.masslessparticle.loggregator.message;

import io.github.masslessparticle.loggregator.ingressclient.Emittable;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;

public class Counter implements Emittable {

    private String name;
    private String appId;
    private int index;
    private long delta = 1;

    public Counter(String name) {
        this.name = name;
    }

    @Override
    public Envelope envelopeWithMessage(Envelope e) {
        Envelope.Builder envelopeBuilder = e.toBuilder();
        envelopeBuilder = addAppInfo(envelopeBuilder);
        envelopeBuilder = addCounter(envelopeBuilder);
        return envelopeBuilder.build();
    }

    private Envelope.Builder addAppInfo(Envelope.Builder envelopeBuilder) {
        if (appId == null) {
            return envelopeBuilder;
        }

        return envelopeBuilder
                .setSourceId(appId)
                .setInstanceId(Integer.toString(index));
    }

    private Envelope.Builder addCounter(Envelope.Builder envelopeBuilder) {
        LoggregatorEnvelope.Counter counter = envelopeBuilder.getCounterBuilder()
                .setName(name)
                .setDelta(getDelta())
                .build();

        return envelopeBuilder.setCounter(counter);
    }
    private long getDelta() {
        return delta;
    }

    public void setAppInfo(String appId, int index) {
        this.appId = appId;
        this.index = index;
    }

    public void setDelta(long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be >= 0");
        }

        this.delta = delta;
    }
}
