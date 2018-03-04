package io.github.masslessparticle.loggregator.message;

import io.github.masslessparticle.loggregator.ingressclient.Emittable;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

import java.util.HashMap;
import java.util.Map;

import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.*;

public class Gauge implements Emittable {

    private Map<String, GaugeValue> metrics;
    private String appId;
    private int index;

    public Gauge(){
        metrics = new HashMap<>();
    }

    @Override
    public Envelope envelopeWithMessage(Envelope e) {
        Envelope.Builder envelopeBuilder = e.toBuilder();
        envelopeBuilder = addAppInfo(envelopeBuilder);
        envelopeBuilder = addGauge(envelopeBuilder);
        return envelopeBuilder.build();
    }

    private Envelope.Builder addGauge(Envelope.Builder envelopeBuilder) {
        LoggregatorEnvelope.Gauge.Builder gaugeBuilder = envelopeBuilder.getGaugeBuilder();
        gaugeBuilder = gaugeBuilder.putAllMetrics(metrics);
        return envelopeBuilder.setGauge(gaugeBuilder.build());
    }

    private Envelope.Builder addAppInfo(Envelope.Builder envelopeBuilder) {
        if (appId == null) {
            return envelopeBuilder;
        }

        return envelopeBuilder
                .setSourceId(appId)
                .setInstanceId(Integer.toString(index));
    }

    public void setAppInfo(String appId, int index) {
        this.appId = appId;
        this.index = index;
    }

    public void setValue(String name, double value, String unit) {
        GaugeValue gaugeValue = GaugeValue.newBuilder()
                .setValue(value)
                .setUnit(unit).build();

        metrics.put(name, gaugeValue);
    }
}
