package io.github.masslessparticle.loggregator.message;

import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GaugeTest {
    @Test
    void addsAGaugeToAnEnvelope() {
        Gauge gauge = new Gauge();

        LoggregatorEnvelope.Envelope e = envelope();
        e = gauge.envelopeWithMessage(e);

        assertTrue(e.hasGauge());
    }

    @Test
    void setsAppInfo() {
        Gauge gauge = new Gauge();
        gauge.setAppInfo("app-1", 3);

        LoggregatorEnvelope.Envelope e = envelope();
        e = gauge.envelopeWithMessage(e);

        assertEquals(e.getSourceId(), "app-1");
        assertEquals(e.getInstanceId(), "3");
    }

    @Test
    void setsMetrics() {
        Gauge gauge = new Gauge();
        gauge.setValue("metric-1", 6.85, "a");
        gauge.setValue("metric-2", 7.85, "b");
        gauge.setValue("metric-3", 8.85, "c");

        LoggregatorEnvelope.Envelope e = envelope();
        e = gauge.envelopeWithMessage(e);
        Map<String, LoggregatorEnvelope.GaugeValue> metrics = e.getGauge().getMetricsMap();

        assertEquals(metrics.get("metric-1").getValue(), 6.85);
        assertEquals(metrics.get("metric-1").getUnit(), "a");

        assertEquals(metrics.get("metric-2").getValue(), 7.85);
        assertEquals(metrics.get("metric-2").getUnit(), "b");

        assertEquals(metrics.get("metric-3").getValue(), 8.85);
        assertEquals(metrics.get("metric-3").getUnit(), "c");
    }

    private LoggregatorEnvelope.Envelope envelope() {
        LoggregatorEnvelope.Envelope.Builder envelopeBuilder = LoggregatorEnvelope.Envelope.newBuilder();
        return envelopeBuilder.setTimestamp(12345).build();
    }
}