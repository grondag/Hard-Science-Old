package grondag.hard_science.simulator.base.jobs.tasks;

import com.google.common.collect.ImmutableList;

import gnu.trove.impl.hash.TIntByteHash;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Stores the placement specification for a construction 
 * job and incrementally generates all the necessary tasks
 * with that job when {@link #doPlanningWork(int)} is called
 * by the system construction planner each server tick.
 * <p>
 * Doing this incrementally allows for very large builds from multiple
 * players without creating the lag spikes that would come 
 * from inspecting 1000's of block states in one tick.
 */
public class BuildPlanningTask  extends AbstractTask
{
    private AbstractPlacementSpec spec;
    
    /** ID of next spec entry we need to plan.  */
    private int indexOfNextSpecToPlan = 0;
    
    /** use this constructor to create new jobs */
    public BuildPlanningTask(AbstractPlacementSpec spec)
    {
        super(true);
        this.spec = spec;
    }
    
    /** This constructor meant for deserialization only. */
    public BuildPlanningTask()
    {
        super(false);
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.BUILD_PLANNING;
    }
    
    public AbstractPlacementSpec spec()
    {
        return this.spec;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.indexOfNextSpecToPlan = tag.getInteger(ModNBTTag.PLACEMENT_SPEC_INDEX);
        NBTTagCompound subTag = tag.getCompoundTag(ModNBTTag.PLACEMENT_ENTRY_DATA);
        this.spec = PlacementSpecType.deserializeSpec(subTag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setInteger(ModNBTTag.PLACEMENT_SPEC_INDEX, this.indexOfNextSpecToPlan);
        tag.setTag(ModNBTTag.PLACEMENT_ENTRY_DATA, PlacementSpecType.serializeSpec(this.spec));
    }

    @Override
    public boolean initialize(Job job)
    {
        boolean result = super.initialize(job);
        if(this.spec == null)
        {
            Log.warn("Build planning task had null specification and was cancelled.  This is probably a bug.");
            this.cancel();
            result = false;
        }
        return result;
    }

    /**
     * Surveys world and generates other tasks in this job
     * to complete the build as specified.  Called by system
     * construction planner during server tick.
     * Changes own status to complete if all planning is done.
     * <p>
     * @param howManyOperations Used to throttle CPU consumption each tick.
     * @return True if planning is complete.
     */
    public boolean doPlanningWork(int howManyOperations)
    {
        ImmutableList<PlacementSpecEntry> entries = this.spec.entries();
        
        World world = this.spec.getLocation().world();
        
        int maxIndex = Math.min(this.indexOfNextSpecToPlan + howManyOperations, entries.size());
        while(this.indexOfNextSpecToPlan < maxIndex)
        {
            this.planEntry(world, entries.get(this.indexOfNextSpecToPlan++));
        }
        
        if(this.indexOfNextSpecToPlan >= entries.size())
        {
            this.complete();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void planEntry(World world, PlacementSpecEntry entry)
    {
        IBlockState curBlockState = world.getBlockState(entry.pos());
        
        boolean isPlacement = !entry.isExcavation();
        
        //check for excavation
        boolean needsExcavate = curBlockState != Blocks.AIR.getDefaultState();
        
        // schedule excavation if necessary
        ExcavationTask exTask = null;
        if(needsExcavate)
        {
            exTask = new ExcavationTask(this, entry);
            link(this, exTask);
            entry.excavationTaskID = exTask.getId();
            this.job.addTask(exTask);
        }
        
        if(isPlacement)
        {
            // schedule procurement/fabrication
            BlockProcurementTask procTask = new BlockProcurementTask(this, entry);
            link(this, procTask);
            entry.procurementTaskID = procTask.getId();
            this.job.addTask(procTask);
            
            // schedule placement
            PlacementTask placeTask = new PlacementTask(this, entry);
            if(exTask != null) link(exTask, placeTask);
            link(procTask, placeTask);
            entry.placementTaskID = placeTask.getId();
            this.job.addTask(placeTask);
            
            // place virtual blocks...  tag them with this plan/entry so they can update if modified
            if(!needsExcavate) placeTask.placeVirtualBlock(world);
        }
    }
    
}
