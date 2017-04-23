package grondag.adversity.library.cache;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.library.cache.longKey.ILongLoadingCache;
import grondag.adversity.library.cache.longKey.LongManagedLoadingCache;
import grondag.adversity.library.cache.longKey.LongSimpleCacheLoader;
import grondag.adversity.library.cache.longKey.LongSimpleLoadingCache;
import grondag.adversity.library.cache.objectKey.IObjectLoadingCache;
import grondag.adversity.library.cache.objectKey.ObjectSimpleCacheLoader;
import io.netty.util.internal.ThreadLocalRandom;

public class SimpleLoadingCacheTest
{

    private class Loader extends CacheLoader<Long, Long> implements LongSimpleCacheLoader<Long>, ObjectSimpleCacheLoader<Long, Long> 
    {

        @Override
        public Long load(long key)
        {
            return new Long(key);
           // return new Long(set.getSetValueFromBits(key).getKey());
        }

        @Override
        public Long load(Long key)
        {
            return load(key.longValue());
        }
    }
    
    private static abstract class Runner implements Callable<Void>
    {
        protected abstract long get(long key);
        
        @Override
        public Void call()
        {
            Random random = ThreadLocalRandom.current();
            
            for(int i = 0; i < 100000000; i++)
            {
                long input = random.nextInt(0x2FFFF);
                Long result = get(input);
                assert(result.longValue() == input);
            }
            
            return null;
        }
        
        public abstract Runner copy();
    }
    private static class GoogleRunner extends Runner
    {    
        private LoadingCache<Long, Long> cache;
        
        private GoogleRunner(LoadingCache<Long, Long> cache)
        {
            this.cache = cache;
        }

        @Override
        protected long get(long key)
        {
            return cache.getUnchecked(key);
        }
        
        @Override
        public GoogleRunner copy()
        {
            return new GoogleRunner(cache);
        }
    }

    private static class LongRunner extends Runner
    {    
        private ILongLoadingCache<Long> cache;
        
        private LongRunner(ILongLoadingCache<Long> cache)
        {
            this.cache = cache;
        }

        @Override
        protected long get(long key)
        {
            return cache.get(key);
        }
        
        @Override
        public LongRunner copy()
        {
            return new LongRunner(cache);
        }
    }
    
    private static class ObjectRunner extends Runner
    {    
        private IObjectLoadingCache<Long, Long> cache;
        
        private ObjectRunner(IObjectLoadingCache<Long, Long> cache)
        {
            this.cache = cache;
        }

        @Override
        protected long get(long key)
        {
            return cache.get(key);
        }
        
        @Override
        public ObjectRunner copy()
        {
            return new ObjectRunner(cache);
        }
    }
    
    private void doTestInner(ExecutorService executor, Runner runner)
    {
        ArrayList<Runner> runs = new ArrayList<Runner>();
        runs.add(runner.copy());
        runs.add(runner.copy());
        runs.add(runner.copy());
        runs.add(runner.copy());
        runs.add(runner.copy());
        runs.add(runner.copy());
        runs.add(runner.copy());
        runs.add(runner);
        long startTime = System.nanoTime();
        try
        {
//            runner.call();
            executor.invokeAll(runs);
            System.out.println("Mean lookuptime - absolute = " + ((System.nanoTime() - startTime) / 800000000));
            System.out.println("Mean lookuptime - in thread = " + ((System.nanoTime() - startTime) / 100000000));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
        
    
    public void doTestOuter(ExecutorService executor, int conncurrencyLevel)
    {
      
        System.out.println("Running simple long cache test");
        doTestInner(executor, new LongRunner(new LongSimpleLoadingCache<Long>(new Loader(), 4096)));
//        
//        System.out.println("Running simple object cache test");
//        doTestInner(executor, new ObjectRunner(new ObjectSimpleLoadingCache<Long, Long>(new Loader(), 4096)));
//
//        System.out.println("Running google cache test with large max");
//        doTestInner(executor, new GoogleRunner(CacheBuilder.newBuilder().concurrencyLevel(conncurrencyLevel).initialCapacity(4096).maximumSize(0x4FFFF).build(new Loader())));
        
        System.out.println("Running managed long cache test");
        doTestInner(executor, new LongRunner(new LongManagedLoadingCache<Long>(new Loader(), 4096, 0xAFFF)));
        
//        System.out.println("Running managed object cache test");
//        doTestInner(executor, new ObjectRunner(new ObjectManagedLoadingCache<Long, Long>(new Loader(), 4096, 0xAFFF)));
//
//        System.out.println("Running google cache test with reasonable max");
//        doTestInner(executor, new GoogleRunner(CacheBuilder.newBuilder().concurrencyLevel(conncurrencyLevel).initialCapacity(4096).maximumSize(0xAFFF).build(new Loader())));
        
        
    }
    
    @Test
    public void test()
    {
        int conncurrencyLevel = Runtime.getRuntime().availableProcessors();
        
        ExecutorService executor;
        executor = Executors.newFixedThreadPool(conncurrencyLevel);
        
        System.out.println("WARM UP RUN");
        doTestOuter(executor, conncurrencyLevel);
        
        System.out.println("TEST RUN");
        doTestOuter(executor, conncurrencyLevel);
    }

}
