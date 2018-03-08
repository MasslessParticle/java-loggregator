package io.github.masslessparticle.loggregator.ingressclient;

import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;

public interface Emittable {
    Envelope envelopeWithMessage(Envelope e);
}
