package grondag.hard_science.simulator.base;


import org.junit.Test;

import grondag.hard_science.Log;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceCache;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.storage.StorageWithQuantity;
import grondag.hard_science.simulator.storage.jobs.Job;
import grondag.hard_science.simulator.storage.jobs.RequestStatus;
import grondag.hard_science.simulator.storage.jobs.tasks.DeliveryTask;
import grondag.hard_science.simulator.storage.jobs.tasks.SimpleProcurementTask;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** 
 * End-to-end acceptance test for 
 * item storage, demand management, procurement systems.
 * Because of run-time requirements has to be run within game.
 * And must run with -ea vm argument.
 * @author grondag
 *
 */
public class ItemSystemTest
{
    Domain domain;
    ItemStorage store1;
    ItemStorage store2;
    
    ItemResource beef;
    ItemResource ironIngot;
    ItemResource ironNugget;
    ItemResource ironBlock;
    ItemResource superBlock1;
    ItemResource superBlock2;

    private void setup()
    {
        DomainManager.INSTANCE.unload();
        DomainManager.INSTANCE.loadNew();
        
        domain = DomainManager.INSTANCE.createDomain();
        
        store1 = new ItemStorage(null);
        domain.itemStorage.addStore(store1);
        
        store2 = new ItemStorage(null);
        domain.itemStorage.addStore(store2);
        
        beef = ItemResourceCache.fromStack(Items.BEEF.getDefaultInstance());
        ironIngot = ItemResourceCache.fromStack(Items.IRON_INGOT.getDefaultInstance());
        ironNugget = ItemResourceCache.fromStack(Items.IRON_NUGGET.getDefaultInstance());
        ironBlock = ItemResourceCache.fromStack(Item.getItemFromBlock(Blocks.IRON_BLOCK).getDefaultInstance());
        superBlock1 = ItemResourceCache.fromStack(this.makeSuperModelStack(0, Textures.BIGTEX_ASPHALT));
        superBlock2 = ItemResourceCache.fromStack(this.makeSuperModelStack(1, Textures.BIGTEX_MARBLE));
    }
    
    @Test
    public void test()
    {
        this.testDomainQueries();
        this.testUserExtract();
        
        // superblock fab mock up
        // crafting producer mock up request
        // crafting with damaged items mock up
        // nugget/ingot/block mock up
        // serialization
            // allocations
            // wip reclaim mock up
        // procurement request fullfill from inventory
        // procurement request break allocation
        
        // monitoring
        // global extract
        
        // concurrency/load tests
        
        // clean up
        DomainManager.INSTANCE.unload();
        
        Log.info("Item System Tests Complete");
        
    }

    /**
     * Test user extraction scenarios
     */
    private void testUserExtract()
    {
        this.setup();
        
        SimpleProcurementTask<StorageTypeStack> procureTask 
            = new SimpleProcurementTask<StorageTypeStack>(beef, 10);
        DeliveryTask<StorageTypeStack> deliveryTask
            = new DeliveryTask<StorageTypeStack>(procureTask);
    
        Job job = new Job();
        job.addTask(procureTask);
        job.addTask(deliveryTask);
        domain.jobManager.addJob(job);
    
        // wait for job to be initialized
        try { Thread.sleep(250); } catch (InterruptedException e) {}
        
        // procurement tasks make self active automatically
        // unless they have predecessors
        assert procureTask.getStatus() == RequestStatus.ACTIVE;
        assert deliveryTask.getStatus() == RequestStatus.WAITING;
        
        // should fulfill the request
        store1.add(beef, 10, false, null);
        
        // wait for thread pool to catch up
        try { Thread.sleep(250); } catch (InterruptedException e) {}
        
        assert procureTask.getStatus() == RequestStatus.COMPLETE;
        assert deliveryTask.getStatus() == RequestStatus.READY;
        
        // should have no available and 10 allocated in storage
        assert domain.itemStorage.getQuantityAvailable(beef) == 0;
        assert domain.itemStorage.getQuantityAllocated(beef) == 10;
        assert domain.itemStorage.getQuantityStored(beef) == 10;
        
        deliveryTask.complete();
        
        // should have none now
        assert domain.itemStorage.getQuantityAvailable(beef) == 0;
        assert domain.itemStorage.getQuantityAllocated(beef) == 0;
        assert domain.itemStorage.getQuantityStored(beef) == 0;        
    }
    
    /**
     * Test wildcard requests
     */

    /**
     * Test types of queries / scenarios that would be used
     * for domain-level inventory queries.
     */
    private void testDomainQueries()
    {
        this.setup();
        
        // put some resources in stores directly..
        
        assert store1.add(beef, 100, false, null) == 100;
        
        // make sure simulation, capacity limits work
        assert store1.add(beef, 2000, true, null) == 1900;
        
        // do some ins and outs
        assert store1.add(beef, 150, false, null) == 150;
        assert store1.takeUpTo(beef, 50, false, null) == 50;
        
        // confirm store has correct total
        assert store1.getQuantityStored(beef) == 200;
        
        // put some stuff in second store
        store2.add(ironIngot, 300, false, null);
        
        // check for strangeness
        assert store1.getQuantityStored(ironIngot) == 0;
        
        // should show up in domain searches
        {
            StorageWithQuantity<StorageTypeStack> swq 
                 = domain.itemStorage.findStorageWithQuantity(beef).get(0);
            assert swq.quantity == 200;
            assert swq.storage == store1;
            assert domain.itemStorage.findQuantityAvailable(beef).get(0).getQuantity() == 200;
            assert domain.itemStorage.getQuantityStored(beef) == 200;
        }
        
        {
           StorageWithQuantity<StorageTypeStack> swq 
                = domain.itemStorage.findStorageWithQuantity(ironIngot).get(0);
           assert swq.quantity == 300;
           assert swq.storage == store2;
           assert domain.itemStorage.findQuantityAvailable(ironIngot).get(0).getQuantity() == 300;
           assert domain.itemStorage.getQuantityStored(ironIngot) == 300;
        }
    }
    
    private ItemStack makeSuperModelStack(int meta, TexturePallette tex)
    {
        ModelState modelState = new ModelState();
        modelState.setTexture(PaintLayer.BASE, tex);
        modelState.setSpecies(meta);
        
        SuperModelBlock smb = ModSuperModelBlocks.findAppropriateSuperModelBlock(BlockSubstance.DURASTONE, modelState);
        
        ItemStack result = smb.getSubItems().get(meta).copy();
        
        PlacementItem.setStackModelState(result, modelState);
        PlacementItem.setStackSubstance(result, BlockSubstance.DURASTONE);
        
        return result;
    }
}