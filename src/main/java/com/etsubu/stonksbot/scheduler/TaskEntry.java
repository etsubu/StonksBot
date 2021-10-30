package com.etsubu.stonksbot.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final boolean weekend;

    public TaskEntry(Schedulable task, int delay) {
        this.task = task;
        this.delay = delay;
        this.ticks = 0;
        this.fromHour = -1;
        this.toHour = -1;
        this.active = true;
        this.weekend = true;
    }

    public TaskEntry(Schedulable task, int delay, int fromHour, int toHour, boolean weekend) {
        this.task = task;
        this.delay = delay;
        this.ticks = 0;
        this.fromHour = -1;
        this.toHour = -1;
        this.active = true;
        this.weekend = weekend;
    }

    public boolean tick() {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        if(!weekend) {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if(day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                // No triggers during weekend
                if(active) {
                    log.info("Entering sleep for the weekend for task {}", task.getClass().getName());
                }
                active = false;
                return false;
            }
        }
        if (fromHour != -1 && toHour != -1) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
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
