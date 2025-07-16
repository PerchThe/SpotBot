package com.olziedev.spotbot.utils;

import com.olziedev.spotbot.SpotBot;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SpotTimer {
    public static void schedule(Runnable runnable, Date date) {
        SpotBot.getExecutor().schedule(runnable, date.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> schedule(Callback<ScheduledFuture<?>> callback, long delay, long period) {
        AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();

        future.set(schedule(() -> callback.call(future.get()), delay, period));
        return future.get();
    }

    private static ScheduledFuture<?> schedule(Runnable runnable, long delay, long period) {
        return SpotBot.getExecutor().scheduleWithFixedDelay(runnable, delay + 100, period, TimeUnit.MILLISECONDS);
    }
}
