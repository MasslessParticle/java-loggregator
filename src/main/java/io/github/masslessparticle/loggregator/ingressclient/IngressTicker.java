package io.github.masslessparticle.loggregator.ingressclient;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class IngressTicker {
    private Timer timer;
    private Runnable task;
    private long period;

    public void schedule(Runnable task, Duration period) {
        if (this.task != null) {
            throw new IllegalArgumentException("Task already scheduled");
        }

        this.task = task;
        this.period = period.toMillis();

        schedule();
    }

    public synchronized void reset() {
        timer.cancel();
        timer.purge();

        schedule();
    }

    private void schedule() {
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, period, period);
    }
}
