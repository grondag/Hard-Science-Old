package grondag.hard_science.simulator.base;

import org.junit.Test;

import grondag.hard_science.simulator.base.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.base.ItemResource;
import grondag.hard_science.simulator.base.StorageType.StorageTypeStack;
import net.minecraft.item.Item;

public class ResourceWithQuantityTest
{

    @Test
    public void test()
    {
        Item i1 = new Item();
        
        ItemResource r1 = new ItemResource(i1, 0, null, null);
        
        AbstractResourceWithQuantity<StorageTypeStack> stack = r1.withQuantity(5);
        
        assert stack.getQuantity() == 5;
        assert !stack.isEmpty();
        
        assert stack.takeUpTo(27) == 5;
        
        assert stack.isEmpty();
        
        assert stack.add(518257) == 518257;
        
    }

}