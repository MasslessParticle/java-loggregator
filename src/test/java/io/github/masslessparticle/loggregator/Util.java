package io.github.masslessparticle.loggregator;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static java.time.Duration.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class Util {
    public static <T> T waitForResult(CompletableFuture<T> future, Duration timeout) {
        try {
            return future.get(timeout.toMillis(), MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T waitForResult(CompletableFuture<T> future) {
        return waitForResult(future, of(3, ChronoUnit.SECONDS));
    }

    public static <T> T waitForever(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
