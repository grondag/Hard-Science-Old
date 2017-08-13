package grondag.hard_science.simulator.wip;


import org.junit.Test;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public class ItemResourceTest
{

    @Test
    public void test()
    {
        Item i1 = new Item();
        Item i2 = new Item();
        
        ItemResource r1a = new ItemResource(i1, 0, null, null);
        ItemResource r1b = new ItemResource(i1, 0, null, null);
        
        assert r1a.equals(r1b);
        assert r1a.hashCode() == r1b.hashCode();
        
        r1b = new ItemResource(i1, 1, null, null);
        
        assert !r1a.equals(r1b);
        assert r1a.hashCode() != r1b.hashCode();
        
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("bloop", 1);
        NBTTagCompound caps = new NBTTagCompound();
        nbt.setInteger("blorp", 2);
        ItemResource r2a = new ItemResource(i2, 5785, nbt, caps);
        
        assert !r1a.equals(r1b);
        assert r1a.hashCode() != r1b.hashCode();
        
        nbt = new NBTTagCompound();
        nbt.setInteger("bloop", 1);
        caps = new NBTTagCompound();
        nbt.setInteger("blorp", 2);
        ItemResource r2b = new ItemResource(i2, 5785, nbt, caps);
        
        assert r2a.equals(r2b);
        assert r2a.hashCode() == r2b.hashCode();
        
        nbt = new NBTTagCompound();
        nbt.setInteger("bloop", 5);
        r2b = new ItemResource(i2, 5785, nbt, caps);
        
        assert !r2a.equals(r2b);
        assert r2a.hashCode() != r2b.hashCode();
        
        // won't work without MC runtime functional
//        NBTTagCompound save = r2a.serializeNBT();
//        
//        // would not normally be used this way!
//        r2b.deserializeNBT(save);
//        
//        assert r2a.equals(r2b);
//        assert r2a.hashCode() == r2b.hashCode();
    }

}
