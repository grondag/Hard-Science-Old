package grondag.adversity.library;


import java.util.ArrayList;

import org.junit.Test;

public class SimpleConcurrentListTest
{
    private class TestItem implements ISimpleListItem
    {
        @SuppressWarnings("unused")
        private final int value;
        private boolean isDeleted = false;
        
        private TestItem(int value)
        {
            this.value = value;
        }
        
        @SuppressWarnings("unused")
        private void delete()
        {
            this.isDeleted = true;
        }
        
        @Override
        public boolean isDeleted()
        {
            return isDeleted;
        }
    }

    @Test
    public void test()
    {
        SimpleConcurrentList<TestItem> list = new SimpleConcurrentList<TestItem>();
        ArrayList<Integer> inputs = new ArrayList<Integer>();
        
        for(int i = 0; i < 10000000; i++)
        {
            inputs.add(i);
        }
        
        inputs.parallelStream().forEach(i -> list.add(new TestItem(i)));
        
        assert(list.size() == inputs.size());
        
    }

   

}
