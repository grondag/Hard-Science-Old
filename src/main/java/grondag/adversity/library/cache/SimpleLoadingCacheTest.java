package grondag.adversity.library.cache;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.library.cache.longKey.LongSimpleCacheLoader;
import grondag.adversity.library.cache.longKey.LongSimpleLoadingCache;
import grondag.adversity.library.cache.longKey.LongAtomicLoadingCache;
import grondag.adversity.library.cache.objectKey2.ObjectSimpleCacheLoader;
import grondag.adversity.library.cache.objectKey2.ObjectSimpleLoadingCache;
import io.netty.util.internal.ThreadLocalRandom;

public class SimpleLoadingCacheTest
{
    /** added to key to produce result */
    private static final long MAGIC_NUMBER = 42L;
    private static final int STEP_COUNT = 10000000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int LOAD_COST = 10;
    private static final AtomicLong twiddler = new AtomicLong(0);
    
    private static class Loader extends CacheLoader<Long, Long> implements LongSimpleCacheLoader<Long>, ObjectSimpleCacheLoader<Long, Long> 
    {
        public Loader createNew()
        {
            return new Loader();
        }

        @SuppressWarnings("unused")
        @Override
        public Long load(long key)
        {
            if(LOAD_COST > 0)
            {
              for(int i = 0; i < LOAD_COST; i++)
              {
                  twiddler.incrementAndGet();
              }
            }
            return new Long(key + MAGIC_NUMBER);
        }

        @SuppressWarnings("unused")
        @Override
        public Long load(Long key)
        {
            if(LOAD_COST > 0)
            {
              for(int i = 0; i < LOAD_COST; i++)
              {
                  twiddler.incrementAndGet();
              }
            }
            return load(key.longValue());
        }
    }
    
    private static interface CacheAdapter
    {
        public abstract long get(long key);
        
        public abstract CacheAdapter newInstance(int maxSize);
    }
    
    private abstract class Runner implements Callable<Void>
    {
        
        private final CacheAdapter subject;
        
        private Runner(CacheAdapter subject)
        {
            this.subject = subject;
        }
        
        @Override
        public Void call()
        {
            try
            {
                Random random = ThreadLocalRandom.current();
                
                for(int i = 0; i < STEP_COUNT; i++)
                {
                    long key = getKey(i, random.nextLong());
                    Long result = subject.get(key);
                    assert(result.longValue() == key + MAGIC_NUMBER);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        
        public abstract long getKey(int step, long randomLong);
    }
    
    private class UniformRunner extends Runner
    {
        private final long keyMask;
        
        private UniformRunner(CacheAdapter subject, long keyMask)
        {
            super(subject);
            this.keyMask = keyMask;
        }

        @Override
        public long getKey(int step, long randomLong)
        {
            return randomLong & keyMask;
        }
    }

    /** shifts from one set of uniform demand to another and then back again */
    private class ShiftRunner extends Runner
    {
        private final static int FIRST_MILESTONE = STEP_COUNT / 3;
        private final static int SECOND_MILESTONE = FIRST_MILESTONE * 2;

        private final long keyMask;
        
        private ShiftRunner(CacheAdapter subject, long keyMask)
        {
            super(subject);
            this.keyMask = keyMask;
        }

        @Override
        public long getKey(int step, long randomLong)
        {
            //return odd values in 1st and 3rd phase, even in middle phase
            if(step < FIRST_MILESTONE || step > SECOND_MILESTONE)
            {
                if((randomLong & 1L) == 0) randomLong++;
            }
            else
            {
                if((randomLong & 1L) == 1) randomLong++;
            }
            return randomLong & keyMask;
        }
    }

    @SuppressWarnings("unused")
    private class GoogleAdapter implements CacheAdapter
    {    
        private LoadingCache<Long, Long> cache;
     
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.getUnchecked(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            GoogleAdapter result = new GoogleAdapter();
  
            result.cache = CacheBuilder.newBuilder().concurrencyLevel(THREAD_COUNT).initialCapacity(maxSize).maximumSize(maxSize).build(new Loader());
            
            return result;
        }
    }

    @SuppressWarnings("unused")
    private class LongAtomicAdapter implements CacheAdapter
    {    
        private LongAtomicLoadingCache<Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            LongAtomicAdapter result = new LongAtomicAdapter();
            result.cache = new LongAtomicLoadingCache<Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    @SuppressWarnings("unused")
    private class LongSimpleAdapter implements CacheAdapter
    {    
        private LongSimpleLoadingCache<Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            LongSimpleAdapter result = new LongSimpleAdapter();
            result.cache = new LongSimpleLoadingCache<Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    private class ObjectSimpleAdapter implements CacheAdapter
    {    
        private ObjectSimpleLoadingCache<Long, Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            ObjectSimpleAdapter result = new ObjectSimpleAdapter();
            result.cache = new ObjectSimpleLoadingCache<Long, Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    AtomicLong nanoCount = new AtomicLong(0);
    
    private void doTestInner(ExecutorService executor, CacheAdapter subject)
    {
        ArrayList<Runner> runs = new ArrayList<Runner>();
        
        System.out.println("Practical best case: key space == max capacity - uniform random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0xFFFFF);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject, 0xFFFFF));
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Suboptimal case: moderately constrained memory test - uniform random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0xCCCCC);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject, 0xFFFFF)); 
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        System.out.println("Worst case: Severely constrained memory test - uniform random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0x2FFFF);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject, 0xFFFFF));
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Nominal case: moderately constrained memory test - shifting random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0x7FFFF);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new ShiftRunner(subject, 0xFFFFF));
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        System.out.println("Nominal case / single thread: moderately constrained memory test - shifting random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0x7FFFF);
        for(int i = 0; i < THREAD_COUNT; i++)
        {
            new ShiftRunner(subject, 0xFFFFF).call();
        }
        System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));

        System.out.println("");
    }
        
    
    public void doTestOuter(ExecutorService executor)
    {
        
        System.out.println("Running simple long cache test");
        doTestInner(executor, new LongSimpleAdapter());
      
        System.out.println("Running atomic long cache test");
        doTestInner(executor, new LongAtomicAdapter());
        
//        System.out.println("Running simple object cache test");
//        doTestInner(executor, new ObjectSimpleAdapter());

//        System.out.println("Running google cache test");
//        doTestInner(executor, new GoogleAdapter());
        
//        System.out.println("Running managed long cache test");
//        doTestInner(executor, new LongRunner(new LongManagedLoadingCache<Long>(new LongSimpleLoadingCache<Long>(new Loader(), 4096), 0xAFFF)));
//        
//        System.out.println("Running managed object cache test");
//        doTestInner(executor, new ObjectRunner(new ObjectManagedLoadingCache<Long, Long>(new Loader(), 4096, 0xAFFF)));
//
//        System.out.println("Running google cache test with reasonable max");
//        doTestInner(executor, new GoogleAdapter(CacheBuilder.newBuilder().concurrencyLevel(THREAD_COUNT).initialCapacity(4096).maximumSize(0xAFFF).build(new Loader())));
        
        
    }
    
    @Test
    public void test()
    {
        
        ExecutorService executor;
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
        
        System.out.println("WARM UP RUN");
        doTestOuter(executor);
        
        System.out.println("TEST RUN");
        doTestOuter(executor);
    }
}
