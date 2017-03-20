package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.Arrays;

import grondag.adversity.library.PackedBlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;



public abstract class BlockEventList
{
    private static final int CAPACITY_INCREMENT = 0x500;
    
    private int capacity = CAPACITY_INCREMENT;
    
    private int eventData[] = new int[capacity];
    
    private int nextEmptyIndex = 0;
    
    private final String nbtTagName;
    
    private final int maxRetries;
    
    /** 
     * Return true if event was processed successfully and can be removed. 
     * Return false if should retry.
     */
    protected abstract boolean processEvent(int x, int y, int z, int amount);
    
    protected BlockEventList(String nbtTagName, int maxRetries)
    {
        this.nbtTagName = nbtTagName;
        this.maxRetries = maxRetries;
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
            eventData[nextEmptyIndex++] = 0; // retry count
        }
    }
    
    public void processAllEvents()
    {
        synchronized(this)
        {
            
            if(this.nextEmptyIndex == 0) return;
            
            int newEmptyIndex = 0;
            int i = 0;
            
            while(i < this.nextEmptyIndex)
            {
                int x = eventData[i++];
                int y = eventData[i++];
                int z = eventData[i++];
                int amount = eventData[i++];
                int retryCount = eventData[i++];
                
                if(!this.processEvent(x, y, z, amount) && retryCount < this.maxRetries)
                {
                    eventData[newEmptyIndex++] = x;
                    eventData[newEmptyIndex++] = y;
                    eventData[newEmptyIndex++] = z;
                    eventData[newEmptyIndex++] = amount;
                    eventData[newEmptyIndex++] = retryCount + 1;
                }
            }
            
            this.nextEmptyIndex = newEmptyIndex;
        }
    }
    
    public int size()
    {
        return this.capacity / 5;
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
