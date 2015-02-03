package com.vtence.molecule.session;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PeriodicSessionHouseKeeping {

    private static final long EVERY_HOUR = HOURS.toSeconds(1);

    private final ScheduledExecutorService scheduler;
    private final SessionHouse sessions;

    private long choresInterval;
    private ScheduledFuture chores;

    public PeriodicSessionHouseKeeping(ScheduledExecutorService scheduler, SessionHouse sessions) {
        this(scheduler, sessions, EVERY_HOUR, SECONDS);
    }

    public PeriodicSessionHouseKeeping(ScheduledExecutorService scheduler, SessionHouse sessions,
                                       long choresInterval, TimeUnit timeUnit) {
        this.scheduler = scheduler;
        this.sessions = sessions;
        this.choresInterval = MILLISECONDS.convert(choresInterval, timeUnit);
    }

    public void start() {
        chores = scheduler.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                sessions.houseKeeping();
            }
        }, choresInterval, choresInterval, MILLISECONDS);
    }

    public void stop() {
        chores.cancel(false);
    }
}