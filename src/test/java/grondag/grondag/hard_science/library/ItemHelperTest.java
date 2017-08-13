package grondag.hard_science.library;

import org.junit.Test;

import grondag.hard_science.library.varia.ItemHelper;
import grondag.hard_science.library.varia.ItemHelper.TestItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public class ItemHelperTest
{

    
    @Test
    public void test()
    {
        Item itemA = new Item();
        Item itemB = new Item();
        
        TestItemStack stackA1 = new TestItemStack(itemA, 5, 0, null);
        
        TestItemStack stackA2 = new TestItemStack(itemA, 3, 0, null);
        
        TestItemStack stackB1 = new TestItemStack(itemB, 1, 1, null);
        
        assert(ItemHelper.canStacksCombine(stackA1, stackA2));
        assert(ItemHelper.stackHashCode(stackA1) == ItemHelper.stackHashCode(stackA2));
        
        assert(!ItemHelper.canStacksCombine(stackA1, stackB1));
        assert(ItemHelper.stackHashCode(stackA1) != ItemHelper.stackHashCode(stackB1));
        
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("blort!", 42);
        stackA1.tag = tag;
        
        tag = new NBTTagCompound();
        tag.setLong("blort!", 42);
        stackA2.tag = tag;
        
        assert(ItemHelper.canStacksCombine(stackA1, stackA2));
        assert(ItemHelper.stackHashCode(stackA1) == ItemHelper.stackHashCode(stackA2));
        
        tag = new NBTTagCompound();
        tag.setLong("lerm", 42);
        stackA2.tag = tag;
        
        assert(!ItemHelper.canStacksCombine(stackA1, stackA2));
        assert(ItemHelper.stackHashCode(stackA1) != ItemHelper.stackHashCode(stackA2));
        
        stackA1.tag = tag;
        stackA1.meta = 79;
        
        assert(!ItemHelper.canStacksCombine(stackA1, stackA2));
        assert(ItemHelper.stackHashCode(stackA1) != ItemHelper.stackHashCode(stackA2));
        
        stackA2.meta = 79;
        assert(ItemHelper.canStacksCombine(stackA1, stackA2));
        assert(ItemHelper.stackHashCode(stackA1) == ItemHelper.stackHashCode(stackA2));
        
    }

}
