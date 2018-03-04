package io.github.masslessparticle.loggregator.ingressclient;

import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

public class Counter implements Emittable {

    private String name;
    private String appId;
    private int index;
    private long delta = 1;

    public Counter(String name) {
        this.name = name;
    }

    @Override
    public LoggregatorEnvelope.Envelope envelopeWithMessage(LoggregatorEnvelope.Envelope e) {
        LoggregatorEnvelope.Envelope.Builder envelopeBuilder = e.toBuilder();
        envelopeBuilder = addAppInfo(envelopeBuilder);
        envelopeBuilder = addCounter(envelopeBuilder);
        return envelopeBuilder.build();
    }

    private LoggregatorEnvelope.Envelope.Builder addAppInfo(LoggregatorEnvelope.Envelope.Builder envelopeBuilder) {
        if (appId == null) {
            return envelopeBuilder;
        }

        return envelopeBuilder
                .setSourceId(appId)
                .setInstanceId(Integer.toString(index));
    }

    private LoggregatorEnvelope.Envelope.Builder addCounter(LoggregatorEnvelope.Envelope.Builder envelopeBuilder) {
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
        this.delta = delta;
    }
}
