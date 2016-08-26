package grondag.adversity.niceblock.base;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import grondag.adversity.niceblock.modelstate.ModelState;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet;

public class SimpleLoadingCacheTest
{

    private class Loader implements SimpleCacheLoader<Long>
    {

        @Override
        public Long load(ModelState state)
        {
            return new Long(state.stateValue.getKey());
        }
        
    }
    
    private class Runner implements Runnable
    {
        @Override
        public void run()
        {
            for(int i = 0; i < 10000000; i++)
            {
                ModelState modelState = new ModelState();
                long input = random.nextInt(bound);
                modelState.stateValue = set.getSetValueFromBits(input);
                Long result = cache.get(modelState);
                assert(result.longValue() == input);
            }
        }
    }
    
    SimpleLoadingCache<Long> cache = new SimpleLoadingCache<Long>(new Loader(), 1024);
    ModelStateSet set = ModelStateSet.find(ModelStateGroup.find(ModelStateComponents.CORNER_JOIN_STATIC));
    Random random = new Random();
    int bound = (int) ModelStateComponents.CORNER_JOIN_STATIC.getValueCount();
    
    @Test
    public void test()
    {
 
//        ModelState modelState = new ModelState();
//        long input = 457;
//        modelState.stateValue = set.getSetValueFromBits(input);
        
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
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("calls: " + cache.calls.get());
        System.out.println("hits: " + cache.hits.get());
        System.out.println("searches: " + cache.searchCount.get());
        System.out.println("hit rate: " + (float) cache.hits.get() / cache.calls.get());
        System.out.println("avg searches per hit: " + (float) cache.searchCount.get() / cache.hits.get());
        
    }

}
