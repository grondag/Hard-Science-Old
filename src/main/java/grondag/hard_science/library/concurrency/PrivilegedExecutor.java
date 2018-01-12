package grondag.hard_science.library.concurrency;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Single-thread executor service with ability to submit privileged tasks
 * that run before non-privileged tasks that have not yet started.
 */
public class PrivilegedExecutor extends ThreadPoolExecutor
{
    public final String threadName;
    
    
    /**
     * The String instance pass to the constructor will be the actual
     * instance value in {@link Thread#getName()} for all threads
     * created for this executor.  No numeric sequence nor anything
     * else is added to the thread name, even if it needs to be
     * recreated.<p>
     * 
     * This means we can always test a thread for ownership by
     * this executor using == vs needing any kind of string comparison.
     * We use these checks frequently, so is desirable to keep it lightweight.
     */
    public PrivilegedExecutor(String threadName)
    {
        super
        (
            1, 
            1,
            0L, 
            TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<Runnable>(11, new Comparator<Runnable>() 
            {

                @Override
                public int compare(Runnable o1, Runnable o2)
                {
                    // note reverse order because we want true (privileged) start
                    return Boolean.compare(
                            ((IPrivileged)o2).isPrivileged(), 
                            ((IPrivileged)o1).isPrivileged());
                }
            }),
            new ThreadFactory()
            {
                @Override
                public Thread newThread(Runnable r)
                {
                    Thread thread = new Thread(r, threadName);
                    thread.setDaemon(true);
                    return thread;
                }
            }
        );
       this.threadName = threadName;
    }
    
    /**
     * True if the current thread is the execution thread for this service.
     */
    public boolean isRunningOn()
    {
        return Thread.currentThread().getName() == this.threadName;
    }
    
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
    {
        throw new UnsupportedOperationException("ambiguous execution on privileged executor");
    }
    
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
    {
        throw new UnsupportedOperationException("ambiguous execution on privileged executor");
    }
    
    private interface IPrivileged
    {
        public boolean isPrivileged();
    }
    
    private static class PrivilegedFutureTask<T> extends FutureTask<T> implements IPrivileged
    {
        private final boolean isPrivileged;

        private PrivilegedFutureTask(Callable<T> callable, boolean isPrivileged)
        {
            super(callable);
            this.isPrivileged = isPrivileged;
        }

        private PrivilegedFutureTask(Runnable runnable, T result, boolean isPrivileged)
        {
            super(runnable, result);
            this.isPrivileged = isPrivileged;
        }

        @Override
        public boolean isPrivileged()
        {
            return this.isPrivileged;
        }
    }
    
    private static class PrivilegedRunnable implements Runnable, IPrivileged
    {
        private final boolean isPrivileged;
        private final Runnable wrapped;
        
        private PrivilegedRunnable(Runnable wrapped, boolean isPrivileged)
        {
            this.wrapped = wrapped;
            this.isPrivileged = isPrivileged;
        }
        
        @Override
        public void run()
        {
            this.wrapped.run();
        }

        @Override
        public boolean isPrivileged()
        {
            return this.isPrivileged;
        }
    }

    public void execute(Runnable command, boolean isPrivileged)
    {
        super.execute(new PrivilegedRunnable(command, isPrivileged));
    }

    public <T> Future<T> submit(Callable<T> task, boolean isPrivileged)
    {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = new PrivilegedFutureTask<T>(task, isPrivileged);
        super.execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Runnable task, T result, boolean isPrivileged)
    {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = new PrivilegedFutureTask<T>(task, result, isPrivileged);
        super.execute(ftask);
        return ftask;
    }

    public Future<?> submit(Runnable task, boolean isPrivileged)
    {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = new PrivilegedFutureTask<Void>(task, null, isPrivileged);
        super.execute(ftask);
        return ftask;
    }
    
    @Override 
    public void execute(Runnable command)
    {
        this.execute(command, false);
    }
    
    @Override
    public Future<?> submit(Runnable task)
    {
        return this.submit(task, false);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        return this.submit(task, result, false);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        return this.submit(task, false);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
    {
        throw new UnsupportedOperationException("Unsupported operation on privileged executor");
    }
}
