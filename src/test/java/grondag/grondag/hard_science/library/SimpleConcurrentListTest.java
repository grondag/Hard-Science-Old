package grondag.hard_science.library;


import java.util.ArrayList;

import org.junit.Test;

import grondag.hard_science.library.concurrency.SimpleConcurrentList;

public class SimpleConcurrentListTest
{
    private class TestItem
    {
        @SuppressWarnings("unused")
        private final int value;
        
        private TestItem(int value)
        {
            this.value = value;
        }
        
    }

    @Test
    public void test()
    {
        SimpleConcurrentList<TestItem> list = SimpleConcurrentList.create(false, "", null);
        ArrayList<Integer> inputs = new ArrayList<Integer>();
        
        for(int i = 0; i < 10000000; i++)
        {
            inputs.add(i);
        }
        
        inputs.parallelStream().forEach(i -> list.add(new TestItem(i)));
        
        assert(list.size() == inputs.size());
        
    }

   

}
