package grondag.adversity.library.cache;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.library.cache.longKey.ILongLoadingCache;
import grondag.adversity.library.cache.longKey.LongManagedLoadingCache;
import grondag.adversity.library.cache.longKey.LongSimpleCacheLoader;
import grondag.adversity.library.cache.longKey.LongSimpleLoadingCache;
import grondag.adversity.library.cache.objectKey.IObjectLoadingCache;
import grondag.adversity.library.cache.objectKey.ObjectManagedLoadingCache;
import grondag.adversity.library.cache.objectKey.ObjectSimpleCacheLoader;
import grondag.adversity.library.cache.objectKey.ObjectSimpleLoadingCache;
import io.netty.util.internal.ThreadLocalRandom;

public class SimpleLoadingCacheTest
{
    /** added to key to produce result */
    private static final long MAGIC_NUMBER = 42L;
    private static final int TEST_PASSES = 10000000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    
    
    private static class Loader extends CacheLoader<Long, Long> implements LongSimpleCacheLoader<Long>, ObjectSimpleCacheLoader<Long, Long> 
    {

        @Override
        public Long load(long key)
        {
            return new Long(key + MAGIC_NUMBER);
        }

        @Override
        public Long load(Long key)
        {
            return load(key.longValue());
        }
    }
    
    private static interface CacheAdapter
    {
        public abstract long get(long key);
        
        public abstract CacheAdapter newInstance(int startingSize, int maxSize);
    }
    
    private static abstract class Runner implements Callable<Void>
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
                
                for(int i = 0; i < TEST_PASSES; i++)
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
    
    private static class UniformRunner extends Runner
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


    private static class GoogleAdapter implements CacheAdapter
    {    
        private LoadingCache<Long, Long> cache;
     
        @Override
        public long get(long key)
        {
            return cache.getUnchecked(key);
        }

        @Override
        public CacheAdapter newInstance(int startingSize, int maxSize)
        {
            GoogleAdapter result = new GoogleAdapter();
            
            if(maxSize == 0)
            {
                result.cache = CacheBuilder.newBuilder().concurrencyLevel(THREAD_COUNT).initialCapacity(startingSize).build(new Loader());
            }
            else
            {
                result.cache = CacheBuilder.newBuilder().concurrencyLevel(THREAD_COUNT).initialCapacity(startingSize).maximumSize(maxSize).build(new Loader());
            }
            return result;
        }
    }

    private static class LongAdapter implements CacheAdapter
    {    
        private ILongLoadingCache<Long> cache;
        
        @Override
        public long get(long key)
        {
            return cache.get(key);
        }

        @Override
        public CacheAdapter newInstance(int startingSize, int maxSize)
        {
            LongAdapter result = new LongAdapter();
            if(maxSize == 0)
            {
                result.cache = new LongSimpleLoadingCache<Long>(new Loader(), startingSize);
            }
            else
            {
                result.cache = new LongManagedLoadingCache<Long>(new LongSimpleLoadingCache<Long>(new Loader(), startingSize), maxSize);
            }
            return result;
        }
    }
    
//    private static class ObjectRunner extends Runner
//    {    
//        private IObjectLoadingCache<Long, Long> cache;
//        
//        private ObjectRunner(IObjectLoadingCache<Long, Long> cache)
//        {
//
//            result.cache = LongAdapter;
//        }
//
//        @Override
//        protected long get(long key)
//        {
//            return cache.get(key);
//        }
//        
//        @Override
//        public ObjectRunner copy()
//        {
//            return new ObjectRunner(cache);
//        }
//    }
    
    private void doTestInner(ExecutorService executor, CacheAdapter subject)
    {
        ArrayList<Runner> runs = new ArrayList<Runner>();
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject.newInstance(4096, 0), 0x2FFF));
        }
        long startTime = System.nanoTime();
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean lookuptime - elapsed = " + ((System.nanoTime() - startTime) / (TEST_PASSES * THREAD_COUNT)));
            System.out.println("Mean lookuptime - in thread = " + ((System.nanoTime() - startTime) / TEST_PASSES));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
        
    
    public void doTestOuter(ExecutorService executor)
    {
      
        System.out.println("Running simple long cache test");
        doTestInner(executor, new LongAdapter());
        
//        System.out.println("Running simple object cache test");
//        doTestInner(executor, new ObjectRunner(new ObjectSimpleLoadingCache<Long, Long>(new Loader(), 4096)));

        System.out.println("Running google cache test");
        doTestInner(executor, new GoogleAdapter());
        
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
