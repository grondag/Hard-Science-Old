package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.ISimulationTickable;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.FluidContainer;
import grondag.hard_science.simulator.storage.ItemContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class BufferManager implements IReadWriteNBT, IItemHandler, IDeviceComponent, ISimulationTickable
{
    private final IDevice owner;
    private final ItemContainer itemInput;
    private final ItemContainer itemOutput;
    private final FluidContainer fluidInput;
    private final FluidContainer fluidOutput;
    
    public BufferManager(
            IDevice owner,
            long itemInputSize,
            IResourcePredicate<StorageTypeStack> itemInputPredicate,
            long itemOutputSize,
            long fluidInputSize,
            IResourcePredicate<StorageTypeFluid> fluidInputPredicate,
            long fluidOutputSize
            )
    {
        this.owner = owner;
        
        if(itemInputSize == 0)
        {
            this.itemInput = null;
        }
        else
        {
            this.itemInput = new ItemContainer(owner, ContainerUsage.PRIVATE_BUFFER_IN, Integer.MAX_VALUE);
            this.itemInput.setCapacity(itemInputSize);
            this.itemInput.setContentPredicate(itemInputPredicate);
        }
        
        if(itemOutputSize == 0)
        {
            this.itemOutput = null;
        }
        else
        {
            this.itemOutput = new ItemContainer(owner, ContainerUsage.PUBLIC_BUFFER_OUT, Integer.MAX_VALUE);
            this.itemOutput.setCapacity(itemOutputSize);
        }
        
        if(fluidInputSize == 0)
        {
            this.fluidInput = null;
        }
        else
        {
            this.fluidInput = new FluidContainer(owner, ContainerUsage.PRIVATE_BUFFER_IN, Integer.MAX_VALUE);
            this.fluidInput.setCapacity(fluidInputSize);
            this.fluidInput.setContentPredicate(fluidInputPredicate);
        }
        
        if(fluidOutputSize == 0)
        {
            this.fluidOutput = null;
        }
        else
        {
            this.fluidOutput = new FluidContainer(owner, ContainerUsage.PUBLIC_BUFFER_OUT, Integer.MAX_VALUE);
            this.fluidOutput.setCapacity(fluidOutputSize);
        }
    }

    public ItemContainer itemInput() 
    {
        return this.itemInput;
    }
    
    public ItemContainer itemOutput()
    {
        return this.itemOutput;
    }
    
    public FluidContainer fluidInput()
    {
        return this.fluidInput;
    }
    
    public FluidContainer fluidOutput()
    {
        return this.fluidOutput;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(this.itemInput != null && tag.hasKey(ModNBTTag.BUFFER_ITEMS_IN))
            this.itemInput.deserializeNBT(tag.getCompoundTag(ModNBTTag.BUFFER_ITEMS_IN));

        if(this.itemOutput != null && tag.hasKey(ModNBTTag.BUFFER_ITEMS_OUT))
            this.itemOutput.deserializeNBT(tag.getCompoundTag(ModNBTTag.BUFFER_ITEMS_OUT));

        if(this.fluidInput != null && tag.hasKey(ModNBTTag.BUFFER_FLUIDS_IN))
            this.fluidInput.deserializeNBT(tag.getCompoundTag(ModNBTTag.BUFFER_FLUIDS_IN));

        if(this.fluidOutput != null && tag.hasKey(ModNBTTag.BUFFER_FLUIDS_OUT))
            this.fluidOutput.deserializeNBT(tag.getCompoundTag(ModNBTTag.BUFFER_FLUIDS_OUT));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(this.itemInput != null)
            tag.setTag(ModNBTTag.BUFFER_ITEMS_IN, this.itemInput.serializeNBT());
        if(this.itemOutput != null)
            tag.setTag(ModNBTTag.BUFFER_ITEMS_OUT, this.itemOutput.serializeNBT());
        if(this.fluidInput != null)
            tag.setTag(ModNBTTag.BUFFER_FLUIDS_IN, this.fluidInput.serializeNBT());
        if(this.fluidOutput != null)
            tag.setTag(ModNBTTag.BUFFER_FLUIDS_OUT, this.fluidOutput.serializeNBT());
    }

    @Override
    public int getSlots()
    {
        return this.inSlots() + this.outSlots();
    }

    private int inSlots()
    {
        return this.itemInput == null ? 0 : this.itemInput.getSlots();
    }
    
    private int outSlots()
    {
        return this.itemOutput == null ? 0 : this.itemOutput.getSlots();
    }
    
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        int inLength = inSlots();
        int outLength = outSlots();
        
        if(slot < inLength)
        {
            return this.itemInput.getStackInSlot(slot);
        }
        else if(slot < inLength + outLength)
        {
            return this.itemOutput.getStackInSlot(slot - inLength);
        }
        else return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if(slot < inSlots())
        {
            return this.itemInput.insertItem(slot, stack, simulate);
        }
        else 
        {
            return stack;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(this.itemOutput == null) return ItemStack.EMPTY;
        
        int inLength = inSlots();
        if(slot >= inLength)
        {
            return this.itemOutput.extractItem(slot - inLength, amount, simulate);
        }
        else return ItemStack.EMPTY;    
    }

    @Override
    public int getSlotLimit(int slot)
    {
        int inLength = inSlots();
        int outLength = outSlots();
        
        if(slot < inLength)
        {
            return this.itemInput.getSlotLimit(slot);
        }
        else if(slot < inLength + outLength)
        {
            return this.itemOutput.getSlotLimit(slot - inLength);
        }
        else return 0;
    }

    @Override
    public void onConnect()
    {
        IDeviceComponent.super.onConnect();
        if(this.itemInput != null) this.itemInput.onConnect();
        if(this.itemOutput != null) this.itemOutput.onConnect();
        if(this.fluidInput != null) this.fluidInput.onConnect();
        if(this.fluidOutput != null) this.fluidOutput.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        IDeviceComponent.super.onDisconnect();
        if(this.itemInput != null) this.itemInput.onDisconnect();
        if(this.itemOutput != null) this.itemOutput.onDisconnect();
        if(this.fluidInput != null) this.fluidInput.onDisconnect();
        if(this.fluidOutput != null) this.fluidOutput.onDisconnect();
    }

    public long[] serializeToArray()
    {
        //TODO: stub
        return new long[0];
    }

    public boolean hasFailureCauseClientSideOnly()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Deprecated
    public BufferDelegate getBuffer(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Deprecated
    public int bufferCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public FluidContainer bufferHDPE()
    {
        //TODO stub
        return null;
    }

    public void deserializeFromArray(long[] materialBufferData)
    {
        // TODO Auto-generated method stub
        
    }

    public void forgiveAll()
    {
        // TODO Auto-generated method stub
        
    }

    public boolean canRestockAny()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean restock(IItemHandler capability)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }
}
