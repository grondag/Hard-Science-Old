package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.Arrays;

import grondag.adversity.library.PackedBlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;



public abstract class BlockEventList
{
    private static final int CAPACITY_INCREMENT = 0x400;
    
    private int capacity = CAPACITY_INCREMENT;
    
    private int eventData[] = new int[capacity];
    
    private int nextEmptyIndex = 0;
    
    private final String nbtTagName;
    
    protected abstract void processEvent(int x, int y, int z, int amount);
    
    protected BlockEventList(String nbtTagName)
    {
        this.nbtTagName = nbtTagName;
    }
    
    public void addEvent(BlockPos pos, int amount)
    {
        this.addEvent(pos.getX(), pos.getY(), pos.getZ(), amount);
    }
    
    public void addEvent(long packedBlockPos, int amount)
    {
        this.addEvent(PackedBlockPos.getX(packedBlockPos), PackedBlockPos.getY(packedBlockPos), PackedBlockPos.getZ(packedBlockPos), amount);
    }
    
    public void addEvent(int x, int y, int z, int amount)
    {
        synchronized(this)
        {
            if(nextEmptyIndex == capacity) increaseCapacity();
            
            eventData[nextEmptyIndex++] = x;
            eventData[nextEmptyIndex++] = y;
            eventData[nextEmptyIndex++] = z;
            eventData[nextEmptyIndex++] = amount;
        }
    }
    
    public void processAllEvents()
    {
        synchronized(this)
        {
            if(this.nextEmptyIndex == 0) return;
            
            int i = 0;
            
            while(i < this.nextEmptyIndex)
            {
                this.processEvent(eventData[i++], eventData[i++], eventData[i++], eventData[i++]);
            }
            
            this.nextEmptyIndex = 0;
        }
    }
    
    public int size()
    {
        return this.capacity / 4;
    }
    
    private void increaseCapacity()
    {
        capacity += CAPACITY_INCREMENT;
        this.eventData = Arrays.copyOf(this.eventData, capacity);
    }
    
    public void writeNBT(NBTTagCompound nbt)
    {
        synchronized(this)
        {
            if(this.nextEmptyIndex == 0) return;
            
            nbt.setIntArray(this.nbtTagName, Arrays.copyOf(this.eventData, this.nextEmptyIndex));
        }
    }
    
    public void readNBT(NBTTagCompound nbt)
    {
        synchronized(this)
        {
            int[] nbtData = nbt.getIntArray(this.nbtTagName);
            
            if(nbtData == null || nbtData.length == 0)
            {
                this.nextEmptyIndex = 0;
                this.capacity = CAPACITY_INCREMENT;
                this.eventData = new int[CAPACITY_INCREMENT];
            }
            else
            {
                this.nextEmptyIndex = nbtData.length;
                this.capacity = (((nextEmptyIndex - 1) / CAPACITY_INCREMENT) + 1) * CAPACITY_INCREMENT;
                this.eventData = Arrays.copyOf(nbtData, this.capacity);
            }
        }
    }
}
