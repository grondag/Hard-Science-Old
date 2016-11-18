package grondag.adversity.library.cache;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet;

public class ManagedLoadingCacheTest
{

    private class Loader extends CacheLoader<Long, Long> implements SimpleCacheLoader<Long> 
    {

        @Override
        public Long load(long key)
        {
            return new Long(key);
           // return new Long(set.getSetValueFromBits(key).getKey());
        }

        @Override
        public Long load(Long key) throws Exception
        {
            return load(key.longValue());
        }
        
    }

    ILoadingCache<Long> cache = new ManagedLoadingCache<Long>(new Loader(), 4096, 0xFFFF);
    Random random = new Random();

    private class Runner implements Runnable
    {
        @Override
        public void run()
        {
            for(int i = 0; i < 100000000; i++)
            {
                long input = random.nextInt(0x2FFFF);
                Long result = cache.get(input);
                assert(result.longValue() == input);
            }
        }
    }
    
    @Test
    public void test()
    {
        ExecutorService executor;
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.execute(new Runner());
        executor.shutdown();
        try
        {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
//        System.out.println("calls: " + cache.calls.get());
//        System.out.println("hits: " + cache.hits.get());
//        System.out.println("searches: " + cache.searchCount.get());
//        System.out.println("hit rate: " + (float) cache.hits.get() / cache.calls.get());
//        System.out.println("avg searches per hit: " + (float) cache.searchCount.get() / cache.hits.get());
        
    }

}
