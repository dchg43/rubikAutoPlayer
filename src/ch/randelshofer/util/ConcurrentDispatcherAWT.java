package ch.randelshofer.util;

import java.util.Vector;

public class ConcurrentDispatcherAWT implements Runnable {
    private int priority;

    private final Vector<Runnable> queue;

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
        this.queue = new Vector<>();
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
            }
            // else ==ENQUEUE_WHEN_BLOCKED时直接丢弃runnable，既不显示这个动画
            return;
        }
        synchronized (this.queue) {
            this.queue.addElement(runnable);
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
