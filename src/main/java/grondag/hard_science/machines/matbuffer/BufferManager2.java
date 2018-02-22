package grondag.hard_science.machines.matbuffer;

import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.simulator.ISimulationTickable;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.BulkContainer;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.FluidContainer;
import grondag.hard_science.simulator.storage.ItemContainer;
import grondag.hard_science.simulator.storage.ResourceContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;

public class BufferManager2 implements IReadWriteNBT, IItemHandler, IDeviceComponent, ISimulationTickable
{
    private final IDevice owner;
    private final ItemContainer itemInput;
    private final ItemContainer itemOutput;
    private final ImmutableMap<FluidResource, FluidContainer> fluidInputs;
    private final ImmutableMap<FluidResource, FluidContainer> fluidOutputs;
    private final ImmutableMap<BulkBufferPurpose, BulkContainer> bulkBuffers;
    
    
    public BufferManager2(IDevice owner, ResourceContainer<?>... containers)
    {
        this.owner = owner;
        ItemContainer itemInput = null;
        ItemContainer itemOutput = null;
        
        ImmutableMap.Builder<FluidResource, FluidContainer> fluidInputs = ImmutableMap.builder();
        ImmutableMap.Builder<FluidResource, FluidContainer> fluidOutputs = ImmutableMap.builder();
        ImmutableMap.Builder<BulkBufferPurpose, BulkContainer> bulkBuffers = ImmutableMap.builder();

        for(ResourceContainer<?> c : containers)
        {
            switch(c.storageType().enumType)
            {
            case FLUID:
                if(c.containerUsage() == ContainerUsage.BUFFER_IN)
                    fluidInputs.put((FluidResource)c.resource(), (FluidContainer) c);
                else if(c.containerUsage() == ContainerUsage.BUFFER_OUT)
                    fluidOutputs.put((FluidResource)c.resource(), (FluidContainer) c);
                else
                    assert false : "Invalid buffer construction";
                break;
                
            case ITEM:
                if(c.containerUsage() == ContainerUsage.BUFFER_IN && itemInput == null)
                    itemInput = (ItemContainer)c;
                else if(c.containerUsage() == ContainerUsage.BUFFER_OUT && itemOutput == null)
                    itemOutput = (ItemContainer) c;
                else
                    assert false : "Invalid buffer construction";
                break;
                
            case PRIVATE:
                if(c.containerUsage() == ContainerUsage.BUFFER_ISOLATED)
                    bulkBuffers.put(((BulkContainer)c).bufferPurpose, (BulkContainer) c);
                else
                    assert false : "Invalid buffer construction";
                break;
                
            case POWER:
            default:
                assert false : "Invalid buffer construction";
                break;
            
            }
        }
        
        if(itemInput == null)
        {
            itemInput = new ItemContainer(owner, ContainerUsage.BUFFER_IN);
            itemInput.setCapacity(0);
        }
        
        if(itemOutput == null)
        {
            itemOutput = new ItemContainer(owner, ContainerUsage.BUFFER_OUT);
            itemOutput.setCapacity(0);
        }
        
        this.itemInput = itemInput;
        this.itemOutput = itemOutput;
        this.fluidInputs = fluidInputs.build();
        this.fluidOutputs = fluidOutputs.build();
        this.bulkBuffers = bulkBuffers.build();
    }

    public ItemContainer itemInput() 
    {
        return this.itemInput;
    }
    
    public ItemContainer itemOutput()
    {
        return this.itemOutput;
    }
    
    public ImmutableMap<FluidResource, FluidContainer> fluidInputs()
    {
        return this.fluidInputs;
    }
    
    public ImmutableMap<FluidResource, FluidContainer> fluidOutputs()
    {
        return this.fluidOutputs;
    }
    
    public ImmutableMap<BulkBufferPurpose, BulkContainer> bulkBuffers()
    {
        return this.bulkBuffers;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(tag.hasKey(ModNBTTag.BUFFER_ITEMS_IN))
            this.itemInput.deserializeNBT(tag.getCompoundTag(ModNBTTag.BUFFER_ITEMS_IN));

        if(tag.hasKey(ModNBTTag.BUFFER_ITEMS_OUT))
            this.itemOutput.deserializeNBT(tag.getCompoundTag(ModNBTTag.BUFFER_ITEMS_OUT));
        
        if(tag.hasKey(ModNBTTag.BUFFER_FLUIDS_IN))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.BUFFER_FLUIDS_IN, 10);
            list.forEach(t -> 
            {
                FluidResource r = (FluidResource) StorageType.FLUID.fromNBT((NBTTagCompound) t);
                FluidContainer c = this.fluidInputs.get(r);
                if(c == null)
                    assert false : "Fluid container not found during buffer manager deserialization";
                else
                    c.deserializeNBT((NBTTagCompound)t);
            });
        }
        else assert this.fluidInputs.isEmpty() : "Invalid fluid buffer deserialization";
        
        if(tag.hasKey(ModNBTTag.BUFFER_FLUIDS_OUT))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.BUFFER_FLUIDS_OUT, 10);
            list.forEach(t -> 
            {
                FluidResource r = (FluidResource) StorageType.FLUID.fromNBT((NBTTagCompound) t);
                FluidContainer c = this.fluidOutputs.get(r);
                if(c == null)
                    assert false : "Fluid container not found during buffer manager deserialization";
                else
                    c.deserializeNBT((NBTTagCompound)t);
            });
        }
        else assert this.fluidOutputs.isEmpty() : "Invalid fluid buffer deserialization";
        
        if(tag.hasKey(ModNBTTag.BUFFER_BULK))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.BUFFER_FLUIDS_OUT, 10);
            list.forEach(t -> 
            {
                BulkBufferPurpose p = Useful.safeEnumFromTag(tag, ModNBTTag.BUFFER_PURPOSE, BulkBufferPurpose.INVALID);
                BulkContainer c = this.bulkBuffers.get(p);
                if(c == null)
                    assert false : "Fluid container not found during buffer manager deserialization";
                else
                    c.deserializeNBT((NBTTagCompound)t);
            });
        }
        else assert this.bulkBuffers.isEmpty() : "Invalid bulk buffer deserialization";
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setTag(ModNBTTag.BUFFER_ITEMS_IN, this.itemInput.serializeNBT());
        tag.setTag(ModNBTTag.BUFFER_ITEMS_OUT, this.itemOutput.serializeNBT());
        
        if(!this.fluidInputs.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            for(Entry<FluidResource, FluidContainer> e : this.fluidInputs.entrySet())
            {
                NBTTagCompound t = StorageType.FLUID.toNBT(e.getKey());
                e.getValue().serializeNBT(t);
                list.appendTag(t);
            }
            tag.setTag(ModNBTTag.BUFFER_FLUIDS_IN, list);
        }
        
        if(!this.fluidOutputs.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            for(Entry<FluidResource, FluidContainer> e : this.fluidOutputs.entrySet())
            {
                NBTTagCompound t = StorageType.FLUID.toNBT(e.getKey());
                e.getValue().serializeNBT(t);
                list.appendTag(t);
            }
            tag.setTag(ModNBTTag.BUFFER_FLUIDS_OUT, list);
        }
        
        if(!this.bulkBuffers.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            for(Entry<BulkBufferPurpose, BulkContainer> e : this.bulkBuffers.entrySet())
            {
                NBTTagCompound t = e.getValue().serializeNBT();
                Useful.saveEnumToTag(t, ModNBTTag.BUFFER_PURPOSE, e.getKey());
                list.appendTag(t);
            }
            tag.setTag(ModNBTTag.BUFFER_BULK, list);
        }        
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


    public long[] serializeToArray()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasFailureCauseClientSideOnly()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public BufferDelegate2 getBuffer(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int bufferCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public BulkContainer bufferHDPE()
    {
        return null;
    }

    public DemandManager2 demandManager()
    {
        // TODO Auto-generated method stub
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
