package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class LavaCell
{
    public static final int FLUID_UNITS_PER_LEVEL = 1000;
    public static final int FLUID_UNITS_PER_BLOCK = FLUID_UNITS_PER_LEVEL * FlowHeightState.BLOCK_LEVELS_INT;
    public static final int FLUID_UNTIS_PER_HALF_BLOCK = FLUID_UNITS_PER_BLOCK / 2;

    
    private int fluidAmount = 0; // 1.0 is one full block of fluid at surface pressure

    private static int nextCellID = 0;

    private byte referenceCount = 0;

    /**
     * If cell was created in a space partially occupied by a solid flow height block, 
     * this is the level of the solid layer.  Used to compute a proper retained level and slope.
     * Because we don't support multi-part yet, this level will be "melted" and included in our currentLevel
     * when the cell is created by adding lava to it.
     */
    private int floorLevel = 0;

    /** 
     * If this is at or near surface, level will not drop below this - to emulate surface tension/viscosity.
     * Can be > FLUID_UNITS_PER_BLOCK if block above should also retain fluid,
     */
    private int retainedLevel;

    /** for tracking block updates */
    private int lastVisibleLevel = 0;

    public final BlockPos pos;

    private final int id;

    private boolean isBarrier;

    private static final int NEVER_COOLS = Integer.MAX_VALUE;
    /**
     *  Marks the last time lava flowed in or out of this cell.
     *  Set to NEVER_COOLS (negative value) if should never cool.
     */
    private int lastFlowTick = 0;
    

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
     * tickindex should be zero if there has never been a flow.
     */
    public LavaCell(LavaSimulator sim, BlockPos pos, int fluidAmount)
    {
        this.pos = pos;
        this.fluidAmount = fluidAmount;
        this.id = nextCellID++;
        this.lastFlowTick = (fluidAmount == 0 | sim == null) ? 0 : sim.getTickIndex();
    }


    protected LavaCell getNeighbor(LavaSimulator sim, EnumFacing face)
    {  
        //TODO: cache for performance?  
        //Suspect DOWN and UP are most accessed - could store cells in 256-tall vertical arrays within an x,z hash table
        //Vertical access would then be very fast without need to cache references
        return sim.getCell(pos.add(face.getDirectionVec()), false);
    }


    public void changeLevel(LavaSimulator sim, int amount)
    {
        if(amount != 0)
        {
            
//            if(this.id == 757)
//                Adversity.log.info("boop");
            
            // don't update lastflowTick if this is marked to never cool
            if(this.lastFlowTick != NEVER_COOLS) this.lastFlowTick = sim.getTickIndex();
            
            final boolean wasDirty = this.getVisibleLevel() != this.lastVisibleLevel;
            boolean oldFluidState = false;
            
            if(this.fluidAmount > 0)
            {
                oldFluidState = true;
            }
            else if(amount > 0)
            {
                //if this is an empty drop cell, queue a drop particle instead of adding the lava
                if(this.isDrop(sim))
                {
                    Adversity.log.info("LavaCell id=" + this.id + " with level =" + this.fluidAmount + " changeLevel diverted to particle: amount=" + amount +" @"+ pos.toString());
                    sim.queueParticle(this.pos, amount);
                    return;
                }
                else
                {
                    // When fluid is first added to a cell with a floor - we melt the floor and include it in the lava.
                    // Necessary because we don't have any kind of multipart capability yet to retain pre-existing block.
                    this.fluidAmount = this.floorLevel;
                }
            }
                
            this.fluidAmount += amount;
      
            if(this.fluidAmount < 0)
            {
                Adversity.log.info("Negative cell level detected: " + this.fluidAmount + " cellID=" + this.id + " pos=" + this.pos.toString());
                this.fluidAmount = 0;
            }


            if(oldFluidState != this.fluidAmount > 0)
            {
                sim.updateFluidStatus(this, this.fluidAmount > 0);
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
     * Always rounds up.
     * 0 means block does not contain any fluid.
     */
    public int getVisibleLevel()
    {
//        if(this.fluidAmount < MINIMUM_CELL_CONTENT) 
//            return 0;
//        else
//            return Math.min(FlowHeightState.BLOCK_LEVELS_INT, Float.((float)this.fluidAmount, FLUID_UNITS_PER_LEVEL));
            
            //effectively rounds up without FP math
            int result = 0;
            if(this.fluidAmount > 0) result = (fluidAmount + FLUID_UNITS_PER_LEVEL - 1) / FLUID_UNITS_PER_LEVEL;
            if(result > FlowHeightState.BLOCK_LEVELS_INT) result = FlowHeightState.BLOCK_LEVELS_INT;
            return result;
    }

    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate()
    {
        this.lastVisibleLevel = this.getVisibleLevel();
    }


    public int getFluidAmount()
    {
        return this.fluidAmount;
    }

    /**
     * Synchronizes cell with world state. World state generally wins.
     * Updates retained level based on surrounding terrain if flag is set 
     * OR if was previously a barrier cell or if floor level has changed.
     * Clears floor value and updates retained level if discovers world lava in a cell thought to be empty.
     * Creates connections if cell was previously a barrier.
     * Updates simulator cellsWithFluid collection if appropriate.
     * 
     * Does not call changeLevel() to avoid intended side effects of that procedure.
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

        int worldVisibleLevel = IFlowBlock.getFlowHeightFromState(myState);
        boolean isLavaInWorld = worldVisibleLevel > 0 && myState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK;

        // barrier if impassable non-flow block, or if full-height non-lava flow block
        boolean isBarrierInWorld = (worldVisibleLevel == 0 && !sim.terrainHelper.isLavaSpace(myState))
                || (!isLavaInWorld && worldVisibleLevel == FlowHeightState.BLOCK_LEVELS_INT);

        if(isLavaInWorld)
        {
            // world has lava - does sim have lava?
            if(this.fluidAmount > 0)
            {
                // yay! We agree with world this is a lava cell.

                // If we dont' agree on the particulars, world wins.
                if(worldVisibleLevel != this.getVisibleLevel())
                {
                    this.fluidAmount = worldVisibleLevel * FLUID_UNITS_PER_LEVEL;
                    this.clearBlockUpdate();
                    sim.setSaveDirty(true);
                }
            }
            else
            {
                // Uh oh! World has lava and we don't!

                // Not a barrier any longer.
                // Adding fluid below will cause me to form connections,
                // so don't need to do that here.
                this.isBarrier = false;
                
                //Avoid stack overflow when bulk loading at start - many new cells just created won't match world blocks with fluid then.
                if(!sim.isLoading)
                {
                    // If I had a floor, it's probably not valid now because we don't know what happened.
                    // And if floor is changing should force update of retained height also.
                    if(this.floorLevel != 0)
                    {
                        this.floorLevel = 0;
                        updateRetainedLevel = true;
                    }
    
                    // Make us a fluid cell.
                    this.fluidAmount = worldVisibleLevel * FLUID_UNITS_PER_LEVEL;
                    sim.updateFluidStatus(this, true);
                    this.clearBlockUpdate();
                    sim.setSaveDirty(true);
                }
            }
        }
        else
        {
            // world doesn't have lava

            // If we have lava, remove it UNLESS world is open space and we just don't have enough lava to be visible
            if(this.fluidAmount > 0 && (isBarrierInWorld || this.getVisibleLevel() > 0))
            {
                this.fluidAmount = 0;
                sim.updateFluidStatus(this, false);
                this.clearBlockUpdate();
                sim.setSaveDirty(true);

            }

            if(!isBarrierInWorld)
            {
                // Should not be a barrier

                if(this.isBarrier)
                {
                    // add connections to nearby fluid cells if we were previously a barrier

                    // should force update of retained level in this case
                    updateRetainedLevel = true;

                    this.isBarrier = false;
                    for(EnumFacing face : EnumFacing.VALUES)
                    {
                        LavaCell other = this.getNeighbor(sim, face);
                        if(!other.isBarrier() && other.fluidAmount > 0)
                        {
                            sim.requestNewConnection(this.pos, other.pos);
                        }
                    }
                }

                int newFloor =  worldVisibleLevel * FLUID_UNITS_PER_LEVEL;
                if(this.floorLevel != newFloor)
                {
                    // should force update of retained level if floor is new
                    updateRetainedLevel = true;
                    this.floorLevel = newFloor;
                }
            }
            else if(!this.isBarrier)
            {
                // Make us a barrier.
                // No other action needed here because we alreadyt remove lava above
                // and connection will be pruned next time connections are processed.
                this.isBarrier = true;
                this.floorLevel = 0;
                this.retainedLevel = 0;
            }
        }

        if(updateRetainedLevel && !this.isBarrier) 
        {
            int effectiveFloor = this.getEffectiveFloor(sim);
            
            //TODO: make this depend on slope
            if(effectiveFloor !=  0)
            {
                this.retainedLevel = Math.max(0, effectiveFloor + FLUID_UNTIS_PER_HALF_BLOCK);
            }
            else if(sim.terrainHelper.isLavaSpace(sim.world.getBlockState(pos.down())))
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
                    //TODO: optimize for integer math
                    this.retainedLevel = Math.max(0, (int)((sim.terrainHelper.computeIdealBaseFlowHeight(pos.down()) - 1F) * FLUID_UNITS_PER_BLOCK));
                }
            }
            else
            {
                this.retainedLevel = (int)(sim.terrainHelper.computeIdealBaseFlowHeight(pos) * FLUID_UNITS_PER_BLOCK);
            }
            
         
            if(this.retainedLevel > FLUID_UNITS_PER_BLOCK)
            {
                // if retained level > full block, want to clamp it at the equilibrium point
                this.retainedLevel  = this.retainedLevel - (int)((this.retainedLevel - FLUID_UNITS_PER_BLOCK) * LavaCellConnection.INVERSE_PRESSURE_FACTOR);
            }
            
        }
    }

    public int getRetainedLevel()
    {
        return this.retainedLevel;
    }

    public boolean isBarrier()
    {
        return this.isBarrier;
    }

    /**
     * True if directly above a barrier or if cell below is full of fluid.
     */
    public boolean isSupported(LavaSimulator sim)
    {
        LavaCell bottom = this.getNeighbor(sim, EnumFacing.DOWN);
        return bottom.isBarrier || bottom.fluidAmount >= LavaCell.FLUID_UNITS_PER_BLOCK;
    }

    /**
     * True if this cell has no floor and cell below also cannot stop lava.
     */
    public boolean isDrop(LavaSimulator sim)
    {
        //My floor doesn't count if it has melted and become fluid.
        if(this.fluidAmount == 0 && this.floorLevel > 0) return false;
        
        LavaCell bottom = this.getNeighbor(sim, EnumFacing.DOWN);
        return !bottom.isBarrier && bottom.fluidAmount == 0 && bottom.floorLevel == 0;
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
    private final static int MINIMUM_CELL_CONTENT = FLUID_UNITS_PER_BLOCK/24;

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

    /**
     * Returns last sim tick when fluid flowed in or out of this cell.
     * If cell is marked to never cool, returns Integer.MAX_VALUE.
     */
    public int getLastFlowTick()
    {
        return this.lastFlowTick;
    }

    /** for use by NBT loader */
    public void setLastFlowTick(int lastFlowTick)
    {
        this.lastFlowTick = lastFlowTick;
    }

    /** 
     * Prevent cooling by disabling lastFlowTick updates
     * and causes getLastFlowTick to always return Integer.MAX_VALUE.
     */
    public void setNeverCools(boolean neverCools)
    {
        if(neverCools)
        {
            this.lastFlowTick = NEVER_COOLS;
        }
        else if(this.lastFlowTick == NEVER_COOLS)
        {
            this.lastFlowTick = 0;
        }
    }
    
    public boolean getNeverCools()
    {
        return this.lastFlowTick == NEVER_COOLS;
    }
    
    // See floorLevel comments - is raw value
    public int getFloor()
    {
        return this.floorLevel;
    }
    
    /**
     *  Same as getFloor() if this cell has a non-zero floor.
     *  If this cell doesn't have a floor but cell below does,
     *  gives a negative value that represents the distance to the floor below.
     */
    public int getEffectiveFloor(LavaSimulator sim)
    {
        int result = this.floorLevel;
        if(result == 0)
        {
            result = this.getNeighbor(sim, EnumFacing.DOWN).getFloor();
            if(result > 0)
            {
                result -= FLUID_UNITS_PER_BLOCK;
            }
            else
            {
                // negative values should never happen, but prevent weirdness if they do
                result = 0;
            }
        }
        return result;
    }
    
    /**
     * How much lava can this cell accept if being added at top.
     * Used by simulation addLava method.
     * Takes floor into account if this is an empty cell witha flow bottom.
     */
    public int getCapacity()
    {
        return this.isBarrier ? 0 : Math.max(0, Math.max(FLUID_UNITS_PER_BLOCK, this.retainedLevel) - Math.max(this.floorLevel, this.fluidAmount));
    }

    /** for use by NBT loader */
    public void setFloor(int floor)
    {
        this.floorLevel = floor;
    }
}