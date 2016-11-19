package grondag.adversity.library.cache;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.CacheLoader;

public class SimpleLoadingCacheTest
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
    
    ILoadingCache<Long> cache = new SimpleLoadingCache<Long>(new Loader(), 4096);
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

        
    }

}
