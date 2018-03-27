package io.github.masslessparticle.loggregator.ingressclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static io.github.masslessparticle.loggregator.Util.waitForResult;
import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class IngressTickerTest {

    @Test
    void schedulesALambda() {
        IngressTicker ticker = new IngressTicker();
        CompletableFuture<Boolean> called = new CompletableFuture<>();

        ticker.schedule(() -> called.complete(true), of(100, MILLIS));

        waitForResult(called.thenAccept(Assertions::assertTrue), of(200, ChronoUnit.MILLIS));
    }

    @Test
    void onlySchedulesOneThing() {
        IngressTicker ticker = new IngressTicker();
        ticker.schedule(() -> {}, of(100, MILLIS));

        assertThrows(IllegalArgumentException.class, () -> ticker.schedule(() -> {}, of(100, MILLIS)));
    }

    @Test
    void canBeReset() {
        IngressTicker ticker = new IngressTicker();

        long startTime = System.currentTimeMillis();
        CompletableFuture<Long> endTimer = new CompletableFuture<>();
        ticker.schedule(() -> endTimer.complete(System.currentTimeMillis()), of(100, MILLIS));

        sleep(50);
        ticker.reset();

        long endTime = waitForResult(endTimer);
        long elapsedTime = endTime - startTime;

        assertTrue(elapsedTime > 150);
    }

    private void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}