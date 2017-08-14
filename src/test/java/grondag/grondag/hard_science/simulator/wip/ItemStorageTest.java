package grondag.hard_science.simulator.wip;



import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import grondag.hard_science.simulator.wip.DomainManager.Domain;
import grondag.hard_science.simulator.wip.IStorage.StorageWithQuantity;
import grondag.hard_science.simulator.wip.IStorage.StorageWithResourceAndQuantity;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraft.item.Item;

public class ItemStorageTest
{

    @Test
    public void test()
    {
DomainManager dm = new DomainManager();
        
        Domain d = dm.createDomain();
        
        ItemStorageManager ism = new ItemStorageManager();
        ism.setDomain(d);
        
        ItemStorage store1 = new ItemStorage();
        store1.setCapacity(100);
        
        ItemStorage store2 = new ItemStorage();
        store2.setCapacity(200);
        
        ItemStorage store3 = new ItemStorage();
        store3.setCapacity(300);
        
        ItemStorage store4 = new ItemStorage();
        store4.setCapacity(400);
        
        Item i1 = new Item().setRegistryName("one");
        Item i2 = new Item().setRegistryName("two");
        Item i3 = new Item().setRegistryName("three");
        Item.REGISTRY.register(1, i1.getRegistryName(), i1);
        Item.REGISTRY.register(2, i2.getRegistryName(), i2);
        Item.REGISTRY.register(3, i3.getRegistryName(), i3);
        
        ItemResource res1 = new ItemResource(i1, 0, null, null);
        ItemResource res2 = new ItemResource(i2, 0, null, null);
        ItemResource res3 = new ItemResource(i3, 0, null, null);
        
        store1.add(res1, 10, false);
        assert store1.getQuantityStored(res1) == 10;
        assert store1.availableCapacity() == 90;
        
        store1.add(res1, 10, false);
        assert store1.add(res1, 1000, true) == 80;
        
        assert store1.getQuantityStored(res1) == 20;
        assert store1.availableCapacity() == 80;
        
        store1.add(res2, 22, false);
        store1.add(res3, 33, false);
        
        store2.deserializeNBT(store1.serializeNBT());
        
        assert store1.availableCapacity() == store2.availableCapacity();
        assert store1.getQuantityStored(res1) == store2.getQuantityStored(res1);
        assert store1.getQuantityStored(res2) == store2.getQuantityStored(res2);
        assert store1.getQuantityStored(res3) == store2.getQuantityStored(res3);

        store2 = new ItemStorage();
        store2.setCapacity(200);
        
        store2.add(res2, 100, false);
        store3.add(res1, 100, false);
        store3.add(res2, 100, false);
        store3.add(res3, 100, false);
        
        ism.addStore(store1);
        assert ism.getQuantityStored(res1) == store1.getQuantityStored(res1);
        assert ism.getCapacity() == store1.getCapacity();
        assert ism.availableCapacity() == store1.availableCapacity();
        
        // this should generate a warning
        ism.addStore(store1);
        
        ism.addStore(store2);
        ism.addStore(store3);
        ism.addStore(store4);
        
        assert ism.getQuantityStored(res1) == store1.getQuantityStored(res1) + store2.getQuantityStored(res1) + store3.getQuantityStored(res1);
        assert ism.getQuantityStored(res2) == store1.getQuantityStored(res2) + store2.getQuantityStored(res2) + store3.getQuantityStored(res2);
        assert ism.getQuantityStored(res3) == store1.getQuantityStored(res3) + store2.getQuantityStored(res3) + store3.getQuantityStored(res3);
        assert ism.getCapacity() == store1.getCapacity() + store2.getCapacity() + store3.getCapacity() + store4.getCapacity();
        
        store2.add(res1,  5, false);
        
        List<StorageWithQuantity<StorageTypeStack>> list = ism.getLocations(res1);
        assert list.size() == 3;
        assert confirmStoreAndQuantity(list, store1, store1.getQuantityStored(res1));
        assert confirmStoreAndQuantity(list, store2, store2.getQuantityStored(res1));
        assert confirmStoreAndQuantity(list, store3, store3.getQuantityStored(res1));
        
        list = ism.findSpaceFor(res3, 2000);
        assert list.size() == 3;
        assert confirmStoreAndQuantity(list, store1, store1.availableCapacity());
        assert confirmStoreAndQuantity(list, store2, store2.availableCapacity());
        assert confirmStoreAndQuantity(list, store4, store4.availableCapacity());
        
        assert ism.findQuantity(new Predicate<IResource<StorageTypeStack>>() 
        {
            @Override
            public boolean test(IResource<StorageTypeStack> t)
            {
                return t.equals(res2);
            }
        }).get(0).quantity == ism.getQuantityStored(res2);
        
        List<StorageWithResourceAndQuantity<StorageTypeStack>> findList = ism.findStorageWithQuantity(new Predicate<IResource<StorageTypeStack>>() 
        {
            @Override
            public boolean test(IResource<StorageTypeStack> t)
            {
                return t.equals(res1);
            }
        });
        assert findList.size() == 3;
        assert findList.get(0).resource.equals(res1);
        assert findList.get(1).resource.equals(res1);
        assert findList.get(2).resource.equals(res1);
        assert findList.get(0).quantity + findList.get(1).quantity + findList.get(2).quantity == ism.getQuantityStored(res1);
        
        dm.setSaveDirty(false);
        assert store3.takeUpTo(res1, 1, false) == 1;
        assert dm.isSaveDirty();

        dm.setSaveDirty(false);
        long oldCapacity = ism.getCapacity();
        long oldAvailable = ism.availableCapacity();
        ism.removeStore(store1);
        // should generate a warning
        ism.removeStore(store1);
        assert dm.isSaveDirty();
        assert ism.getCapacity() == oldCapacity - store1.getCapacity();
        assert ism.availableCapacity() == oldAvailable - store1.availableCapacity();
        assert ism.getQuantityStored(res1) == store2.getQuantityStored(res1) + store3.getQuantityStored(res1);

        ism.addStore(store1);
        assert ism.getCapacity() == oldCapacity ;
        assert ism.availableCapacity() == oldAvailable;
        assert ism.getQuantityStored(res1) == store1.getQuantityStored(res1) + store2.getQuantityStored(res1) + store3.getQuantityStored(res1);
        
        // this will cause collisions in assigned number index - will not happen in game because won't clone storage managers this way
        ItemStorageManager ism2 = new ItemStorageManager(ism.serializeNBT());
        assert ism2.availableCapacity() == ism.availableCapacity();
        assert ism2.getQuantityStored(res2) == ism.getQuantityStored(res2);
        store4.add(res2, 7, false);
        assert ism2.getQuantityStored(res2) == ism.getQuantityStored(res2) - 7;
    }

    private boolean confirmStoreAndQuantity(List<StorageWithQuantity<StorageTypeStack>> list, ItemStorage store, long quantity)
    {
        for(StorageWithQuantity<StorageTypeStack> entry : list)
        {
            if(entry.storage == store && entry.quantity == quantity) return true;
        }
        return false;
    }
    
   
}
