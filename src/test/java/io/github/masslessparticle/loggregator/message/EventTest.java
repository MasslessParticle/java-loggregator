package io.github.masslessparticle.loggregator.message;

import org.junit.jupiter.api.Test;

import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EventTest {

    @Test
    void addsAnEventToAnEnvelope() {
        Event event = new Event("event-1", "body");

        Envelope e = envelope();
        e = event.envelopeWithMessage(e);

        assertEquals(e.getEvent().getTitle(), "event-1");
        assertEquals(e.getEvent().getBody(), "body");
    }

    @Test
    void eventsAreNotBatchable() {
        Event event = new Event("event-1", "body");

        assertFalse(event.shouldBatch());
    }

    private Envelope envelope() {
        Envelope.Builder envelopeBuilder = Envelope.newBuilder();
        return envelopeBuilder.setTimestamp(12345).build();
    }
}