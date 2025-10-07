package ch.randelshofer.util;

import java.util.Vector;

public class PooledSequentialDispatcherAWT implements Runnable {
    private static ConcurrentDispatcherAWT threadPool = new ConcurrentDispatcherAWT();

    private static final int STOPPED = 0;

    private static final int STARTING = 1;

    private static final int RUNNING = 2;

    private static final int STOPPING = 3;

    private volatile int state = STOPPED;

    private final Vector<Runnable> queue = new Vector<>();

    public static void dispatchConcurrently(Runnable runnable) {
        threadPool.dispatch(runnable);
    }

    public void dispatch(Runnable runnable) {
        dispatch(runnable, threadPool);
    }

    public void dispatch(Runnable runnable, ConcurrentDispatcherAWT concurrentDispatcherAWT) {
        synchronized (this.queue) {
            this.queue.addElement(runnable);
            if (this.state == STOPPED) {
                this.state = STARTING;
                concurrentDispatcherAWT.dispatch(this);
            }
        }
    }

    public void reassign() {
        synchronized (this.queue) {
            stop();
            if (!this.queue.isEmpty()) {
                this.state = STARTING;
                threadPool.dispatch(this);
            }
        }
    }

    public void stop() {
        synchronized (this.queue) {
            if (this.state == RUNNING) {
                this.state = STOPPING;
                while (this.state != STOPPED) {
                    try {
                        this.queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                this.state = STOPPED;
            }
        }
    }

    @Override
    public void run() {
        Runnable objElementAt;
        synchronized (this.queue) {
            if (this.state == STARTING) {
                this.state = RUNNING;
                while (true) {
                    synchronized (this.queue) {
                        if (this.queue.isEmpty() || this.state != RUNNING) {
                            break;
                        }
                        objElementAt = this.queue.elementAt(0);
                        this.queue.removeElementAt(0);
                    }
                    try {
                        objElementAt.run();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
                this.state = STOPPED;
                this.queue.notifyAll();
            }
        }
    }
}
