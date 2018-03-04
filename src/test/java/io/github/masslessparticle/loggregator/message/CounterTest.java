package io.github.masslessparticle.loggregator.message;

import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CounterTest {

    @Test
    void addsACounterToAnEnvelope() {
        Counter counter = new Counter("counter-1");

        LoggregatorEnvelope.Envelope e = envelope();
        e = counter.envelopeWithMessage(e);

        assertEquals(e.getCounter().getName(), "counter-1");
        assertEquals(e.getCounter().getDelta(), 1);
    }

    @Test
    void deltaMustBeGreaterThanOrEqualTo0() {
        Counter counter = new Counter("counter-1");
        assertThrows(IllegalArgumentException.class, () -> counter.setDelta(-1));
    }

    @Test
    void setsAdditionalInformation() {
        Counter counter = new Counter("counter-1");
        counter.setAppInfo("app-1", 3);
        counter.setDelta(6);

        LoggregatorEnvelope.Envelope e = envelope();
        e = counter.envelopeWithMessage(e);

        assertEquals(e.getSourceId(), "app-1");
        assertEquals(e.getInstanceId(), "3");
        assertEquals(e.getCounter().getName(), "counter-1");
        assertEquals(e.getCounter().getDelta(), 6);
    }

    private LoggregatorEnvelope.Envelope envelope() {
        LoggregatorEnvelope.Envelope.Builder envelopeBuilder = LoggregatorEnvelope.Envelope.newBuilder();
        return envelopeBuilder.setTimestamp(12345).build();
    }
}