package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class BufferManager2 implements IReadWriteNBT, IItemHandler
{

    public BufferManager2(VolumetricBufferSpec[] bufferSpecs)
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getSlots()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean restock(IItemHandler capability)
    {
        // TODO Auto-generated method stub
        return false;
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

    public BufferDelegate2 bufferHDPE()
    {
        // TODO Auto-generated method stub
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

}
