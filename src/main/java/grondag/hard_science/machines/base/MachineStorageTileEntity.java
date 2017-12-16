package grondag.hard_science.machines.base;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.ItemStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

/**
 * For containers with a persisted IStorage.
 * Most of the IStorage stuff that should be needed is implemented in the base class.
 */
public abstract class MachineStorageTileEntity extends MachineContainerTileEntity
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
   
    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    private IStorage<StorageType.StorageTypeStack> storage;
    
    /** 
     * On deserialization, server contents tag is put here if found.
     * If we are restoring from item stack, will not find storage ID
     * and can use this to reconstitue our storage.  Wiped after used
     * or determined no longer needed.
     */
    private NBTTagCompound loadedStorageNBT = null;
    
    private void setStorage(IStorage<StorageType.StorageTypeStack> storage)
    {
        this.storage = storage;
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
        
        if(this.getId() > 0)
        {
            result = (IStorage<StorageTypeStack>) DeviceManager.getDevice(this.getId());
        }
        
        if(result == null)
        {
            if(this.loadedStorageNBT != null && !this.loadedStorageNBT.hasNoTags())
            {
                result = new ItemStorage();
                result.deserializeID(this.loadedStorageNBT);
                result.setLocation(this.pos, this.world);
            }
        }
        
        this.loadedStorageNBT = null;
        
        if(result == null)
        {
            result = new ItemStorage();
            result.setLocation(pos, world);
            DomainManager.INSTANCE.defaultDomain().itemStorage.addStore(result);
        }
        return result;
    }

    
    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        if(this.isRemote()) return;
        this.storage = null;
    }
    
    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        
        // can't rely on world here, because won't be set yet on reload
        if(!this.isRemote())
        {
            NBTTagCompound serverTag = getServerTag(compound);
            this.loadedStorageNBT = serverTag.getCompoundTag(ModNBTTag.STORAGE_CONTENTS).copy();
        }
    }

    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        
        if(!this.world.isRemote)
        {
            NBTTagCompound serverTag = getServerTag(compound);
            // save stored items
            IStorage<StorageTypeStack> store = this.getStorage();
            if(store != null)
            {
                serverTag.setTag(ModNBTTag.STORAGE_CONTENTS, store.serializeNBT());
            }
        }
    }

    @Override
    public void reconnect()
    {
        if(this.isRemote()) return;
     
        this.getStorage();
    }
    
    @Override
    public void disconnect()
    {
        if(this.isRemote()) return;
        
        if(this.storage != null)
        {
            DomainManager.INSTANCE.defaultDomain().itemStorage.removeStore(this.storage);
            this.setStorage(null);
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
         return super.canInteractWith(playerIn) && this.getStorage() != null;
    }
}