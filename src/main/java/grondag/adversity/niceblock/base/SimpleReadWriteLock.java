package grondag.adversity.niceblock.base;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** adapted from http://tutorials.jenkov.com/java-concurrency/read-write-locks.html */

public class SimpleReadWriteLock
{
    private AtomicInteger readerCount = new AtomicInteger(0);
    private volatile int writeRequestCount = 0;
    private Object writeRequestCountLock = new Object();
    private AtomicBoolean writeLock = new AtomicBoolean(false);

    public void lockRead()
    {
        synchronized(writeRequestCountLock)
        {
            while(writeRequestCount != 0)
            {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
            readerCount.incrementAndGet();
        }
    }

    public void unlockRead()
    {
        readerCount.decrementAndGet();
        notifyAll();
    }

    public void lockWrite()
    {
        synchronized(writeRequestCountLock)
        {   
            writeRequestCount++;
        }

        while(readerCount.get() != 0 || !writeLock.compareAndSet(false, true))
        {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    public void unlockWrite() throws InterruptedException
    {
        writeLock.compareAndSet(true, false);
        synchronized(writeRequestCountLock)
        {  
            writeRequestCount--;
        }
        notifyAll();
    }
}