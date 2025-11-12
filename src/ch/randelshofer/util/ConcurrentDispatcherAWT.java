package ch.randelshofer.util;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentDispatcherAWT extends Thread {

    private final List<Runnable> queue;

    private int threadMax;

    // 待执行动画太多时丢弃动画。效果是界面流畅不卡顿，但是会多消耗CPU
    public static final int ENQUEUE_WHEN_BLOCKED = 0;

    // 待执行动画太多时以同步阻塞方式执行动画。频繁点击前进后退时（1秒很多次）界面会有卡顿
    public static final int RUN_WHEN_BLOCKED = 1;

    private int blockingPolicy;

    public ConcurrentDispatcherAWT() {
        this(5, 5);
    }

    public ConcurrentDispatcherAWT(int priority, int threadMax) {
        this.queue = new ArrayList<>();
        this.blockingPolicy = ENQUEUE_WHEN_BLOCKED;
        this.threadMax = threadMax;
        setPriority(priority);
        start();
    }

    public void setMaxThreads(int threadMax) {
        this.threadMax = threadMax;
    }

    public int getThreadCount() {
        return this.queue.size();
    }

    public void dispatch(Runnable runnable) {
        if (this.queue.size() >= this.threadMax) {
            if (this.blockingPolicy == RUN_WHEN_BLOCKED) {
                runnable.run();
            } else { // else ==ENQUEUE_WHEN_BLOCKED时跳过这个动画显示
                this.queue.add(runnable);
            }
            return;
        }
        synchronized (this) {
            this.queue.add(runnable);
            notify(); // Interrupted wait()
        }
    }

    @Override
    public void run() {
        try {
            Runnable objElementAt = null;
            while (true) {
                synchronized (this) {
                    if (this.queue.isEmpty()) {
                        wait();
                        continue;
                    } else {
                        objElementAt = this.queue.remove(0);
                    }
                }
                objElementAt.run();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
