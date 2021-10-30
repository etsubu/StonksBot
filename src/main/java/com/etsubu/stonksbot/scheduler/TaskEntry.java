package com.etsubu.stonksbot.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

public class TaskEntry {
    private static final Logger log = LoggerFactory.getLogger(TaskEntry.class);
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone(ZoneOffset.UTC);
    private final Schedulable task;
    private int ticks;
    private boolean active;
    private final long delay;
    private final int fromHour;
    private final int toHour;

    public TaskEntry(Schedulable task, int delay) {
        this.task = task;
        this.delay = delay;
        this.ticks = 0;
        this.fromHour = -1;
        this.toHour = -1;
        this.active = true;
    }

    public TaskEntry(Schedulable task, int delay, int fromHour, int toHour) {
        this.task = task;
        this.delay = delay;
        this.ticks = 0;
        this.fromHour = -1;
        this.toHour = -1;
        this.active = true;
    }

    public boolean tick() {
        if (fromHour != -1 && toHour != -1) {
            int hour = Calendar.getInstance(TIME_ZONE).get(Calendar.HOUR_OF_DAY);
            if (!(hour >= fromHour || hour <= toHour)) {
                if (active) {
                    log.info("Entering sleep for task {}", task.getClass().getName());
                    active = false;
                }
                // Not within time window. No triggers at this time of day
                return false;
            }
        }
        if (!active) {
            log.info("Woke up from sleep, {}", task.getClass().getName());
            active = true;
        }
        ticks++;
        if (ticks == delay) {
            ticks = 0;
            return true;
        }
        if (ticks == Integer.MAX_VALUE) {
            ticks = 0;
        }
        return false;
    }

    public void call() {
        task.invoke();
    }

    public Schedulable getTask() {
        return task;
    }
}
