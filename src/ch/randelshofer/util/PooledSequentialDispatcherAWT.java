package ch.randelshofer.util;

import java.util.ArrayList;
import java.util.List;

public class PooledSequentialDispatcherAWT implements Runnable {
    private static ConcurrentDispatcherAWT threadPool = new ConcurrentDispatcherAWT();

    private static final int STOPPED = 0;

    private static final int STARTING = 1;

    private static final int RUNNING = 2;

    private static final int STOPPING = 3;

    private volatile int state = STOPPED;

    private final List<Runnable> queue = new ArrayList<>();

    public static void dispatchConcurrently(Runnable runnable) {
        threadPool.dispatch(runnable);
    }

    public void dispatch(Runnable runnable) {
        dispatch(runnable, threadPool);
    }

    public void dispatch(Runnable runnable, ConcurrentDispatcherAWT concurrentDispatcherAWT) {
        synchronized (this.queue) {
            this.queue.add(runnable);
            if (this.state == STOPPED) {
                this.state = STARTING;
                concurrentDispatcherAWT.dispatch(this);
            }
        }
    }

    public void reassign() {
        stop();
        synchronized (this.queue) {
            if (!this.queue.isEmpty()) {
                this.state = STARTING;
                threadPool.dispatch(this);
            }
        }
    }

    private void stop() {
        synchronized (this.queue) {
            if (this.state == RUNNING) {
                this.state = STOPPING;
            } else {
                this.state = STOPPED;
            }
            while (this.state != STOPPED) {
                try {
                    this.queue.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public boolean isRunning() {
        return this.state != STOPPED || threadPool.getThreadCount() != 0;
    }

    @Override
    public void run() {
        synchronized (this.queue) {
            if (this.state == STARTING) {
                this.state = RUNNING;
            } else {
                return;
            }
        }
        try {
            Runnable objElementAt;
            while (true) {
                synchronized (this.queue) {
                    if (this.queue.isEmpty() || this.state != RUNNING) {
                        this.state = STOPPED;
                        this.queue.notifyAll(); // Interrupted this.queue.wait()
                        break;
                    }
                    objElementAt = this.queue.remove(0);
                }
                objElementAt.run();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
