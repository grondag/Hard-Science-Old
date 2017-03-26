package grondag.adversity.feature.volcano.lava.simulator;

import java.util.Arrays;
import java.util.concurrent.Executor;

import grondag.adversity.Adversity;
import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.library.Job;
import grondag.adversity.library.SimpleConcurrentList;
import grondag.adversity.library.Job.JobTask;
import grondag.adversity.library.PackedBlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class BlockEventList2 extends SimpleConcurrentList<BlockEventList2.BlockEvent>
{
    
    private final int maxRetries;
    private final String nbtTagName;
    private final BlockEventHandler eventHandler;
    
    private final JobTask processTask = new JobTask() {

        @Override
        public void doJobTask(int index)
        {
            ((BlockEvent)items[index]).process(maxRetries);
        }
    };
    
    private final Job<BlockEvent> processJob = new Job<BlockEvent>(processTask, this, 64);
    
    public BlockEventList2(int maxRetries, String nbtTagName, BlockEventHandler eventHandler)
    {
        super();
        this.maxRetries = maxRetries;
        this.nbtTagName = nbtTagName;
        this.eventHandler = eventHandler;
    }

    public void addEvent(int x, int y, int z, int amount)
    {
        this.add(new BlockEvent(x, y, z, amount));
        
    }
    
    public void addEvent(long packedBlockPos, int amount)
    {
        this.addEvent(PackedBlockPos.getX(packedBlockPos), PackedBlockPos.getY(packedBlockPos), PackedBlockPos.getZ(packedBlockPos), amount);
    }
    
    public void addEvent(BlockPos pos, int amount)
    {
        this.addEvent(pos.getX(), pos.getY(), pos.getZ(), amount);
    }
    
    public void processAllEventsOn(Executor executor)
    {
        synchronized(this)
        {
            processJob.runOn(executor);
            this.removeDeletedItems();
        }
    }
    
    public void writeNBT(NBTTagCompound nbt)
    {
      
        int[] saveData = new int[this.size() * BlockEvent.NBT_WIDTH];
        int i = 0;

        for(BlockEvent event : this)
        {
            if(!event.isDeleted())
            {
                event.writeNBTArray(saveData, i);
                
                // Java parameters are always pass by value, so have to advance index here
                i += BlockEvent.NBT_WIDTH;
            }
        }
        
        Adversity.log.info("Saving " + i / BlockEvent.NBT_WIDTH + " Block Events with tag " + this.nbtTagName);
        
        nbt.setIntArray(this.nbtTagName, Arrays.copyOfRange(saveData, 0, i));
    }
    
    public void readNBT(NBTTagCompound nbt)
    {
        this.clear();
        
        int[] saveData = nbt.getIntArray(this.nbtTagName);
        
        //confirm correct size
        if(saveData == null || saveData.length % BlockEvent.NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading block events with tag " + nbtTagName + ". Lava blocks may not be updated properly.");
        }
        else
        {
            this.clear();
            int i = 0;
            
            while(i < saveData.length)
            {
                this.add(new BlockEvent(saveData, i));
                i += BlockEvent.NBT_WIDTH;
            }
          
            Adversity.log.info("Loaded " + this.size() + " block events with NBT Tag " + nbtTagName);
        }
    }
    
    public interface BlockEventHandler
    {
        /** return true if event is complete and should not be retried */
        public abstract boolean handleEvent(BlockEvent event);
    }
    
    public class BlockEvent implements ISimpleListItem
    {
        public final int x;
        public final int y;
        public final int z;
        public final int amount;
        private int retryCount = 0;
        
        public static final int NBT_WIDTH = 5;
        
        private static final int IS_COMPLETE = Integer.MAX_VALUE;
        
        private void process(int maxRetries)
        {
            if(retryCount++ < maxRetries)
            {
                if(eventHandler.handleEvent(this)) retryCount = IS_COMPLETE;
            }
        }
        
        private BlockEvent(int x, int y, int z, int amount)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.amount = amount;
        }
        
        @Override
        public boolean isDeleted()
        {
            return retryCount == IS_COMPLETE;
        }
        
        /** 
         * Writes data to array starting at location i.
         */
        private void writeNBTArray(int[] saveData, int i)
        {
             
            saveData[i++] = this.x;
            saveData[i++] = this.y;
            saveData[i++] = this.z;
            saveData[i++] = this.amount;
            saveData[i++] = this.retryCount;
        }
        
        /** 
         * Reads data from array starting at location i.
         */
        private BlockEvent(int[] saveData, int i)
        {
            // see writeNBT to understand how data are persisted
            this.x = saveData[i++];
            this.y = saveData[i++];
            this.z = saveData[i++];
            this.amount = saveData[i++];
            this.retryCount = saveData[i++];
        }
    }
}
