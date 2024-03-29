// need to be redone if keeping

//package grondag.hard_science.simulator.base;
//
//import java.util.List;
//import java.util.function.Predicate;
//
//import org.junit.Test;
//
//import com.google.common.collect.ImmutableList;
//
//import grondag.hard_science.library.world.Location;
//import grondag.hard_science.simulator.device.DeviceManager;
//import grondag.hard_science.simulator.domain.Domain;
//import grondag.hard_science.simulator.domain.DomainManager;
//import grondag.hard_science.simulator.resource.IResource;
//import grondag.hard_science.simulator.resource.ItemResource;
//import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
//import grondag.hard_science.simulator.storage.ItemStorage;
//import grondag.hard_science.simulator.storage.StorageManager;
//import grondag.hard_science.simulator.storage.StorageWithQuantity;
//import grondag.hard_science.simulator.storage.StorageWithResourceAndQuantity;
//import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
//import grondag.hard_science.simulator.transport.endpoint.PortFunction;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//
//public class ItemStorageTest
//{
//
//    @Test
//    public void test()
//    {
//        DomainManager dm = DomainManager.instance();
//        dm.loadNew();
//        
//        Domain d = dm.createDomain();
//        
//        StorageManager<StorageTypeStack> ism = d.itemStorage;
//        
//        ItemStorage store1 = new ItemStorage(CarrierLevel.BOTTOM, PortFunction.CARRIER);
//        store1.setCapacity(100);
//        store1.setDomain(d);
//        store1.setLocation(new Location(1, 1, 1, 1));
//        DeviceManager.addDevice(store1);
//        
//        ItemResource res1 = ItemResource.fromStack(Items.BEEF.getDefaultInstance());
//        ItemResource res2 = ItemResource.fromStack(Items.BAKED_POTATO.getDefaultInstance());
//        ItemResource res3 = ItemResource.fromStack(Items.BREWING_STAND.getDefaultInstance());
//        
//        store1.add(res1, 10, false, null);
//        assert store1.getQuantityStored(res1) == 10;
//        assert store1.availableCapacity() == 90;
//        
//        store1.add(res1, 10, false, null);
//        assert store1.add(res1, 1000, true, null) == 80;
//        
//        assert store1.getQuantityStored(res1) == 20;
//        assert store1.availableCapacity() == 80;
//        
//        store1.add(res2, 22, false, null);
//        store1.add(res3, 33, false, null);
//        
//        assert ism.getQuantityStored(res1) == store1.getQuantityStored(res1);
//        assert ism.getCapacity() == store1.getCapacity();
//        assert ism.availableCapacity() == store1.availableCapacity();
//        
//        ItemStorage store2 = new ItemStorage(CarrierLevel.BOTTOM, PortFunction.CARRIER);
//        store2.setCapacity(200);
//        store2.setDomain(d);
//        store2.setLocation(new Location(2, 1, 1, 1));
//        DeviceManager.addDevice(store2);
//        
//        ItemStorage store3 = new ItemStorage(CarrierLevel.BOTTOM, PortFunction.CARRIER);
//        store3.setCapacity(300);
//        store3.setDomain(d);
//        store3.setLocation(new Location(3, 1, 1, 1));
//        DeviceManager.addDevice(store3);
//        
//        ItemStorage store4 = new ItemStorage(CarrierLevel.BOTTOM, PortFunction.CARRIER);
//        store4.setCapacity(400);
//        store4.setDomain(d);
//        store4.setLocation(new Location(4, 1, 1, 1));
//        DeviceManager.addDevice(store4);
//        
//        store2.add(res2, 100, false, null);
//        store3.add(res1, 100, false, null);
//        store3.add(res2, 100, false, null);
//        store3.add(res3, 100, false, null);
//        
//        assert ism.getQuantityStored(res1) == store1.getQuantityStored(res1) + store2.getQuantityStored(res1) + store3.getQuantityStored(res1);
//        assert ism.getQuantityStored(res2) == store1.getQuantityStored(res2) + store2.getQuantityStored(res2) + store3.getQuantityStored(res2);
//        assert ism.getQuantityStored(res3) == store1.getQuantityStored(res3) + store2.getQuantityStored(res3) + store3.getQuantityStored(res3);
//        assert ism.getCapacity() == store1.getCapacity() + store2.getCapacity() + store3.getCapacity() + store4.getCapacity();
//        
//        store2.add(res1,  5, false, null);
//        
//        List<StorageWithQuantity<StorageTypeStack>> list = ism.getLocations(res1)
//                .stream().map(p -> p.withQuantity(p.availableCapacity()))
//                .collect(ImmutableList.toImmutableList());
//        assert list.size() == 3;
//        assert confirmStoreAndQuantity(list, store1, store1.getQuantityStored(res1));
//        assert confirmStoreAndQuantity(list, store2, store2.getQuantityStored(res1));
//        assert confirmStoreAndQuantity(list, store3, store3.getQuantityStored(res1));
//        
//        list = ism.findSpaceFor(res3, 2000).stream().map(p -> p.withQuantity(p.availableCapacity()))
//                .collect(ImmutableList.toImmutableList());
//        assert list.size() == 3;
//        assert confirmStoreAndQuantity(list, store1, store1.availableCapacity());
//        assert confirmStoreAndQuantity(list, store2, store2.availableCapacity());
//        assert confirmStoreAndQuantity(list, store4, store4.availableCapacity());
//        
//        assert ism.findQuantityAvailable(new Predicate<IResource<StorageTypeStack>>() 
//        {
//            @Override
//            public boolean test(IResource<StorageTypeStack> t)
//            {
//                return t.equals(res2);
//            }
//        }).get(0).getQuantity() == ism.getQuantityStored(res2);
//        
//        List<StorageWithResourceAndQuantity<StorageTypeStack>> findList = ism.findStorageWithQuantity(new Predicate<IResource<StorageTypeStack>>() 
//        {
//            @Override
//            public boolean test(IResource<StorageTypeStack> t)
//            {
//                return t.equals(res1);
//            }
//        });
//        assert findList.size() == 3;
//        assert findList.get(0).resource.equals(res1);
//        assert findList.get(1).resource.equals(res1);
//        assert findList.get(2).resource.equals(res1);
//        assert findList.get(0).quantity + findList.get(1).quantity + findList.get(2).quantity == ism.getQuantityStored(res1);
//        
//        DeviceManager.instance().setSaveDirty(false);
//        assert store3.takeUpTo(res1, 1, false, null) == 1;
//        assert DeviceManager.instance().isSaveDirty();
//
//        DeviceManager.instance().setSaveDirty(false);
//        long oldCapacity = ism.getCapacity();
//        long oldAvailable = ism.availableCapacity();
//        DeviceManager.removeDevice(store1);
//        
//        assert DeviceManager.instance().isSaveDirty();
//        assert ism.getCapacity() == oldCapacity - store1.getCapacity();
//        assert ism.availableCapacity() == oldAvailable - store1.availableCapacity();
//        assert ism.getQuantityStored(res1) == store2.getQuantityStored(res1) + store3.getQuantityStored(res1);
//
//        DeviceManager.addDevice(store1);
//        assert ism.getCapacity() == oldCapacity ;
//        assert ism.availableCapacity() == oldAvailable;
//        assert ism.getQuantityStored(res1) == store1.getQuantityStored(res1) + store2.getQuantityStored(res1) + store3.getQuantityStored(res1);
//        
//        dm.loadNew();
//
//        // serialization moved to device manager because storage is now a device
////        StorageManager<StorageTypeStack> ism2 = new StorageManager<StorageTypeStack>(StorageType.ITEM, dm.defaultDomain());
////        ism2.deserializeNBT(ism.serializeNBT());
////        assert ism2.availableCapacity() == ism.availableCapacity();
////        assert ism2.getQuantityStored(res2) == ism.getQuantityStored(res2);
////        store4.add(res2, 7, false, null);
////        assert ism2.getQuantityStored(res2) == ism.getQuantityStored(res2) - 7;
//        
//        
//        // IItemHandler tests
//        ItemStorage ihStore = new ItemStorage(CarrierLevel.BOTTOM, PortFunction.CARRIER);
//        ihStore.setCapacity(64);
//        assert ihStore.getSlots() == 1;
//        assert ihStore.getSlotLimit(0) == 64;
//        assert ihStore.extractItem(0, 100, false) == ItemStack.EMPTY;
//        
//        ItemStack stack = res1.sampleItemStack();
//        stack.setCount(32);
//        assert ihStore.insertItem(0, stack, true) == ItemStack.EMPTY;
//        assert ihStore.insertItem(0, stack, false) == ItemStack.EMPTY;
//        assert ihStore.usedCapacity() == 32;
//        assert ihStore.getSlots() == 2;
//        assert ihStore.getSlotLimit(0) == 64;
//        assert ihStore.getSlotLimit(1) == 64 - 32;
//        
//        stack = res2.sampleItemStack();
//        stack.setCount(64);
//        
//        ItemStack returnStack = ihStore.insertItem(1, stack, false);
//        assert returnStack.getCount() == 32;
//        assert returnStack.getItem() == stack.getItem();
//        assert ihStore.usedCapacity() == 64;
//        assert ihStore.getSlots() == 2;
//        assert ihStore.getSlotLimit(0) == 32;
//        assert ihStore.getSlotLimit(1) == 32;
//        
//        stack = res1.sampleItemStack();
//        returnStack = ihStore.extractItem(0, 16, false);
//        assert returnStack.getCount() == 16;
//        assert returnStack.getItem() == stack.getItem();
//        assert ihStore.usedCapacity() == 48;
//        assert ihStore.getSlots() == 3;
//        assert ihStore.getSlotLimit(0) == 32;
//        assert ihStore.getSlotLimit(1) == 48;
//        
//        returnStack = ihStore.extractItem(0, 64, false);
//        assert returnStack.getCount() == 16;
//        assert returnStack.getItem() == stack.getItem();
//        assert ihStore.usedCapacity() == 32;
//        assert ihStore.getSlots() == 2;
//        assert ihStore.getSlotLimit(0) == 64;
//        assert ihStore.getSlotLimit(1) == 32;
//    }
//
//    private boolean confirmStoreAndQuantity(List<StorageWithQuantity<StorageTypeStack>> list, ItemStorage store, long quantity)
//    {
//        for(StorageWithQuantity<StorageTypeStack> entry : list)
//        {
//            if(entry.storage == store && entry.quantity == quantity) return true;
//        }
//        return false;
//    }
//    
//   
//}