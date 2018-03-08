package io.github.masslessparticle.loggregator.message;

import io.github.masslessparticle.loggregator.ingressclient.Emittable;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.*;

public class Event implements Emittable {

    private String title;
    private String body;

    public Event(String title, String body) {
        this.title = title;
        this.body = body;
    }

    @Override
    public Envelope envelopeWithMessage(Envelope e) {
        Envelope.Builder envelopeBuilder = e.toBuilder();
        LoggregatorEnvelope.Event event = envelopeBuilder.getEventBuilder()
                .setTitle(title)
                .setBody(body).build();
        return envelopeBuilder.setEvent(event).build();
    }
}

