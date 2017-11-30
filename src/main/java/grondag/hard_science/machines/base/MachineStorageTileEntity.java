package grondag.hard_science.machines.base;

/**
 * Hides the mechanics of storage access to ensure consistency of handling.
 */
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.superblock.block.SuperTileEntity;
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
    /** -1 signifies not loaded */
    private int storageID = -1; 
    
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
            IStorage<?> s = (this.storageID == 0) ? null : DomainManager.INSTANCE.assignedNumbersAuthority().storageIndex().get(this.storageID);
            
            if(s == null)
            {
                if(this.loadedStorageNBT == null)
                {
                    Log.error("Unable to read storage info for tile entity @ " + this.pos.toString());
                    Log.error("Storage will be reinitialized and prior contents, if any, will be lost.");
                    Log.error("This may be bug or may be caused by world corruption.");
                }
                else
                {
                    result = new ItemStorage(this.loadedStorageNBT);
                    result.setLocation(this.pos, this.world);
                    this.getDomain().ITEM_STORAGE.addStore(result);
                }
            }
            else
            {
                result = (IStorage<StorageTypeStack>) s;
            }
        }
        
        this.loadedStorageNBT = null;
        
        if(result == null)
        {
            result = new ItemStorage(null);
            result.setLocation(pos, world);
            DomainManager.INSTANCE.defaultDomain().ITEM_STORAGE.addStore(result);
            
            //FIXME: remove
            Log.info("created new storage, id = " + result.getId());
        }
        else
        {
            //FIXME: remove
            Log.info("retrieved storage, id = " + result.getId());
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
        if(!this.world.isRemote)
        {
            NBTTagCompound serverTag = getServerTag(compound);
            this.storageID = serverTag.getInteger(ModNBTTag.STORAGE_ID);
            this.loadedStorageNBT = serverTag.getCompoundTag(ModNBTTag.STORAGE_CONTENTS);
        }
    }

    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        
        if(!this.world.isRemote)
        {
            NBTTagCompound serverTag = getServerTag(compound);
            serverTag.setInteger(ModNBTTag.STORAGE_ID, this.storageID);
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
     
        IStorage<StorageTypeStack> store = this.getStorage();
        
        //FIXME: handle duplication via pickblock in create mode
        if(store != null)
        {
            DomainManager.INSTANCE.defaultDomain().ITEM_STORAGE.addStore(this.getStorage());

            //FIXME: remove
            Log.info("reconnect storage id=" + this.storageID);
        }
    }
    
    @Override
    public void disconnect()
    {
        if(this.isRemote()) return;
        DomainManager.INSTANCE.defaultDomain().ITEM_STORAGE.removeStore(this.storage);
        
        //FIXME: remove
        Log.info("disconnect id=" + this.storageID);
        
        this.setStorage(null);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
         return super.canInteractWith(playerIn) && this.getStorage() != null;
    }
}