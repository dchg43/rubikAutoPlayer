package ch.randelshofer.util;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentDispatcherAWT implements Runnable {
    private int priority;

    private final List<Runnable> queue;

    private int threadCount;

    private int threadMax;

    // 待执行动画太多时丢弃动画。效果是界面流畅不卡顿，但是会多消耗CPU
    public static int ENQUEUE_WHEN_BLOCKED = 0;

    // 待执行动画太多时以同步阻塞方式执行动画。频繁点击前进后退时（1秒很多次）界面会有卡顿
    public static int RUN_WHEN_BLOCKED = 1;

    private int blockingPolicy;

    public ConcurrentDispatcherAWT() {
        this(5, 5);
    }

    public ConcurrentDispatcherAWT(int priority, int threadMax) {
        this.queue = new ArrayList<>();
        this.blockingPolicy = ENQUEUE_WHEN_BLOCKED;
        this.priority = priority;
        this.threadMax = threadMax;
    }

    public void setMaxThreads(int threadMax) {
        this.threadMax = threadMax;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void dispatch(Runnable runnable) {
        if (this.threadCount >= this.threadMax) {
            if (this.blockingPolicy == RUN_WHEN_BLOCKED) {
                runnable.run();
            } else { // else ==ENQUEUE_WHEN_BLOCKED时跳过这个动画显示
                this.queue.add(runnable);
            }
            return;
        }
        synchronized (this.queue) {
            this.queue.add(runnable);
            this.threadCount++;
        }

        Thread thread = new Thread(this, this + " Processor");
        try {
            thread.setDaemon(false);
        } catch (SecurityException e) {
        }
        try {
            thread.setPriority(this.priority);
        } catch (SecurityException e2) {
        }
        thread.start();
    }

    @Override
    public void run() {
        Runnable objElementAt;
        while (true) {
            synchronized (this.queue) {
                if (this.queue.isEmpty()) {
                    this.threadCount--;
                    return;
                } else {
                    objElementAt = this.queue.remove(0);
                }
            }
            try {
                objElementAt.run();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
