package grondag.hard_science.simulator.base;


import org.junit.Test;

import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.storage.StorageResourceManager;
import net.minecraft.init.Items;

public class StorageResourceManagerTest
{

    @Test
    public void test()
    {
        ItemResource res = ItemResource.fromStack(Items.COOKED_CHICKEN.getDefaultInstance());
        
        ItemStorage store1 = new ItemStorage(null);
        ItemStorage store2 = new ItemStorage(null);
        
//        BlockProcurementTask task = new BlockProcurementTask();
        
        store1.add(res, 100, false, null);
        store2.add(res, 50, false, null);
        
        StorageResourceManager<StorageTypeStack> subject 
            = new StorageResourceManager<StorageTypeStack>(res, store1, 0, null);
        assert subject.quantityStored() == 100;
        assert subject.quantityAllocated() == 0;
        assert subject.quantityAvailable() == 100;
        
        subject.notifyAdded(store2, 50, null);
        
        
    }

}