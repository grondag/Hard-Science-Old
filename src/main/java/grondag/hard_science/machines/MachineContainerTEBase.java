package grondag.hard_science.machines;

import java.util.List;
import java.util.stream.Collectors;

import grondag.hard_science.Log;
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
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MachineContainerTEBase extends MachineTileEntity
{
    /** -1 signifies not loaded */
    
    private int storageID = -1; 
    private IStorage<StorageType.StorageTypeStack> storage;

    private ItemStackHandler itemStackHandler = new ItemStackHandler(1)
    {
        @Override
        protected void onContentsChanged(int slot) 
        {
            MachineContainerTEBase.this.markDirty();
        }
    };
    
    
    public boolean canInteractWith(EntityPlayer playerIn)
    {
         return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D
                 && this.storage() != null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
                
        if(this.world == null)
        {
            if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return; ;
        }
        else if(world.isRemote) return;
        
        this.storageID = compound.getInteger("storageID");
        
        //FIXME: remove
        Log.info("TE NBT read id = " + this.storageID);
    }

    /**
     * Retrieved lazily because simulator may not loaded/running when this TE is deserialized.
     */
    @SuppressWarnings("unchecked")
    public IStorage<StorageType.StorageTypeStack> storage()
    {
        if(this.world.isRemote) return null;
        
        if(this.storage == null)
        {
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
                    this.storage = (IStorage<StorageTypeStack>) s;
                }
            }
            
            if(this.storage == null)
            {
                this.storage = new ItemStorage(null);
                this.storage.setLocation(pos, world);
                Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.addStore(this.storage);
                this.storageID = this.storage.getId();
                this.markDirty();
                //FIXME: remove
                Log.info("created new storge, id = " + this.storageID);
            }
            else
            {
                //FIXME: remove
                Log.info("retrieved storage, id = " + this.storageID);
            }
        }
        return this.storage;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        
        //FIXME: remove
        Log.info("TE NBT write id = " + this.storageID);
        
        compound.setInteger("storageID", this.storageID);
        
        return compound;
    }


    @Override
    public void restoreStateFromStackAndReconnect(ItemStack stack)
    {
       if(this.world == null || this.world.isRemote) return;
       NBTTagCompound tag = stack.getTagCompound();
       if(tag == null) return;
       tag = tag.getCompoundTag(MachineItemBlock.NBT_SERVER_SIDE_TAG);
       if(!tag.hasNoTags())
       {
           this.storage = new ItemStorage(tag);
           this.storage.setLocation(this.pos, this.world);
           //force new ID
           this.storage.setId(0);
           Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.addStore(this.storage);
           this.storageID = this.storage.getId();
           
           //FIXME: remove
           Log.info("restoreStateFromStackAndReconnect id=" + this.storageID);
           this.markDirty();
       }
    }

    @Override
    public void saveStateInStack(ItemStack stack)
    {
        if(this.world == null || this.world.isRemote) return;
        IStorage<StorageTypeStack> store = this.storage();
        //FIXME: remove
        Log.info("saveStateInStack id=" + this.storageID);
        
        if(this.storage.usedCapacity() == 0) return;
        
        if(store != null) stack.setTagInfo(MachineItemBlock.NBT_SERVER_SIDE_TAG, store.serializeNBT());
        
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
        if(this.world == null || this.world.isRemote) return;
        Simulator.INSTANCE.domainManager().defaultDomain().ITEM_STORAGE.removeStore(this.storage);
        
        //FIXME: remove
        Log.info("disconnect id=" + this.storageID);
        
        this.storage = null;
        this.storageID = -1;
        this.markDirty();
        
    }
}