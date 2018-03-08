package io.github.masslessparticle.loggregator.message;

import io.github.masslessparticle.loggregator.ingressclient.Emittable;
import org.cloudfoundry.loggregator.v2.LoggregatorEnvelope;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static org.cloudfoundry.loggregator.v2.LoggregatorEnvelope.Envelope;

class Log implements Emittable {
    private String message;

    private String appId;
    private String sourceType;
    private String sourceInstance;
    private boolean isStdOut = false;

    public Log(String message) {
        this.message = message;
    }

    public void setAppInfo(String appId, String sourceType, String sourceInstance) {
        this.appId = appId;
        this.sourceType = sourceType;
        this.sourceInstance = sourceInstance;
    }

    public void setStdOut() {
        this.isStdOut = true;
    }

    public Envelope envelopeWithMessage(Envelope e) {
        Envelope.Builder envelopeBuilder = e.toBuilder();
        envelopeBuilder = addAppInfo(envelopeBuilder);
        envelopeBuilder = addLog(envelopeBuilder);
        return envelopeBuilder.build();
    }

    private Envelope.Builder addAppInfo(Envelope.Builder envelopeBuilder) {
        if (appId == null) {
            return envelopeBuilder;
        }

        return envelopeBuilder
                .setSourceId(appId)
                .setInstanceId(sourceInstance)
                .putTags("source_type", sourceType);
    }

    private Envelope.Builder addLog(Envelope.Builder envelopeBuilder) {
        LoggregatorEnvelope.Log log = envelopeBuilder.getLogBuilder()
                .setType(getLogType())
                .setPayload(copyFromUtf8(message)).build();
        return envelopeBuilder.setLog(log);
    }

    private LoggregatorEnvelope.Log.Type getLogType() {
        return isStdOut ? LoggregatorEnvelope.Log.Type.OUT : LoggregatorEnvelope.Log.Type.ERR;
    }
}