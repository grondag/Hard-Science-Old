// need to redo if keeping

//package grondag.hard_science.simulator.base;
//
//
//import org.junit.Test;
//
//import grondag.hard_science.simulator.resource.ItemResource;
//import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
//import grondag.hard_science.simulator.storage.ItemStorage;
//import grondag.hard_science.simulator.storage.StorageResourceManager;
//import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
//import grondag.hard_science.simulator.transport.endpoint.PortType;
//import net.minecraft.init.Items;
//
//public class StorageResourceManagerTest
//{
//
//    @Test
//    public void test()
//    {
//        ItemResource res = ItemResource.fromStack(Items.COOKED_CHICKEN.getDefaultInstance());
//        
//        ItemStorage store1 = new ItemStorage(CarrierLevel.BOTTOM, PortType.CARRIER);
//        ItemStorage store2 = new ItemStorage(CarrierLevel.BOTTOM, PortType.CARRIER);
//        
////        BlockProcurementTask task = new BlockProcurementTask();
//        
//        store1.add(res, 100, false, null);
//        store2.add(res, 50, false, null);
//        
//        StorageResourceManager<StorageTypeStack> subject 
//            = new StorageResourceManager<StorageTypeStack>(res, store1, 0, null);
//        assert subject.quantityStored() == 100;
//        assert subject.quantityAllocated() == 0;
//        assert subject.quantityAvailable() == 100;
//        
//        subject.notifyAdded(store2, 50, null);
//        
//        
//    }
//
//}