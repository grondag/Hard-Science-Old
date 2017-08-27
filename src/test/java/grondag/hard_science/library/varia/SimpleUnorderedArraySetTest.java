package grondag.hard_science.library.varia;

import org.junit.Test;

import grondag.hard_science.superblock.varia.KeyedTuple;

public class SimpleUnorderedArraySetTest
{
    private class TestObject extends KeyedTuple<String>
    {
        public final int value;
        
        private TestObject(String key, int value)
        {
            super(key);
            this.value = value;
        }
    }
    
    @Test
    public void test()
    {
        SimpleUnorderedArraySet<TestObject> set = new SimpleUnorderedArraySet<TestObject>();
        
        TestObject tobj = new TestObject("A", 1);
        set.putIfNotPresent(tobj);
        assert set.size == 1;
        assert set.contains(new TestObject("A", 2));
        
        assert set.putIfNotPresent(new TestObject("A", 3)) == tobj;
        assert set.findIndex(tobj) == 0;
        assert set.get(tobj) == tobj;
        assert set.size == 1;
        assert set.get(0).value == 1;
        
        set.putIfNotPresent(new TestObject("B", 2));
        set.putIfNotPresent(new TestObject("C", 3));
        assert set.size == 3;
        
        set.put(new TestObject("A", 4));
        int index = set.findIndex(new TestObject("A", 0));
        assert set.get(index).value == 4;
        
        
    }

}
