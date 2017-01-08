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
    private float fluidAmount = 0; // 1.0 is one full block of fluid at surface pressure

    private static int nextCellID = 0;

    private byte referenceCount = 0;

    /**
     * If cell was created in a space partially occupied by a solid flow height block, 
     * this is the level of the solid layer.  Used to compute a proper retained level and slope.
     * Because we don't support multi-part yet, this level will be "melted" and included in our currentLevel
     * when the cell is created by adding lava to it.
     */
    private float floorLevel = 0;

    /** 
     * If this is at or near surface, level will not drop below this - to emulate surface tension/viscosity.
     * Can be > 1 if block above should also retain fluid,
     */
    private float retainedLevel;

    private float delta = 0;

    /** for tracking block updates */
    private int lastVisibleLevel = 0;

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
     * tickindex should be zero if there has never been a flow.
     */
    public LavaCell(LavaSimulator sim, BlockPos pos, float fluidAmount)
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
            final boolean wasDirty = this.getVisibleLevel() != this.lastVisibleLevel;
            boolean oldFluidState = false;
            
            if(this.fluidAmount > 0)
            {
                oldFluidState = true;
            }
            else if(this.delta > 0)
            {
                
                // When fluid is first added to a cell with a floor - we melt the floor and include it in the lava.
                // Necessary because we don't have any kind of multipart capability yet to retain pre-existing block.
                this.fluidAmount = this.floorLevel;
            }
                
            this.fluidAmount += this.delta;
            this.delta = 0;

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
     * 0 means block does not contain fluid.
     */
    public int getVisibleLevel()
    {
        if(this.fluidAmount < MINIMUM_CELL_CONTENT) 
            return 0;
        else
            return Math.round(Math.max(1, Math.min(FlowHeightState.BLOCK_LEVELS_FLOAT, this.fluidAmount * FlowHeightState.BLOCK_LEVELS_FLOAT)));
    }

    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate()
    {
        this.lastVisibleLevel = this.getVisibleLevel();
    }


    public float getFluidAmount()
    {
        return this.fluidAmount;
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
     * Updates retained level based on surrounding terrain if flag is set 
     * OR if was previously a barrier cell or if floor level has changed.
     * Clears floor value and updates retained level if discovers world lava in a cell thought to be empty.
     * Creates connections if cell was previously a barrier.
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

                // If we dont' agree on the particulars, simulation wins.
                if(worldVisibleLevel != this.getVisibleLevel())
                {
                    // ask for a block update
                    sim.dirtyCells.add(this);
                }
            }
            else
            {
                // Uh oh! World has lava and we don't!

                // If I had a floor, it's probably not valid now because we don't know what happened.
                // And if floor is changing should force update of retained height also.
                if(this.floorLevel != 0)
                {
                    this.floorLevel = 0;
                    updateRetainedLevel = true;
                }

                // Not a barrier any longer.
                // Adding fluid below will cause me to form connections,
                // so don't need to do that here.
                this.isBarrier = false;

                // Make us a fluid cell.
                this.changeLevel(sim, (worldVisibleLevel / FlowHeightState.BLOCK_LEVELS_FLOAT), false);
                this.applyUpdates(sim);
                this.clearBlockUpdate();
                sim.setSaveDirty(true);
            }
        }
        else
        {
            // world doesn't have lava

            // If we have lava, remove it UNLESS world is open space and we just don't have enough lava to be visible
            if(this.fluidAmount > 0 && (isBarrierInWorld || this.getVisibleLevel() > 0))
            {
                this.changeLevel(sim, (-this.fluidAmount), false);
                this.applyUpdates(sim);
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

                float newFloor =  worldVisibleLevel / FlowHeightState.BLOCK_LEVELS_FLOAT;
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
            if(this.floorLevel > 0)
            {
                this.retainedLevel = this.floorLevel + 0.5F;
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
                    this.retainedLevel = Math.max(0, sim.terrainHelper.computeIdealBaseFlowHeight(pos.down()) - 1F);
                }
            }
            else
            {
                this.retainedLevel = sim.terrainHelper.computeIdealBaseFlowHeight(pos);
            }
            
         
            if(this.retainedLevel > 1)
            {
                // if retained level > 1, want to clamp it at the equilibrium point
                this.retainedLevel  = this.retainedLevel - (this.retainedLevel - 1) * LavaCellConnection.INVERSE_PRESSURE_FACTOR;
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
        return bottom.isBarrier || bottom.fluidAmount >= 1;
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

    public float getFloor()
    {
        return this.floorLevel;
    }
    
    /**
     * How much lava can this cell accept if being added at top.
     * Used by simulation addLava method.
     * Takes floor into account if this is an empty cell witha flow bottom.
     */
    public float getCapacity()
    {
        return this.isBarrier ? 0 : Math.max(0, Math.max(1, this.retainedLevel) - Math.max(this.floorLevel, this.fluidAmount));
    }

    /** for use by NBT loader */
    public void setFloor(float floor)
    {
        this.floorLevel = floor;
    }
}