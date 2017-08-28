package grondag.hard_science.machines.base;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Hides the mechanics of storage access to ensure consistency of handling.
 */
import grondag.hard_science.Log;
import grondag.hard_science.machines.support.MachineItemBlock;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.wip.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.wip.IStorage;
import grondag.hard_science.simulator.wip.ItemStorage;
import grondag.hard_science.simulator.wip.StorageType;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.items.IItemHandler;

/**
 * For containers with a persisted IStorage.
 * Most of the IStorage stuff that should be needed is implemented in the base class.
 */
public abstract class MachineStorageTileEntity extends MachineContainerTileEntity
{
    /** -1 signifies not loaded */
    private int storageID = -1; 
    
    private IStorage<StorageType.StorageTypeStack> storage;
    
    private void setStorage(IStorage<StorageType.StorageTypeStack> storage)
    {
        this.storage = storage;
        this.storageID = storage == null ? -1 : storage.getId();
        this.markDirty();
    }

    /**
     * Retrieved lazily because simulator may not loaded/running when this TE is deserialized.
     */
    public IStorage<StorageType.StorageTypeStack> getStorage()
    {
        if(this.isRemote()) return null;
        
        if(this.storage == null)
        {
            this.setStorage(this.retrieveOrCreateStorage());
        }
        return this.storage;
    }
    
    @Override
    public IItemHandler getItemHandler()
    {
        return (IItemHandler) this.getStorage();
    }

    @SuppressWarnings("unchecked")
    private IStorage<StorageTypeStack> retrieveOrCreateStorage()
    {
        IStorage<StorageTypeStack> result = null;
        
        if(this.storageID >= 0)
        {
            IStorage<?> s = (this.storageID == 0) ? null : Simulator.INSTANCE.domainManager().storageIndex().get(this.storageID);
            
            if(s == null)
            {
                Log.error("Unable to read storage info for tile entity @ " + this.pos.toString());
                Log.error("Storage will be reinitialized and prior contents, if any, will be lost.");
                Log.error("This may be bug or may be caused by world corruption.");
            }
            else
            {
                result = (IStorage<StorageTypeStack>) s;
            }
        }
        
        if(result == null)
        {
            result = new ItemStorage(null);
            result.setLocation(pos, world);
            Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.addStore(result);
            this.markDirty();
            
            //FIXME: remove
            Log.info("created new storge, id = " + this.storageID);
        }
        else
        {
            //FIXME: remove
            Log.info("retrieved storage, id = " + this.storageID);
        }
        return result;
    }

    
    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        if(this.isRemote()) return;
        this.setStorage(null);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(this.isRemote()) return;
        
        this.storageID = compound.getInteger("storageID");
        
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if(this.isRemote()) return compound;
        
        compound.setInteger("storageID", this.storageID);
        
        return compound;
    }

    @Override
    public void restoreStateFromStackAndReconnect(ItemStack stack, NBTTagCompound serverSideTag)
    {
        if(this.isRemote()) return;
       super.restoreStateFromStackAndReconnect(stack, serverSideTag);
       if(serverSideTag == null || serverSideTag.hasNoTags()) return;
     
       IStorage<StorageType.StorageTypeStack> storage = new ItemStorage(serverSideTag.getCompoundTag("storage"));
       storage.setLocation(this.pos, this.world);
       //force new ID
       storage.setId(0);
       Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.addStore(storage);
       
       this.setStorage(storage);
       
       //FIXME: remove
       Log.info("restoreStateFromStackAndReconnect id=" + this.storageID);
       
    }
    
    @Override
    public void saveStateInStack(ItemStack stack, NBTTagCompound serverSideTag)
    {
        if(this.isRemote()) return;
        super.saveStateInStack(stack, serverSideTag);
        
        IStorage<StorageTypeStack> store = this.getStorage();
     
        
        if(this.storage.usedCapacity() == 0) return;
        
        if(store != null) serverSideTag.setTag("storage", store.serializeNBT());
        
        NBTTagCompound displayTag = stack.getOrCreateSubCompound("display");
            
        NBTTagList loreTag = new NBTTagList(); 

        List<AbstractResourceWithQuantity<StorageTypeStack>> items = this.storage.find(storage.storageType().MATCH_ANY).stream()
                .sorted(AbstractResourceWithQuantity.SORT_BY_QTY_DESC).collect(Collectors.toList());

        if(!items.isEmpty())
        {
            long printedQty = 0;
            int printedCount = 0;
            for(AbstractResourceWithQuantity<StorageTypeStack> item : items)
            {
                loreTag.appendTag(new NBTTagString(item.toString()));
                printedQty += item.getQuantity();
                if(++printedCount == 10)
                {
                    //FIXME: localize
                    loreTag.appendTag(new NBTTagString(String.format("...plus %,d of %d other items", 
                            this.storage.usedCapacity() - printedQty, items.size() - printedCount)));
                    break;
                }
            }
            
            stack.setItemDamage(Math.max(1, (int) (MachineItemBlock.MAX_DAMAGE * this.storage.availableCapacity() / this.storage.getCapacity())));
        }
        displayTag.setTag("Lore", loreTag);
    }
    
    @Override
    public void disconnect()
    {
        if(this.isRemote()) return;
        Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.removeStore(this.storage);
        
        //FIXME: remove
        Log.info("disconnect id=" + this.storageID);
        
        this.setStorage(null);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
         return super.canInteractWith(playerIn) && this.getStorage() != null;
    }
}