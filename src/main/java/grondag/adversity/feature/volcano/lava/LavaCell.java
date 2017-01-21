package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class LavaCell
{
    public static final int FLUID_UNITS_PER_LEVEL = 1000;
    public static final int FLUID_UNITS_PER_BLOCK = FLUID_UNITS_PER_LEVEL * FlowHeightState.BLOCK_LEVELS_INT;
    public static final int FLUID_UNTIS_PER_HALF_BLOCK = FLUID_UNITS_PER_BLOCK / 2;

    private LavaCellConnection neighborUp = NowhereConnection.INSTANCE;
    private LavaCellConnection neighborDown = NowhereConnection.INSTANCE;
    private LavaCellConnection neighborEast = NowhereConnection.INSTANCE;
    private LavaCellConnection neighborWest = NowhereConnection.INSTANCE;
    private LavaCellConnection neighborNorth = NowhereConnection.INSTANCE;
    private LavaCellConnection neighborSouth = NowhereConnection.INSTANCE;

    private int fluidAmount = 0; // 1.0 is one full block of fluid at surface pressure

    private static int nextCellID = 0;

    private boolean neverCools = false;

    /** 
     * False if it is possible currentVisibleLevel won't match lastVisible 
     * Saves cost of calculating currentVisible if isn't necessary.
     */
    private boolean isBlockUpdateCurrent = true;
    
    private byte referenceCount = 0;
    
    /** false until first validate - lets us know we don't have to remove connections or do other housekeeping that won't apply */
    private boolean isFirstValidationComplete = false;

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
    
    public void changeLevel(LavaSimulator sim, int amount)
    {
//        if(this.hashCode() == 1271)
//            Adversity.log.info("boop");
        
        if(amount != 0)
        {

            this.lastFlowTick = sim.getTickIndex();

            final boolean oldFluidState = this.fluidAmount > 0;

            if(amount > 0)
            {
                //if this is an empty drop cell, queue a drop particle instead of adding the lava
                if(this.isDrop(sim))
                {
                    //                    Adversity.log.info("LavaCell id=" + this.id + " with level =" + this.fluidAmount + " changeLevel diverted to particle: amount=" + amount +" @"+ pos.toString());
                    sim.queueParticle(this.pos, amount);
                    return;
                }
                else if(!oldFluidState)
                {
                    // When fluid is first added to a cell with a floor - we melt the floor and include it in the lava.
                    // Necessary because we don't have any kind of multipart capability yet to retain pre-existing block.
                    this.fluidAmount = this.floorLevel;
                }
            }

            this.fluidAmount += amount;
            
            // Don't want to do these next two if spawned a particle and exited - because we're still an  empty cell - no change.
            isBlockUpdateCurrent = false;
            this.makeAllConnectionsDirty();
            
            if(this.fluidAmount < 0)
            {
                Adversity.log.info("Negative cell level detected: " + this.fluidAmount + " cellID=" + this.id + " pos=" + this.pos.toString());
                this.fluidAmount = 0;
            }

            /**
             * If we have a floor and we've somehow drained below it, floor must be lowered to the new fluid level because
             * the solid surface it represents can no longer exist.
             */
            if(this.fluidAmount < this.floorLevel) this.floorLevel = this.fluidAmount;

            if(oldFluidState != this.fluidAmount > 0)
            {
                sim.updateFluidStatus(this, this.fluidAmount > 0);
            }
        }
    }

    /**
     * Assumes block updates will be applied to world/worldBuffer before any more world interaction occurs.
     * Consistent with this expectations, it sets lastVisibleLevel = currentVisibleLevel.
     */
    public void provideBlockUpdateIfNeeded(LavaSimulator sim)
    {
        if(isBlockUpdateCurrent || this.isBarrier) return;

        int currentVisible = this.getCurrentVisibleLevel();
        if(this.lastVisibleLevel != currentVisible)
        {
            LavaSimulator.blockUpdatesProvisionCounter++;
            
            final IBlockState priorState = sim.worldBuffer.getBlockState(pos);
            if(currentVisible == 0)
            {
                if(priorState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    sim.worldBuffer.setBlockState(pos, Blocks.AIR.getDefaultState(), priorState);
                }
            }
            else
            {
                sim.worldBuffer.setBlockState(pos, 
                        IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), currentVisible),
                        priorState);
            }
            this.lastVisibleLevel = currentVisible;
            this.isBlockUpdateCurrent = true;
        }
    }

    /**
     * Returns number between 0 and 12 representing current visible block level.
     * Always rounds up.
     * 0 means block does not contain any fluid.
     */
    public int getCurrentVisibleLevel()
    {
        //effectively rounds up without FP math
        int result = 0;
        if(this.fluidAmount > 0) result = (fluidAmount + FLUID_UNITS_PER_LEVEL - 1) / FLUID_UNITS_PER_LEVEL;
        if(result > FlowHeightState.BLOCK_LEVELS_INT) result = FlowHeightState.BLOCK_LEVELS_INT;
        return result;
    }

    /**
     * Value that should be in the world. 
     */
    public int getLastVisibleLevel()
    {
        return this.lastVisibleLevel;
    }

    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate()
    {
        this.lastVisibleLevel = this.getCurrentVisibleLevel();
        this.isBlockUpdateCurrent = true;
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

//        if(this.hashCode() == 3316 || this.hashCode() == 3316)
//            Adversity.log.info("boop");
        
        IBlockState myState = sim.worldBuffer.getBlockState(this.pos);

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
                if(worldVisibleLevel != this.lastVisibleLevel)
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
        else
        {
            // world doesn't have lava

            // If we have lava, remove it UNLESS world is open space and we just don't have enough lava to be visible
            if(this.fluidAmount > 0 && (isBarrierInWorld || this.lastVisibleLevel > 0))
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
                        LavaCell other = sim.getFluidCellIfItExists(this.pos.add(face.getDirectionVec()));
                        if(other != null && !other.isBarrier() && other.fluidAmount > 0)
                        {
                            sim.addConnection(this.pos, other.pos);
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
                this.isBarrier = true;
                this.floorLevel = 0;
                this.retainedLevel = 0;
                
                // Make us a barrier.
                // Remove connections to neighboring cells if there were any.
                if(this.isFirstValidationComplete)
                {
                    for(EnumFacing face : EnumFacing.VALUES)
                    {
                        sim.removeConnection(this.pos, pos.add(face.getDirectionVec()));
                    }
                }
            }
        }
        
        if(updateRetainedLevel) updateRetainedLevel(sim);

        this.isFirstValidationComplete = true;
    }

    protected void updateRetainedLevel(LavaSimulator sim)
    {
        if(this.isBarrier)
        {
            this.retainedLevel = 0;
        }
        else
        {
            int effectiveFloor = this.getEffectiveFloor(sim);


            if(effectiveFloor !=  0)
            {
                this.retainedLevel = Math.max(0, effectiveFloor + FLUID_UNTIS_PER_HALF_BLOCK);
            }
            else if(sim.terrainHelper.isLavaSpace(sim.worldBuffer.getBlockState(pos.down())))
            {
                if(sim.terrainHelper.isLavaSpace(sim.worldBuffer.getBlockState(pos.down().down())))
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

    public boolean canCool(LavaSimulator sim)
    {
        //TODO: make ticks to cool configurable
        return !this.neverCools && this.fluidAmount > 0 && sim.getTickIndex() - this.getLastFlowTick() > 200;
    }

    /**
     * True if directly above a barrier or if cell below is full of fluid.
     */
    public boolean isSupported(LavaSimulator sim)
    {
        return this.neighborDown.isBottomSupporting();
    }

    /**
     * True if this cell has no floor and cell below also cannot stop lava.
     */
    public boolean isDrop(LavaSimulator sim)
    {
        //My floor doesn't count if it has melted and become fluid.
        if(this.fluidAmount == 0 && this.floorLevel > 0) return false;

        return this.neighborDown.isBottomDrop();
    }

    //TODO: remove
    private static int retainCallCount = 0;
    private static int releaseCallCount = 0;
    private static long lastUpdateNanoTime = System.nanoTime();
//    private StringBuilder builder = new StringBuilder();

//    /** 
//     * Don't create cells with less than this amount of fluid.
//     * Vertical cells with less than this amount will be compressed into the cell below.
//     */
//    private final static int MINIMUM_CELL_CONTENT = FLUID_UNITS_PER_BLOCK/24;

    public void retain(String desc)
    {

//        builder.append("retain " + desc + System.lineSeparator());

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

    public void bindUp(LavaCellConnection connection) { this.neighborUp = connection; }
    public void bindDown(LavaCellConnection connection) { this.neighborDown = connection; }
    public void bindEast(LavaCellConnection connection) { this.neighborEast = connection; }
    public void bindWest(LavaCellConnection connection) { this.neighborWest = connection; }
    public void bindNorth(LavaCellConnection connection) { this.neighborNorth = connection; }
    public void bindSouth(LavaCellConnection connection) { this.neighborSouth = connection; }
    
    public void unbindUp() { this.neighborUp = NowhereConnection.INSTANCE; }
    public void unbindDown() { this.neighborDown = NowhereConnection.INSTANCE; }
    public void unbindEast() { this.neighborEast = NowhereConnection.INSTANCE; }
    public void unbindWest() { this.neighborWest = NowhereConnection.INSTANCE; }
    public void unbindNorth() { this.neighborNorth = NowhereConnection.INSTANCE; }
    public void unbindSouth() { this.neighborSouth = NowhereConnection.INSTANCE; }

    private void makeAllConnectionsDirty()
    {
        neighborUp.setDirty();
        neighborDown.setDirty();
        neighborEast.setDirty();
        neighborWest.setDirty();
        neighborNorth.setDirty();
        neighborSouth.setDirty();
    }
    
    public void release(String desc)
    {
//        builder.append("release " + desc + System.lineSeparator());

        releaseCallCount++;

        //        Adversity.log.info("release id=" + this.id);
        this.referenceCount--;

        if (this.referenceCount < 0) 
        {
            Adversity.log.info("negative reference count " + this.referenceCount + " for cell id=" + this.id);
//            Adversity.log.info(builder.toString());
        }
    }

    public boolean isRetained()
    {
        return this.referenceCount > 0;
    }

    /**
     * Returns last sim tick when fluid flowed in or out of this cell.
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
        this.neverCools = neverCools;
    }

    public boolean getNeverCools()
    {
        return this.neverCools;
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
     *  For use during validation to determine what my floor should be.
     */
    private int getEffectiveFloor(LavaSimulator sim)
    {
        if(this.isBarrier) return 0;
        
        int result = this.floorLevel;
        if(result == 0)
        {
            BlockPos downPos = this.pos.down();
            LavaCell down = sim.getCellIfItExists(downPos);
            if(down != null)
            {
                if(!down.isBarrier && down.getFloor() > 0)
                {
                    result = down.getFloor() - FLUID_UNITS_PER_BLOCK;
                }
            }
            else
            {
                // Simulation hasn't captured state as a cell, so almost certainly does
                // not contain lava but could contain a height block.
                // Even so, confirm it isn't an orphaned lava block before using it as a floor.
                IBlockState state = sim.worldBuffer.getBlockState(downPos);
                int worldLevel = IFlowBlock.getFlowHeightFromState(state);
                if(worldLevel > 0 && state.getBlock() != NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    result = worldLevel - FLUID_UNITS_PER_BLOCK;
                }
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