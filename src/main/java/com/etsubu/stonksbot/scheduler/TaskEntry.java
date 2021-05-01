package com.etsubu.stonksbot.scheduler;

public class TaskEntry {
    private final Schedulable task;
    private int ticks;
    private final int delay;

    public TaskEntry(Schedulable task, int delay) {
        this.task = task;
        this.delay = delay;
        this.ticks = 0;
    }

    public boolean tick() {
        ticks++;
        if(ticks == delay) {
            ticks = 0;
            return true;
        }
        return false;
    }

    public void call() {
        task.invoke();
    }

    public Schedulable getTask() { return task; }
}
