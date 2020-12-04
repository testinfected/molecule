package com.vtence.molecule.session;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.DeterministicScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PeriodicSessionHouseKeepingTest {

    static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    CountChores count = new CountChores();
    DeterministicScheduler scheduler = new DeterministicScheduler();

    PeriodicSessionHouseKeeping houseKeeper = new PeriodicSessionHouseKeeping(scheduler, count);

    @Before public void
    start() {
        houseKeeper.start();
    }

    @After public void
    stop() {
        houseKeeper.stop();
    }

    @Test public void
    scheduleHouseKeepingEveryHourByDefault() throws Exception {
        assertHouseKeepingChores(0);
        tick(ONE_HOUR);
        assertHouseKeepingChores(1);
        tick(ONE_HOUR / 2);
        assertHouseKeepingChores(1);
        tick(ONE_HOUR / 2);
        assertHouseKeepingChores(2);
        tick(ONE_HOUR);
        assertHouseKeepingChores(3);
    }

    private void tick(long millis) {
        scheduler.tick(millis, TimeUnit.MILLISECONDS);
    }

    private void assertHouseKeepingChores(int operand) {
        assertThat("housekeeping chores", count.chores, equalTo(operand));
    }

    private static class CountChores implements SessionHouse {
        private int chores;

        public void houseKeeping() {
            chores++;
        }
    }
}