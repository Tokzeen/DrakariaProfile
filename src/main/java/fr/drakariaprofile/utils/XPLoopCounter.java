package fr.drakariaprofile.utils;

import java.util.concurrent.*;

public class XPLoopCounter {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Future<?> currentTask;
    private final Runnable resetCallback;

    public XPLoopCounter(Runnable resetCallback, int i) {
        this.resetCallback = resetCallback;
    }

    public synchronized void startOrReset() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
        }
        currentTask = scheduler.schedule(() -> {
            resetCallback.run();
        }, 2500, TimeUnit.MILLISECONDS); // 2.5 secondes
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}