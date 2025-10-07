package ch.randelshofer.util;

import java.util.Vector;

public class ConcurrentDispatcherAWT implements Runnable {
    private int priority;

    private final Vector<Runnable> queue;

    private int threadCount;

    private int threadMax;

    public static int ENQUEUE_WHEN_BLOCKED = 0;

    public static int RUN_WHEN_BLOCKED = 1;

    private int blockingPolicy;

    public ConcurrentDispatcherAWT() {
        this(5, 5);
    }

    public ConcurrentDispatcherAWT(int i, int i2) {
        this.queue = new Vector<>();
        this.blockingPolicy = ENQUEUE_WHEN_BLOCKED;
        this.priority = i;
        this.threadMax = i2;
    }

    public void setMaxThreads(int i) {
        this.threadMax = i;
    }

    public void dispatch(Runnable runnable) {
        synchronized (this.queue) {
            if (this.threadCount >= this.threadMax) {
                if (this.blockingPolicy == ENQUEUE_WHEN_BLOCKED) {
                    this.queue.addElement(runnable);
                    return;
                } else {
                    runnable.run();
                    return;
                }
            }
            this.queue.addElement(runnable);
            Thread thread = new Thread(this, new StringBuffer().append(this).append(" Processor").toString());
            this.threadCount++;
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
                    objElementAt = this.queue.elementAt(0);
                    this.queue.removeElementAt(0);
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
