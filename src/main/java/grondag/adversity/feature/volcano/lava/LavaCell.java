package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class LavaCell
{
    private float currentLevel = 0; // 1.0 is one full block of fluid at surface pressure
    
    private static int nextCellID = 0;
    
    private byte referenceCount = 0;
    
    /** 
     * If this is at or near surface, level will not drop below this - to emulate surface tension/viscosity.
     * Can be > 1 if block above should also retain fluid,
     */
    private float retainedLevel;
    
    private float delta = 0;
    
    /** for tracking block updates */
    private int lastVisibleLevel = 0;
//    
//    /** if this had a solid flow-block floor, the 
//    private float floorLevel;
    
//    private float pressure; // == currentLevel / actualVolume
    
    public final BlockPos pos;
    
    private final int id;

    private boolean isBarrier;
    
    /** marks the last time lava flowed in or out of this cell */
    private int lastFlowTick;
    
    @Override 
    public int hashCode()
    {
        return this.id;
    }
    
    /**
     * Creates a lava cell without any notification or addition to collections.
     */
    public LavaCell(LavaSimulator sim, BlockPos pos)
    {
     this(sim, pos, 0);
    }
    
    /**
     * Creates a lava cell without any notification or addition to collections.
     */
    public LavaCell(LavaSimulator sim, BlockPos pos, float level)
    {
        this(sim, pos, level, (level == 0 | sim == null) ? 0 : sim.getTickIndex());

    }
    
    /**
     * Creates a lava cell without any notification or addition to collections.
     * tickindex should be zero if there has never been a flow.
     */
    public LavaCell(LavaSimulator sim, BlockPos pos, float level, int tickIndex)
    {
        this.pos = pos;
        this.currentLevel = level;
        this.id = nextCellID++;
        this.lastFlowTick = tickIndex;
    }
    
    
    protected LavaCell getNeighbor(LavaSimulator sim, EnumFacing face)
    {  
        //TODO: cache for performance?  
        //Suspect DOWN and UP are most accessed - could store cells in 256-tall vertical arrays within an x,z hash table
        //Vertical access would then be very fast without need to cache references
        return sim.getCell(pos.add(face.getDirectionVec()));

    }
  
    
    public void changeLevel(LavaSimulator sim, float amount)
    {
        this.changeLevel(sim, amount, true);
    }
    
    /**
     * Set notifySimulator = false if going to call applyUpdate directly
     */
    public void changeLevel(LavaSimulator sim, float amount, boolean notifySimulator)
    {
        
//        if(this.id == 1947 || this.id == 3196 || this.id == 3198)
//                    Adversity.log.info("changeLevel cell=" + this.id + " amount=" + amount);
        
        if(amount != 0)
        {
            this.lastFlowTick = sim.getTickIndex();
            this.delta += amount;
            if(notifySimulator) sim.notifyCellChange(this);
        }
    }
    
    public void applyUpdates(LavaSimulator sim)
    {
//        Adversity.log.info("LavaSimCell applyUpdates id=" + this.id + "w/ delta=" + this.delta + " @" + this.pos.toString());
        
        if(this.delta != 0)
        {
            boolean wasDirty = this.getVisibleLevel() != this.lastVisibleLevel;
            boolean oldFluidState = this.currentLevel > 0;
            
            this.currentLevel += this.delta;
            this.delta = 0;
            
            if(this.currentLevel < 0)
            {
                Adversity.log.info("Negative cell level detected: " + this.currentLevel + " cellID=" + this.id + " pos=" + this.pos.toString());
                this.currentLevel = 0;
            }
            
            
            if(oldFluidState != this.currentLevel > 0)
            {
                sim.updateFluidStatus(this, this.currentLevel > 0);
            }
            
            if(wasDirty)
            {
                if(this.getVisibleLevel() == this.lastVisibleLevel) 
                {
                    sim.dirtyCells.remove(this);
                }
                
            }
            else if(this.getVisibleLevel() != this.lastVisibleLevel)
            {
                sim.dirtyCells.add(this);
            }
            

        }
    }
    
    public void provideBlockUpdate(LavaSimulator sim, Collection<LavaBlockUpdate> updateList)
    {
        if(this.isBarrier) return;
        
        int currentVisible = this.getVisibleLevel();
        if(this.lastVisibleLevel != currentVisible)
        {
            updateList.add(new LavaBlockUpdate(pos, currentVisible));
            this.lastVisibleLevel = currentVisible;
        }
    }
    
    /**
     * Returns number between 0 and 12 representing current visible block level.
     * 0 means block is empty and does not contain fluid.
     */
    public int getVisibleLevel()
    {
        if(this.currentLevel < MINIMUM_CELL_CONTENT) 
            return 0;
        else
            return Math.round(Math.max(1, Math.min(FlowHeightState.BLOCK_LEVELS_FLOAT, this.currentLevel * FlowHeightState.BLOCK_LEVELS_FLOAT)));
    }
    
    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate()
    {
        this.lastVisibleLevel = this.getVisibleLevel();
    }
    
    public float getCurrentLevel()
    {
        return this.currentLevel;
    }
 
    public float getDelta()
    {
        return this.delta;
    }
    
//    public void markdeleted()
//    {
//        this.isDeleted = true;
//    }
    
    /**
     * Synchronizes cell with world state.
     * Updates retained level based on surrounding terrain if flag is set or if was previously a barrier cell.
     * Updates simulator cellsWithFluid collection if appropriate.
     */
    public void validate(LavaSimulator sim, boolean updateRetainedLevel)
    {
        
        //TODO: remove
//        if(pos.getX() == 70 && pos.getY() == 78 && pos.getZ() == 110)
//            Adversity.log.info("boop");
//        
//        if(pos.getX() == 70 && pos.getY() == 79 && pos.getZ() == 110)
//            Adversity.log.info("boop");
        
        IBlockState myState = sim.world.getBlockState(this.pos);
        if(sim.terrainHelper.isLavaSpace(myState))
        {
            //Space can contain lava, so we should be non-barrier
            //and be either empty or have lava.

            if(this.isBarrier)
            {
                // If was a barrier, need to create connections with neighboring fluid cells
                // May be redundant of requests made from applyUpdates below if we have fluid, but shouldn't matter.
                updateRetainedLevel = true;
                this.isBarrier = false;
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    LavaCell other = this.getNeighbor(sim, face);
                    if(!other.isBarrier() && other.currentLevel > 0)
                    {
                        sim.requestNewConnection(this.pos, other.pos);
                    }
                }
            }
            
           if(updateRetainedLevel) 
           {
                if(sim.terrainHelper.isLavaSpace(sim.world.getBlockState(pos.down())))
                {
                    if(sim.terrainHelper.isLavaSpace(sim.world.getBlockState(pos.down().down())))
                    {
                        //if two blocks below is also open/lava, then will have no retained level
                        this.retainedLevel = 0;
                    }
                    else
                    {
                        // If two blocks below is a barrier, then will have a retained level
                        // when the retained level below is > 1.
                        // Clamp this to 1 so that laval can flow out of this block.
                        this.retainedLevel = Math.max(0, sim.terrainHelper.computeIdealBaseFlowHeight(pos.down()) - 1F);
                    }
                }
                else
                {
                    // if retained level > 1, want to clamp it at the equilibrium point
                    float level = sim.terrainHelper.computeIdealBaseFlowHeight(pos);
                    if(level <= 1)
                    {
                        this.retainedLevel = level;
                    }
                    else
                    {
                        this.retainedLevel  = level - (level - 1) * LavaCellConnection.INVERSE_PRESSURE_FACTOR;
//                        this.retainedLevel = Math.min(1, sim.terrainHelper.computeIdealBaseFlowHeight(pos));
                    }
                }
            }
            
            // sim wins over world state
            int worldLevel = IFlowBlock.getFlowHeightFromState(sim.world.getBlockState(this.pos));
            int myLevel = this.getVisibleLevel();
            if(worldLevel != myLevel)
            {
                sim.dirtyCells.add(this);
            }
        }
        else
        {
            // should be a barrier cell
            if(!this.isBarrier)
            {
                if(this.currentLevel != 0)
                {
                    this.changeLevel(sim, (-this.currentLevel), false);
                    this.applyUpdates(sim);
                    this.clearBlockUpdate();
                    sim.setSaveDirty(true);
                }
                this.isBarrier = true;
            }
        }
   
    }
   
    public float getRetainedLevel()
    {
        return this.retainedLevel;
    }
    
    public boolean isBarrier()
    {
        return this.isBarrier;
    }
    
    /**
     * True if directly above a barrier or if cell below is full (level >= 1).
    */
    public boolean isSupported(LavaSimulator sim)
    {
        LavaCell bottom = this.getNeighbor(sim, EnumFacing.DOWN);
        return bottom.isBarrier || bottom.getCurrentLevel() >= 1;
    }
    
    public boolean isDrop(LavaSimulator sim)
    {
        LavaCell bottom = this.getNeighbor(sim, EnumFacing.DOWN);
        return !bottom.isBarrier && bottom.getCurrentLevel() == 0;
    }
    
    //TODO: remove
    private static int retainCallCount = 0;
    private static int releaseCallCount = 0;
    private static long lastUpdateNanoTime = System.nanoTime();
    private StringBuilder builder = new StringBuilder();

    /** 
     * Don't create cells with less than this amount of fluid.
     * Vertical cells with less than this amount will be compressed into the cell below.
     */
    private final static float MINIMUM_CELL_CONTENT = 1F/24F;
    
    public void retain(String desc)
    {

        builder.append("retain " + desc + System.lineSeparator());
        
        //TODO: remove
        if(System.nanoTime() - lastUpdateNanoTime >= 10000000000L)
        {
            Adversity.log.info("cell retains/sec=" + retainCallCount / (lastUpdateNanoTime/1000000000));
            Adversity.log.info("cell release/sec=" + releaseCallCount / (lastUpdateNanoTime/1000000000));
            retainCallCount = 0;
            releaseCallCount = 0;
            lastUpdateNanoTime = System.nanoTime();
        }
        retainCallCount++;
        
//        Adversity.log.info("retain id=" + this.id);
        this.referenceCount++;
    }
    
    public void release(String desc)
    {
        builder.append("release " + desc + System.lineSeparator());
        
        releaseCallCount++;
        
//        Adversity.log.info("release id=" + this.id);
        this.referenceCount--;
        
        if (this.referenceCount < 0) 
        {
            Adversity.log.info("negative reference count " + this.referenceCount + " for cell id=" + this.id);
            Adversity.log.info(builder.toString());
        }
    }
    
    public boolean isRetained()
    {
        return this.referenceCount > 0;
    }
    
    public int getLastFlowTick()
    {
        return this.lastFlowTick;
    }
    
    /** for use by NBT loader */
    public void setLastFlowTick(int lastFlowTick)
    {
        this.lastFlowTick = lastFlowTick;
    }
}