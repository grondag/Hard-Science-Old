package grondag.hard_science.simulator.base;


import org.junit.Test;

import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceCache;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemResourceTest
{

    @Test
    public void test()
    {
        Item i1 = Items.BEETROOT_SEEDS;
        Item i2 = Items.BLAZE_POWDER;
        
        ItemResource r1a = ItemResourceCache.fromStack(i1.getDefaultInstance());
        ItemStack stack = i1.getDefaultInstance().copy();
        stack.setCount(32);
        ItemResource r1b = ItemResourceCache.fromStack(stack);
        
        assert r1a.equals(r1b);
        assert r1a.hashCode() == r1b.hashCode();
        assert r1a == r1b;
        
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("Boop!", true);
        stack.setTagCompound(nbt);
        
        r1b = ItemResourceCache.fromStack(stack);
        
        // should not match now because of NBT tag
        assert !r1a.equals(r1b);
        assert r1a.hashCode() != r1b.hashCode();
        
        nbt = new NBTTagCompound();
        nbt.setInteger("bloop", 1);
        nbt.setInteger("blorp", 2);
        stack = i2.getDefaultInstance().copy();
        stack.setTagCompound(nbt);
        ItemResource r2a = ItemResourceCache.fromStack(stack);
        
        assert !r1a.equals(r1b);
        assert r1a.hashCode() != r1b.hashCode();
        
        nbt = new NBTTagCompound();
        nbt.setInteger("bloop", 1);
        nbt.setInteger("blorp", 2);
        stack = i2.getDefaultInstance().copy();
        stack.setTagCompound(nbt);
        ItemResource r2b = ItemResourceCache.fromStack(stack);
        
        assert r2a.equals(r2b);
        assert r2a.hashCode() == r2b.hashCode();
        
        nbt = new NBTTagCompound();
        nbt.setInteger("bloop", 5);
        stack = i2.getDefaultInstance().copy();
        stack.setTagCompound(nbt);
        r2b = ItemResourceCache.fromStack(stack);
        
        assert !r2a.equals(r2b);
        assert r2a.hashCode() != r2b.hashCode();
    }

}