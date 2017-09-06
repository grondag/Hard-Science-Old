package grondag.hard_science.machines.base;

/**
 * Hides the mechanics of storage access to ensure consistency of handling.
 */
import grondag.hard_science.Log;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.wip.IStorage;
import grondag.hard_science.simulator.wip.ItemStorage;
import grondag.hard_science.simulator.wip.StorageType;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
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
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        this.storageID = getServerTag(compound).getInteger("HS_STRID");
        
    }

    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        getServerTag(compound).setInteger("HS_STRID", this.storageID);
    }

    @Override
    public void reconnect()
    {
        if(this.isRemote()) return;
     
        IStorage<StorageTypeStack> store = this.getStorage();
        
        //FIXME: handle duplication via pickblock in create mode
        if(store != null)
        {
            Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.addStore(this.getStorage());

            //FIXME: remove
            Log.info("reconnect storage id=" + this.storageID);
        }
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