package io.github.masslessparticle.loggregator.ingressclient;

import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

public interface Emittable {
    LoggregatorEnvelope.Envelope envelopeWithMessage(LoggregatorEnvelope.Envelope e);
}
