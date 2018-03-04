package io.github.masslessparticle.loggregator.ingressclient;

import com.google.protobuf.ByteString;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogTest {

    @Test
    void addsALogToAnEnvelope() {
        Log log = new Log("Log Message");

        LoggregatorEnvelope.Envelope e = envelope();
        e = log.envelopeWithMessage(e);

        ByteString logLessage = e.getLog().getPayload();
        assertEquals(logLessage.toStringUtf8(), "Log Message");
        assertEquals(e.getLog().getType(), LoggregatorEnvelope.Log.Type.ERR);
    }

    @Test
    void setsAdditionalInformation() {
        Log log = new Log("Log Message");
        log.setAppInfo("app-1", "app", "3");
        log.setStdOut();

        LoggregatorEnvelope.Envelope e = envelope();
        e = log.envelopeWithMessage(e);

        assertEquals(e.getSourceId(), "app-1");
        assertEquals(e.getInstanceId(), "3");
        assertEquals(e.getTagsOrThrow("source_type"), "app");
        assertEquals(e.getLog().getType(), LoggregatorEnvelope.Log.Type.OUT);
    }

    private LoggregatorEnvelope.Envelope envelope() {
        LoggregatorEnvelope.Envelope.Builder envelopeBuilder = LoggregatorEnvelope.Envelope.newBuilder();
        return envelopeBuilder.setTimestamp(12345).build();
    }
}