package grondag.adversity.simulator;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskCounter
{
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    
    public synchronized void waitUntilAllTasksComplete()
    {
        while(activeTaskCount.get() > 0) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Reliably increments active task count.
     */
    public void incrementActiveTasks() {
        activeTaskCount.incrementAndGet();
    }

    /**
     * Reliably decrements active task count.
     */
    public void decrementActiveTasks() {
        activeTaskCount.decrementAndGet();
        synchronized(this)
        {
            this.notifyAll();
        }
    }   
}