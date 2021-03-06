package io.github.masslessparticle.loggregator.message;

import com.google.protobuf.ByteString;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;
import org.junit.jupiter.api.Test;

import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LogTest {

    @Test
    void addsALogToAnEnvelope() {
        Log log = new Log("Log Message");

        Envelope e = envelope();
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

        Envelope e = envelope();
        e = log.envelopeWithMessage(e);

        assertEquals(e.getSourceId(), "app-1");
        assertEquals(e.getInstanceId(), "3");
        assertEquals(e.getTagsOrThrow("source_type"), "app");
        assertEquals(e.getLog().getType(), LoggregatorEnvelope.Log.Type.OUT);
    }

    private Envelope envelope() {
        Envelope.Builder envelopeBuilder = Envelope.newBuilder();
        return envelopeBuilder.setTimestamp(12345).build();
    }
}