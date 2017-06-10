package grondag.adversity.feature.volcano.lava.simulator;

import java.util.Arrays;
import java.util.concurrent.Executor;

import grondag.adversity.Configurator;
import grondag.adversity.Output;
import grondag.adversity.library.CountedJob;
import grondag.adversity.library.CountedJob.CountedJobTask;
import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.library.Job;
import grondag.adversity.library.SimpleConcurrentList;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.PerformanceCollector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class BlockEventList
{
    private final SimpleConcurrentList<BlockEventList.BlockEvent> eventList;

    private final int maxRetries;
    private final String nbtTagName;
    private final BlockEventHandler eventHandler;
    
    private final CountedJobTask<BlockEventList.BlockEvent> processTask = new CountedJobTask<BlockEventList.BlockEvent>() {

        @Override
        public void doJobTask(BlockEventList.BlockEvent operand)
        {
            if(!operand.isDeleted()) operand.process(maxRetries);
        }
    };
    
    private final Job processJob;
    
    public BlockEventList(int maxRetries, String nbtTagName, BlockEventHandler eventHandler, PerformanceCollector perfCollector)
    {
        eventList = SimpleConcurrentList.create(Configurator.VOLCANO.enablePerformanceLogging, nbtTagName + " Block Events", perfCollector);
        
        processJob = new CountedJob<BlockEventList.BlockEvent>(this.eventList, processTask, 64, 
                Configurator.VOLCANO.enablePerformanceLogging, nbtTagName + " Event Processing", perfCollector);
        
        this.maxRetries = maxRetries;
        this.nbtTagName = nbtTagName;
        this.eventHandler = eventHandler;
    }

    public void addEvent(int x, int y, int z, int amount)
    {
        this.eventList.add(new BlockEvent(x, y, z, amount));
        
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
            this.eventList.removeDeletedItems();
        }
    }
    
    public void writeNBT(NBTTagCompound nbt)
    {
      
        int[] saveData = new int[this.eventList.size() * BlockEvent.NBT_WIDTH];
        int i = 0;

        for(BlockEvent event : this.eventList)
        {
            if(!event.isDeleted())
            {
                event.writeNBTArray(saveData, i);
                
                // Java parameters are always pass by value, so have to advance index here
                i += BlockEvent.NBT_WIDTH;
            }
        }
        
        Output.info("Saving " + i / BlockEvent.NBT_WIDTH + " Block Events with tag " + this.nbtTagName);
        
        nbt.setIntArray(this.nbtTagName, Arrays.copyOfRange(saveData, 0, i));
    }
    
    public void readNBT(NBTTagCompound nbt)
    {
        this.eventList.clear();
        
        int[] saveData = nbt.getIntArray(this.nbtTagName);
        
        //confirm correct size
        if(saveData == null || saveData.length % BlockEvent.NBT_WIDTH != 0)
        {
            Output.warn("Invalid save data loading block events with tag " + nbtTagName + ". Lava blocks may not be updated properly.");
        }
        else
        {
            this.eventList.clear();
            int i = 0;
            
            while(i < saveData.length)
            {
                this.eventList.add(new BlockEvent(saveData, i));
                i += BlockEvent.NBT_WIDTH;
            }
          
            Output.info("Loaded " + this.eventList.size() + " block events with NBT Tag " + nbtTagName);
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
        private volatile int retryCount = 0;
        
        public static final int NBT_WIDTH = 5;
        
        private static final int IS_COMPLETE = Integer.MAX_VALUE;
        
        private void process(int maxRetries)
        {
            if(eventHandler.handleEvent(this)) 
            {
                retryCount = IS_COMPLETE;
            }
            else if(retryCount++ > maxRetries)
            {
                //exceeded max retries - give up
                if(Output.DEBUG_MODE)
                    Output.info(String.format("Lava add event @ %1$d %2$d %3$d discarded after max retries. Amount = %4$d", this.x, this.y, this.z, this.amount));
                retryCount = IS_COMPLETE;
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
        
        public int retryCount()
        {
            return this.retryCount;
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
