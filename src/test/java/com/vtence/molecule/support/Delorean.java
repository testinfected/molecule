package com.vtence.molecule.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class Delorean extends Clock {

    private final Clock baseClock;

    private Instant frozenAt;
    private long timeTravel;

    public Delorean() {
        this(Clock.systemDefaultZone());
    }

    public Delorean(Clock base) {
        this.baseClock = base;
    }

    public Delorean withZone(ZoneId zone) {
        Delorean delorean = new Delorean(baseClock.withZone(zone));
        delorean.freezeAt(frozenAt);
        delorean.travelInTime(timeTravel);
        return delorean;
    }

    public ZoneId getZone() {
        return baseClock.getZone();
    }

    public Instant instant() {
        return currentTime().plusMillis(timeTravel);
    }

    public Instant freeze() {
        return freezeAt(instant());
    }

    public Instant freezeAt(Instant pointInTime) {
        frozenAt = pointInTime;
        return frozenAt;
    }

    public void unfreeze() {
        freezeAt(null);
    }

    public void travelInTime(long offsetInMillis) {
        this.timeTravel = offsetInMillis;
    }

    public void back() {
        travelInTime(0);
    }

    private boolean frozen() {
        return frozenAt != null;
    }

    private Instant currentTime() {
        return frozen() ? frozenAt : baseClock.instant();
    }
}