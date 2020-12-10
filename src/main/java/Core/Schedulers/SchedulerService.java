package Core.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SchedulerService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);
    private final List<TaskEntry> tasks;
    private final ExecutorService executorService;

    public SchedulerService() {
        tasks = new LinkedList<>();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        log.info("Scheduler service started with {} threads in pool", Runtime.getRuntime().availableProcessors());
        new Thread(this).start();
    }

    public static void sleep(int seconds) {
        long until = System.currentTimeMillis() + (seconds * 1000L);
        long current;
        while((current = System.currentTimeMillis()) < until) {
            try {
                Thread.sleep(until - current);
            } catch (InterruptedException e) {
                log.error("Sleep was interrupted", e);
            }
        }
    }

    public void registerTask(Schedulable task, int delay) {
        synchronized (tasks) {
            tasks.add(new TaskEntry(task, delay));
        }
        log.info("Registered new task");
    }

    public void deregisterTask(Schedulable task) {
        synchronized (tasks) {
            tasks.removeIf(x -> x.getTask().equals(task));
            log.info("Deregistered scheduled task");
        }
    }

    @Override
    public void run() {
        while(true) {
            sleep(1);
            synchronized (tasks) {
                tasks.stream().filter(TaskEntry::tick).forEach(x -> executorService.submit(x::call));
            }
        }
    }
}
