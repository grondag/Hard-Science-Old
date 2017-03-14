package grondag.adversity.feature.volcano.lava.blockmodel;

import grondag.adversity.Adversity;
import static grondag.adversity.Adversity.DEBUG_MODE;

import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.LavaTerrainHelper;
import grondag.adversity.feature.volcano.lava.blockmodel.LavaCellConnection.BottomType;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class LavaCell
{
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
     * Tracks if self-retain and connection have been set up based on fluid status.
     * Theoretically unnecessary if updateFluidStatus is never called redundantly but
     * here to catch any holes in that logic and prevent extra retains, connections.
     */
    private boolean hasFluidStatus = false;

    /** 
     * False if it is possible currentVisibleLevel won't match lastVisible 
     * Saves cost of calculating currentVisible if isn't necessary.
     */
    private boolean isBlockUpdateCurrent = true;

    private byte referenceCount = 0;

    private boolean isDeleted = false;

    private LavaCell bottomNeighbor = null;
    private LavaCell upNeighbor = null;

    /** false until first validate - lets us know we don't have to remove connections or do other housekeeping that won't apply */
    private boolean isFirstValidationComplete = false;

    /** 
     * If this is at or near surface, level will not drop below this - to emulate surface tension/viscosity.
     * Can be > FLUID_UNITS_PER_BLOCK if block above should also retain fluid.
     * Established when cell is first created.  Does not change until cell solidifies or bottom drops out.
     * Initialized to -1 to indicate has not yet been set.
     */
    private int rawRetainedLevel = -1;

    /** last visible level reported to world */
    private int lastVisibleLevel = 0;

    /** 
     * Exponential average of current level - used for computing visible level.
     * Holds 6 bits of integer precision.  Needs >> 6 to get usable value.
     * Maintained by provideBlockUpdate.
     */
    public int avgFluidAmountWithPrecision = 0;

    public final long packedBlockPos;

    private final int id;

    private boolean isBarrier;

    //TODO: remove
    public int maxLevel;

    /** 
     * True if this cell represents a flow height block. (Solid or liquid.)
     * Updated during every validate.
     */
    private boolean isFlowHeightBlock;

    public static final byte FLOW_FLOOR_DISTANCE_REALLY_FAR = Byte.MAX_VALUE;

    /** see {@link #getDistanceToFlowFloor()} */
    private byte distanceToFlowFloor;

    /**
     * Non-zero if this cell has/had an interior floor because it is/was a solid flow block.
     * Set when the cell is first validated.
     * Should remain if the cell melts but must be reduced to match the fluid level if 
     * the melted fluid drains out somehow, or if the block below is known to be non-solid.
     * Set to 0 if this cell is a non-flow, non-barrier block.
     * Set to 12 if this cell is a barrier (flow or non-flow.)
     * Represented as fluid levels.  (12 per block.)
     */
    private byte interiorFloorLevel;

    /** 
     * True if the block that is represented by distanceToFlowFloor is a flow-type block.
     * False if it is a regular, non-flow block or if this is not known.
     * Used to determine which type of slope calculation is used in horizontal connections.
     * If true, also means the floor could melt and flow away if not supported.
     */
    private boolean flowFloorIsFlowBlock = false;

    //TODO: consolidate flags into single byte value to conserve memory

    /**
     *  Marks the last time lava flowed in or out of this cell.
     */
    private int lastFlowTick = 0;


    //    private StringBuffer traceLog = new StringBuffer();

    @Override 
    public int hashCode()
    {
        return this.id;
    }

    /**
     * Creates a lava cell without any notification or addition to collections.
     */
    public LavaCell(LavaSimulator sim, long packedBlockPos)
    {
        this(sim, packedBlockPos, 0);
    }


    /**
     * Creates a lava cell without any notification or addition to collections.
     * tickindex should be zero if there has never been a flow.
     */
    public LavaCell(LavaSimulator sim, long packedBlockPos, int fluidAmount)
    {
        this.packedBlockPos = packedBlockPos;
        this.fluidAmount = fluidAmount;
        this.id = nextCellID++;
        this.lastFlowTick = (fluidAmount == 0 | sim == null) ? 0 : sim.getTickIndex();

        //        if(sim != null) traceLog.append(sim.getTickIndex() + " create " + fluidAmount + "\n");
    }

    public void changeLevel(LavaSimulator sim, int amount)
    {
        //        if(this.id == 1104 || this.id == 8187)
        //            Adversity.log.info("boop");

        //        traceLog.append(sim.getTickIndex() + " changeLevel " + amount + "\n");

        if(amount != 0)
        {

            this.lastFlowTick = sim.getTickIndex();

            if(amount > 0)
            {
                if(this.fluidAmount == 0)
                {
                    //TODO - already checked this earlier in get flow so any way to prevent a recheck?

                    // If this is an empty drop cell, divert to cell below or queue a particle if cell below is also high up
                    // Note - can't do this if the cell somehow has lava - particle will just be reabsorbed.
                    if(this.distanceToFlowFloor > AbstractLavaSimulator.LEVELS_PER_BLOCK)
                    {
                        LavaCell down = this.getDownEfficiently(sim, false);
                        if(down != null && !down.isBarrier && down.getFluidAmount() == 0)
                            //                          if(this.getBottomType(sim) == BottomType.DROP)
                        {
                            if(down.distanceToFlowFloor < FLOW_FLOOR_DISTANCE_REALLY_FAR)
                            {
                                down.changeLevel(sim, amount);
                                return;
                            }
                            else
                            {
                                //                              Adversity.log.info("LavaCell id=" + this.id + " with level =" + this.fluidAmount + " changeLevel diverted to particle: amount=" + amount +" @"
                                //                                      + PackedBlockPos.unpack(this.packedBlockPos).toString());

                                sim.queueParticle(this.packedBlockPos, amount);
                                return;
                            }
                        }
                    }

                    else
                    {
                        // When fluid is first added to a cell with a floor - we melt the floor and include it in the lava.
                        // Necessary because we don't have any kind of multipart capability yet to retain pre-existing block.
                        if(this.interiorFloorLevel > 0) 
                        {
                            this.fluidAmount = AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL * this.interiorFloorLevel;
                            LavaCell down = this.getDownEfficiently(sim, false);
                            
                            // if the block below is not solid, then our interior floor "melts" away completely and no longer applies.
                            if(!down.isBarrier)
                            {
                                this.interiorFloorLevel = 0;
                                
                                //force recalc of retained level when an unsupported partial flow block melts so that it can drain
                                this.rawRetainedLevel = -1;
                            }
                        }
                    }
                }
            }

            //            Adversity.log.info("changeLevel for cell " + this.id + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
            //                    + " delta=" + amount + " priorfluidAmount=" + this.fluidAmount + " lastVisibleLevel=" + this.lastVisibleLevel);

            this.fluidAmount += amount;

            if(this.fluidAmount >  this.maxLevel) this.maxLevel = fluidAmount; 

            // Note these two steps won't happen if we spawned a particle and exited 
            // This is intentional because we're still an  empty cell - no change.
            isBlockUpdateCurrent = false;
            this.makeAllConnectionsDirty();

            if(this.fluidAmount < 0)
            {
                Adversity.log.info("Negative cell level detected: " + this.fluidAmount + " cellID=" + this.id + " pos=" 
                        + PackedBlockPos.unpack(this.packedBlockPos).toString());
                this.fluidAmount = 0;

                //force recalc of retained level when a cell becomes empty
                this.rawRetainedLevel = -1;
            }
            else if(this.fluidAmount == 0)
            {
                //force recalc of retained level when a cell becomes empty
                this.rawRetainedLevel = -1;
            }

            /**
             * If we have a floor and we've somehow drained below it, floor must be lowered to the new fluid level because
             * the solid surface it represents can no longer exist.
             */
            //            if(this.fluidAmount < this.floorLevel) this.floorLevel = this.fluidAmount;

            if(this.interiorFloorLevel > 0 && this.fluidAmount < AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL * this.interiorFloorLevel)
            {
                this.interiorFloorLevel = (byte) (this.fluidAmount / AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
            }
        }
    }

    /**
     * Assumes block updates will be applied to world/worldBuffer before any more world interaction occurs.
     * Consistent with this expectations, it sets lastVisibleLevel = currentVisibleLevel.
     * Returns the number of updates provided.
     */
    public void provideBlockUpdateIfNeeded(LavaSimulator sim)
    {

        if(isBlockUpdateCurrent || this.isBarrier) return;

        //        if(this.id == 1104 || this.id == 8187)
        //            Adversity.log.info("boop");

        // if we are empty always reflect that immediately - otherwise have ghosting in world as lava drains from drop cells
        if(this.fluidAmount == 0)
        {
            this.avgFluidAmountWithPrecision = 0;
            this.isBlockUpdateCurrent = true;
        }
        else
        {
            int avgAmount = this.avgFluidAmountWithPrecision >> 6;
    
            // don't average big changes
            if(Math.abs(avgAmount - this.fluidAmount) > AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL * 4)
            {
                this.avgFluidAmountWithPrecision = this.fluidAmount << 6;
                this.isBlockUpdateCurrent = true;
            }
            else
            {
                this.avgFluidAmountWithPrecision -= avgAmount; 
                this.avgFluidAmountWithPrecision += this.fluidAmount;
    
                if(this.avgFluidAmountWithPrecision  == this.fluidAmount << 6)
                {
                    this.isBlockUpdateCurrent = true;
                }
            }
        }

        int currentVisible = this.getCurrentVisibleLevel();
        if(this.lastVisibleLevel != currentVisible)
        {
            //            Adversity.log.info("Providing block update for cell " + this.id + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
            //                    + " currentVisibleLevel=" + currentVisible + " lastVisibleLevel=" + this.lastVisibleLevel);

            final IBlockState priorState = sim.worldBuffer.getBlockState(this.packedBlockPos);
            if(currentVisible == 0)
            {
                if(priorState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    sim.worldBuffer.setBlockState(this.packedBlockPos, Blocks.AIR.getDefaultState(), priorState);

                    //                    traceLog.append(sim.getTickIndex() + "provideBlockUpdate current=" + currentVisible + " last=" + this.lastVisibleLevel
                    //                            + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
                    //                            + " priorState=" + priorState.toString()
                    //                            + " newState=" +Blocks.AIR.getDefaultState().toString()
                    //                            + "\n");
                }
            }
            else
            {
                sim.worldBuffer.setBlockState(this.packedBlockPos, 
                        IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), currentVisible),
                        priorState);

                //                traceLog.append(sim.getTickIndex() + "provideBlockUpdate current=" + currentVisible + " last=" + this.lastVisibleLevel
                //                        + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
                //                        + " priorState=" + priorState.toString()
                //                        + " newState=" + IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), currentVisible).toString()
                //                        + "\n");
            }
            this.lastVisibleLevel = currentVisible;
        }
    }

    /**
     * Returns number between 0 and 12 representing current visible block level.
     * Always rounds down.
     * 0 means block does not contain enough fluid to be visible in world.
     */
    public int getCurrentVisibleLevel()
    {
        int result = (this.avgFluidAmountWithPrecision >> 6) / AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
        //        //effectively rounds up without FP math
        //        if(this.fluidAmount > 0) result = (fluidAmount + FLUID_UNITS_PER_LEVEL - 1) / FLUID_UNITS_PER_LEVEL;

        if(result > AbstractLavaSimulator.LEVELS_PER_BLOCK) result = AbstractLavaSimulator.LEVELS_PER_BLOCK;
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
    public void clearBlockUpdate(AbstractLavaSimulator sim)
    {
        //        traceLog.append(sim.getTickIndex() + " clearBlockUpdate\n");

        this.avgFluidAmountWithPrecision = this.fluidAmount << 6;
        this.lastVisibleLevel = this.getCurrentVisibleLevel();
        this.isBlockUpdateCurrent = true;
    }

    public int getFluidAmount()
    {
        return this.fluidAmount;
    }

    /**
     * Synchronizes cell with world state. World state generally wins.
     * Clears floor value and forces update of retained level if discovers world lava in a cell thought to be empty.
     * Creates connections if cell was previously a barrier.
     * Does not call changeLevel() to avoid intended side effects of that procedure.
     */
    public void validate(LavaSimulator sim)
    {

        //        if(this.id == 3887)// || this.id == 8187)
        //            Adversity.log.info("boop");



        //        Adversity.log.info("Validating cell " + this.id + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
        //                + " fluidAmount=" + this.fluidAmount + " lastVisibleLevel=" + this.lastVisibleLevel);

        IBlockState myState = sim.worldBuffer.getBlockState(this.packedBlockPos);

        this.isFlowHeightBlock = IFlowBlock.isFlowHeight(myState.getBlock());

        int worldVisibleLevel = this.isFlowHeightBlock 
                ? IFlowBlock.getFlowHeightFromState(myState)
                        : 0;

                boolean isLavaInWorld = worldVisibleLevel > 0 && myState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK;

                // barrier if impassable non-flow block, or if full-height non-lava flow block
                boolean isBarrierInWorld = this.isFlowHeightBlock 
                        ? (!isLavaInWorld && worldVisibleLevel == AbstractLavaSimulator.LEVELS_PER_BLOCK)
                                : !sim.terrainHelper.isLavaSpace(myState);


                        if(isLavaInWorld)
                        {
                            // world has lava - does sim have lava?
                            if(this.lastVisibleLevel > 0)
                            {
                                // yay! We agree with world this is a lava cell.

                                // If we dont' agree on the particulars, world wins.
                                if(worldVisibleLevel != this.lastVisibleLevel)
                                {
                                    //                    traceLog.append(sim.getTickIndex() + " validate level mismatch world=" + worldVisibleLevel + " sim=" + this.lastVisibleLevel + "\n");
                                    this.fluidAmount = worldVisibleLevel * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
                                    this.clearBlockUpdate(sim);
                                }
                            }
                            else
                            {
                                //                traceLog.append(sim.getTickIndex() + " validate sim missing lava world=" + worldVisibleLevel + "\n");
                                // Uh oh! World has lava and we don't!

                                // Not a barrier any longer.
                                // Adding fluid below will cause me to form connections,
                                // so don't need to do that here.
                                this.isBarrier = false;

                                if(DEBUG_MODE && this.interiorFloorLevel != 0)
                                    Adversity.log.info("Internal floor lost due to external lava placement. This is unusual.");

                                // Remove any interior floor established during first validation because we don't know 
                                // how lava got placed in this cell. 
                                this.interiorFloorLevel = 0;

                                // Make us a fluid cell.
                                this.fluidAmount = worldVisibleLevel * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
                                this.lastFlowTick = sim.getTickIndex();
                                this.clearBlockUpdate(sim);
                                this.updateFluidStatus(sim);

                                // Add connections (strongly because we contain lava.)
                                this.addNeighborConnectionsStrongly(sim);

                            }
                        }
                        else
                        {
                            // world doesn't have lava

                            // If we have lava, remove it UNLESS world is open space and we just don't have enough lava to be visible
                            if(this.fluidAmount > 0 && (isBarrierInWorld || this.lastVisibleLevel > 0))
                            {
                                //                traceLog.append(sim.getTickIndex() + " validate world missing lava sim=" + this.lastVisibleLevel + "\n");

                                this.fluidAmount = 0;
                                this.updateFluidStatus(sim);
                                this.clearBlockUpdate(sim);

                            }

                            if(!isBarrierInWorld)
                            {
                                // Should not be a barrier
                                if(this.isBarrier)
                                {
                                    // add connections to nearby fluid cells if we were previously a barrier

                                    // should force update of retained level now that we aren't a barrier
                                    // note that we don't contain lava - should not effect flow in this cell
                                    this.rawRetainedLevel = -1;

                                    this.isBarrier = false;

                                    // weakly because we don't have lava
                                    this.addNeighborConnectionsWeakly(sim);
                                }
                                else if(!this.isFirstValidationComplete)
                                {
                                    // when a cell is first created (especially by world notify event)
                                    // look for nearby lava cells and connect to them if not already
                                    this.addNeighborConnectionsWeakly(sim);
                                }


                                if(this.interiorFloorLevel != worldVisibleLevel)
                                {
                                    // should force update of retained level if floor is new
                                    // note that we don't contain lava - should not effect flow in this cell
                                    this.rawRetainedLevel = -1;
                                    this.interiorFloorLevel = (byte) worldVisibleLevel;
                                }
                            }
                            else if(!this.isBarrier)
                            {
                                this.isBarrier = true;
                                this.interiorFloorLevel = AbstractLavaSimulator.LEVELS_PER_BLOCK;
                                this.rawRetainedLevel = 0;

                                // Make us a barrier.
                                // Remove connections to neighboring cells if there were any.
                                if(this.isFirstValidationComplete)
                                {
                                    this.removeInvalidNeighborConnections(sim);
                                }
                            }
                        }

                        //TODO: remove
                        //        if(this.id == 6281 )
                        //            updateRetainedLevel = true;

                        int previousFloor = this.distanceToFlowFloor;
                        this.updateFloor(sim);

                        // Once established, don't want to update retention unless we have to because having it 
                        // change while laval is flowing causes instability and, as blocks harden, can also ruin
                        // the smooth surface the retention logic tries to create.  
                        // So only update it the cell beneath has changed and we empty and not ready to cool. 
                        // (Cooling might cause lower cells to harden.) Also update if it appears bottom has dropped out.

                        if( isFirstValidationComplete 
                                && previousFloor != this.distanceToFlowFloor 
                                && (this.fluidAmount == 0 || !this.canCool(sim) || (this.rawRetainedLevel > 0 && this.distanceToFlowFloor > AbstractLavaSimulator.LEVELS_PER_BLOCK)))
                        {
                            this.rawRetainedLevel = -1;
                        }

                        this.isFirstValidationComplete = true;

                        //        Adversity.log.info("Validated cell " + this.id + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
                        //                + " fluidAmount=" + this.fluidAmount + " lastVisibleLevel=" + this.lastVisibleLevel);
    }

    protected void updateFloor(LavaSimulator sim)
    {
        // Note that we could simply call getInteriorFloor for this block, but using our internal state 
        // directly prevents a lookup of this cell at the expense of duplicating some of the logic there.
        if(this.isBarrier)
        {
            this.distanceToFlowFloor = 0;
            this.flowFloorIsFlowBlock = this.isFlowHeightBlock;

            if(DEBUG_MODE && this.interiorFloorLevel != 12)
                Adversity.log.info("Invalid value (not 12) of interiorFloor for barrier cell.");

            return;
        }

        if(this.interiorFloorLevel > 0)
        {
            this.distanceToFlowFloor = (byte) (AbstractLavaSimulator.LEVELS_PER_BLOCK - interiorFloorLevel);
            this.flowFloorIsFlowBlock = true;
            return;
        }
        
        //handle special case when at bottom of world somehow
        if(PackedBlockPos.getY(this.packedBlockPos) == 0)
        {
            this.distanceToFlowFloor =  AbstractLavaSimulator.LEVELS_PER_BLOCK;
            this.flowFloorIsFlowBlock = false;
            return;
        }

        // check down 1
        long pos = PackedBlockPos.down(this.packedBlockPos);
        int floor =  getInteriorFloor(sim, pos);

        if (floor == AbstractLavaSimulator.LEVELS_PER_BLOCK)
        {
            this.distanceToFlowFloor =  AbstractLavaSimulator.LEVELS_PER_BLOCK;
            this.flowFloorIsFlowBlock = isFlowHeightBlock(sim, pos);
            return;
        }
        
        if(floor > 0)
        {
            this.distanceToFlowFloor = (byte) (AbstractLavaSimulator.LEVELS_PER_BLOCK * 2 - floor);
            this.flowFloorIsFlowBlock = true;
            return;
        }

        //abort if next block down would be below the world
        if(PackedBlockPos.getY(this.packedBlockPos) == 1)
        {
            this.distanceToFlowFloor = AbstractLavaSimulator.LEVELS_PER_BLOCK * 2;
            this.flowFloorIsFlowBlock = false;
            return;
        }
        
        // check down 2
        pos = PackedBlockPos.down(this.packedBlockPos, 2);
        floor =  getInteriorFloor(sim, pos);

        if (floor == AbstractLavaSimulator.LEVELS_PER_BLOCK)
        {
            this.distanceToFlowFloor =  AbstractLavaSimulator.LEVELS_PER_BLOCK * 2;
            this.flowFloorIsFlowBlock = isFlowHeightBlock(sim, pos);
            return;
        }
        
        if(floor > 0)
        {
            this.distanceToFlowFloor = (byte) (AbstractLavaSimulator.LEVELS_PER_BLOCK * 3 - floor);
            this.flowFloorIsFlowBlock = true;
            return;
        }
        
        //abort if next block down would be below the world
        if(PackedBlockPos.getY(this.packedBlockPos) == 2)
        {
            this.distanceToFlowFloor = FLOW_FLOOR_DISTANCE_REALLY_FAR;
            this.flowFloorIsFlowBlock = false;
            return;
        }
        
        // check down 3
        pos = PackedBlockPos.down(this.packedBlockPos, 3);
        floor =  getInteriorFloor(sim, pos);

        if (floor == AbstractLavaSimulator.LEVELS_PER_BLOCK)
        {
            this.distanceToFlowFloor =  AbstractLavaSimulator.LEVELS_PER_BLOCK * 3;
            this.flowFloorIsFlowBlock = isFlowHeightBlock(sim, pos);
            return;
        }
        
        if(floor > 0)
        {
            this.distanceToFlowFloor = (byte) (AbstractLavaSimulator.LEVELS_PER_BLOCK * 4 - floor);
            this.flowFloorIsFlowBlock = true;
            return;
        }
        
        //abort if next block down would be below the world
        if(PackedBlockPos.getY(this.packedBlockPos) == 3)
        {
            this.distanceToFlowFloor = FLOW_FLOOR_DISTANCE_REALLY_FAR;
            this.flowFloorIsFlowBlock = false;
            return;
        }
        
        // check down 4
        pos = PackedBlockPos.down(this.packedBlockPos, 4);
        floor =  getInteriorFloor(sim, pos);

        if (floor == AbstractLavaSimulator.LEVELS_PER_BLOCK)
        {
            this.distanceToFlowFloor =  AbstractLavaSimulator.LEVELS_PER_BLOCK * 4;
            this.flowFloorIsFlowBlock = isFlowHeightBlock(sim, pos);
            return;
        }
        
        if(floor > 0)
        {
            this.distanceToFlowFloor = (byte) (AbstractLavaSimulator.LEVELS_PER_BLOCK * 5 - floor);
            this.flowFloorIsFlowBlock = true;
            return;
        }
        
        // any floor is pretty far down there, and we don't know if it is a flow block
        this.distanceToFlowFloor = FLOW_FLOOR_DISTANCE_REALLY_FAR;
        this.flowFloorIsFlowBlock = false;
    }

    
    private void updateRawRetention(LavaSimulator sim)
    {
        //        if(this.id == 746)
        //            Adversity.log.info("yurb");

        // if no chance for retention don't bother
        // Note that >= would not work. Can have retention based on floor two below.
        if(this.isBarrier || this.distanceToFlowFloor > AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS)
        {
            this.rawRetainedLevel = 0;
        }
        else if(this.flowFloorIsFlowBlock)
        {
  
            // if floor is not in this block have to base on floor below
            if(this.distanceToFlowFloor > AbstractLavaSimulator.LEVELS_PER_BLOCK)
            {
                int depthBelow = getFlowFloorRetentionDepth(sim, PackedBlockPos.down(this.packedBlockPos));
                
                int distanceToRetention = this.distanceToFlowFloor - depthBelow;
                
                this.rawRetainedLevel = Math.max(0, AbstractLavaSimulator.LEVELS_PER_BLOCK - distanceToRetention) * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
            }
            //floor is in this block (or block has no interior floor - IOW floor is the block below)
            else
            {
                int depth = getFlowFloorRetentionDepth(sim, this.packedBlockPos);
                
                // note that code at bottom of routine handles case when retention is more than a full block
                this.rawRetainedLevel = (this.interiorFloorLevel + depth) * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
            }

        }
        else if(sim.terrainHelper.isLavaSpace(sim.worldBuffer.getBlockState(PackedBlockPos.down(this.packedBlockPos))))
        {
            if(sim.terrainHelper.isLavaSpace(sim.worldBuffer.getBlockState(PackedBlockPos.down(PackedBlockPos.down(this.packedBlockPos)))))
            {
                //if two blocks below is also open/lava, then will have no retained level
                this.rawRetainedLevel = 0;
                return;
            }
            else
            {
                long downPos = PackedBlockPos.down(PackedBlockPos.down(this.packedBlockPos));
                // If two blocks below is a barrier, then will have a retained level
                // when the retained level below is > 1.
                //TODO: optimize for integer math
                //TODO: have terrain helper use packed block coords?
                
                // note that code at bottom of routine handles case when retention is more than a full block
                this.rawRetainedLevel = Math.max(0, (int)((sim.terrainHelper.computeIdealBaseFlowHeight(downPos) - 1F) * AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK));
            }
        }
        else
        {
            this.rawRetainedLevel = (int)(sim.terrainHelper.computeIdealBaseFlowHeight(this.packedBlockPos) * AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK);
        }

        if(this.rawRetainedLevel > AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK)
        {
            // if retained level > full block, want to clamp it at an equilibrium point > than normal block max to support stable surface above
            this.rawRetainedLevel  = this.rawRetainedLevel - (int)((this.rawRetainedLevel - AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK) * LavaCellConnection.INVERSE_PRESSURE_FACTOR);
        }

    }

    /**
     * Returns retained depth of lava on the given flow block in block levels.
     * Must give it a surface, non-barrier cell with a flow-type floor - otherwise always returns 0.
     */
    private static int getFlowFloorRetentionDepthOld(LavaSimulator sim, long atPackedBlockPosition)
    {
        LavaCell centerCell = sim.getCell(atPackedBlockPosition, false);
        
        if(centerCell.isBarrier || !centerCell.flowFloorIsFlowBlock || centerCell.distanceToFlowFloor > AbstractLavaSimulator.LEVELS_PER_BLOCK) return 0;
        
        int eastDrop = Useful.clamp(centerCell.getEastEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int westDrop = Useful.clamp(centerCell.getWestEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int northDrop = Useful.clamp(centerCell.getNorthEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int southDrop = Useful.clamp(centerCell.getSouthEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int centerDrop = Useful.clamp(centerCell.getDistanceToFlowFloor(), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        
        int maxDrop = Math.max(Math.max(eastDrop, westDrop), Math.max(northDrop, southDrop));
        int minDrop = Math.min(Math.min(eastDrop, westDrop), Math.min(northDrop, southDrop));

        /**
         *  Type            myDrop___maxDrop    myDrop___minDrop      minDrop___maxDrop
         *  Pit                    >                   > (implied)            <= (doesn't matter)
         *  Peak                   < (implied)         <                      <= (doesn't matter)
         *  Slope                  <                   >                      <  (implied)
         *  Cliff                  <                   =                      <
         *  Landing                =                   >                      <
         *  Flat                   =                   =                      =
         */
        int depth;
        
        if(centerDrop < maxDrop)
        {
            if(centerDrop == minDrop)
            {
                // Cliff  ⎺⎺\
                
                // should give half to full block, depending on steepness of fall
                depth = AbstractLavaSimulator.LEVELS_PER_BLOCK - Math.min(AbstractLavaSimulator.LEVELS_PER_BLOCK, maxDrop - minDrop) / 2;
            }
            else if(centerDrop > minDrop)
            {
                // Slope  ⎺\_ 
                
                // 1 block slope should give half block, steeper less, down to 1/4.
                // Almost flat slope should give close to 1.
                depth = AbstractLavaSimulator.LEVELS_PER_BLOCK - Math.min(AbstractLavaSimulator.LEVELS_PER_BLOCK_AND_A_HALF, maxDrop - minDrop) / 2;
                
            }
            else  // implies centerDrop < minDrop
            {
                // Peak /⎺\
                
                // should give quarter to half block, depending on steepness of fall
                depth = AbstractLavaSimulator.LEVELS_PER_BLOCK / 2 - Math.min(AbstractLavaSimulator.LEVELS_PER_BLOCK / 4, maxDrop - centerDrop);
            }
        }
        else if(centerDrop == maxDrop)
        {
            if(centerDrop > minDrop)
            {
                // Landing \__
                
                // Should give one to one and a half blocks, depending on steepness
                depth = AbstractLavaSimulator.LEVELS_PER_BLOCK + Math.min(AbstractLavaSimulator.LEVELS_PER_BLOCK, (maxDrop - minDrop)) / 2;
                
            }
            else // implies centerDrop == maxDrop == minDrop
            {
                // Flat ___
                depth = AbstractLavaSimulator.LEVELS_PER_BLOCK;
            }
        }
        else // implies centerDrop > maxDrop
        {
            //  Pit \_/
            depth = AbstractLavaSimulator.LEVELS_PER_BLOCK + Math.min(AbstractLavaSimulator.LEVELS_PER_BLOCK, (centerDrop - minDrop)) / 2;
            
        }
        
        return depth;
    }
    
    /**
     * Returns retained depth of lava on the given flow block in block levels.
     * Must give it a surface, non-barrier cell with a flow-type floor - otherwise always returns 0.
     */
    private static int getFlowFloorRetentionDepth(LavaSimulator sim, long atPackedBlockPosition)
    {
        LavaCell centerCell = sim.getCell(atPackedBlockPosition, false);
        
        if(centerCell.isBarrier || !centerCell.flowFloorIsFlowBlock || centerCell.distanceToFlowFloor > AbstractLavaSimulator.LEVELS_PER_BLOCK) return 0;
        
        int eastDrop = Useful.clamp(centerCell.getEastEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int westDrop = Useful.clamp(centerCell.getWestEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int northDrop = Useful.clamp(centerCell.getNorthEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int southDrop = Useful.clamp(centerCell.getSouthEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        
        int northEastDrop = Useful.clamp(centerCell.getNorthEfficiently(sim).getEastEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int southEastDrop = Useful.clamp(centerCell.getSouthEfficiently(sim).getEastEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int northWestDrop = Useful.clamp(centerCell.getNorthEfficiently(sim).getWestEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int southWestDrop = Useful.clamp(centerCell.getSouthEfficiently(sim).getWestEfficiently(sim).getRelativeDistanceToFlowFloor(sim), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        
        int centerDrop = Useful.clamp(centerCell.getDistanceToFlowFloor(), -AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS, AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);

        int avgDrop = (eastDrop + westDrop + northDrop + southDrop + northEastDrop + southEastDrop + northWestDrop + southWestDrop + centerDrop) / 9;
        
        // Dividing by LEVELS_PER_TWO_BLOCKS normalizes the resulting slope values to the range 0 to 1. 
        float deltaNorthSouth = (northDrop + northEastDrop + northWestDrop - southDrop - southEastDrop - southWestDrop) / 6F / AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS;
        float deltaEastWest = (eastDrop + northEastDrop + southEastDrop - westDrop - northWestDrop - southWestDrop)  / 6F / AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS;
        double slope = Math.sqrt(deltaNorthSouth * deltaNorthSouth + deltaEastWest * deltaEastWest);
      
        int depth = (int) (AbstractLavaSimulator.LEVELS_PER_BLOCK * (1.0 - slope));
        
        // Abandoned experiment...
        // this function gives a value of 1 for slope = 0 then drops steeply 
        // as slope increases and then levels off to 1/4 height as slope approaches 1.
        // Function is only well-behaved for our purpose within the range 0 to 1.
        // More concisely, function is (1-sqrt(x)) ^ 2, applied to the top 3/4 of a full block height.
        // int depth = (int) (0.25 + 0.75 * Math.pow(1 - Math.sqrt(slope), 2));
        
        // Add drop to the average floor instead of my floor, effectively acting as a box filter. 
        // For example, if center drop is 10 below, and average is 12 below, 
        // depth should be 2 less than it would be if it were added to center drop.
        depth += (centerDrop - avgDrop);
        
        //clamp to at least 1/4 of a block and no more than 1.25 block
        depth = Useful.clamp(depth, AbstractLavaSimulator.LEVELS_PER_QUARTER_BLOCK, AbstractLavaSimulator.LEVELS_PER_BLOCK_AND_A_QUARTER);
        
      
        return depth;
    }
    /**
     * Logic to determine if cell/block at the given location contains a solid floor.
     * Returns value 0-12 in the floor result portion.
     * 0 indicates no floor. 12 indicates a barrier, which may also be a flow block.
     */
    private static byte getInteriorFloor(LavaSimulator sim, long packedPosition)
    {

        LavaCell target = sim.getCellIfItExists(packedPosition);
        if(target != null)
        {
            if(target.isBarrier) 
            {
                return AbstractLavaSimulator.LEVELS_PER_BLOCK;
            }
            else
            {
                return target.interiorFloorLevel;
            }
        }
        else
        {
            // Simulation hasn't captured state as a cell, so have to check world directly.
            IBlockState state = sim.worldBuffer.getBlockState(packedPosition);
            if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || LavaTerrainHelper.canLavaDisplace(state))
            {
                // Not a barrier or floor of any kind
                // Note: if it is lava, does not have a floor because we didn't find a cell for it. (orphaned lava)
                return 0;
            }
            else
            {
                int floor = IFlowBlock.getFlowHeightFromState(state);
                return floor == 0 ? AbstractLavaSimulator.LEVELS_PER_BLOCK : (byte) floor; 
            }
        }
    }
    
    public static boolean isFlowHeightBlock(LavaSimulator sim, long packedPosition)
    {
        LavaCell target = sim.getCellIfItExists(packedPosition);
        if(target != null)
        {
            return target.isFlowHeightBlock;
        }
        else
        {
            // Simulation hasn't captured state as a cell, so have to check world directly.
            IBlockState state = sim.worldBuffer.getBlockState(packedPosition);
            return IFlowBlock.isFlowHeight(state.getBlock());
        }
    }

    public int getRawRetainedLevel(LavaSimulator sim)
    {
        if(this.rawRetainedLevel == -1)
        {
            this.updateRawRetention(sim);
        }
        return this.rawRetainedLevel;
    }

    public void setRawRetainedLevel(int level)
    {
        this.rawRetainedLevel = level;
    }

    public boolean isBarrier()
    {
        return this.isBarrier;
    }

    public boolean canCool(AbstractLavaSimulator sim)
    {
        //TODO: make ticks to cool configurable
        return !this.neverCools && this.fluidAmount > 0 && sim.getTickIndex() - this.getLastFlowTick() > 200;
    }

    //    static int[] EXITS = new int[6];

    public BottomType getBottomType(LavaSimulator sim)
    {

        if(this.distanceToFlowFloor <= AbstractLavaSimulator.LEVELS_PER_BLOCK) return BottomType.SUPPORTING;

        LavaCell down = this.getDownEfficiently(sim, false);

        //        if(this.neighborDown == NowhereConnection.INSTANCE) 
        //            {
        ////                EXITS[0]++;
        //                return BottomType.SUPPORTING;
        //            }

        int bottomFluid = down.getFluidAmount();

        if(bottomFluid > 0)
        {

            if(bottomFluid >= AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK)
            {
                // bottom cell is full of fluid
                //                EXITS[1]++;
                return BottomType.SUPPORTING;
            }
            else
            {
                // bottom cell is partially full of fluid
                //                EXITS[2]++;
                return BottomType.PARTIAL;
            }
        }
        else
        {
            int downDist = down.getDistanceToFlowFloor();
            if(downDist == 0)
            {
                // barrier should not occur frequently because connection should not exist long if so
                // but need to handle for transient situations
                //                    EXITS[4]++;
                return BottomType.SUPPORTING;
            }
            else if(downDist < AbstractLavaSimulator.LEVELS_PER_BLOCK)
            {
                //                EXITS[3]++;
                return BottomType.PARTIAL;
            }
            else
            {
                //                    EXITS[5]++;
                return BottomType.DROP;
            }
        }

        //        return this.neighborDown.getBottomType();
    }

    //TODO: remove
    //    private static int retainCallCount = 0;
    //    private static int releaseCallCount = 0;
    //    private static long lastUpdateNanoTime = System.nanoTime();
    //    private StringBuilder builder = new StringBuilder();

    //    /** 
    //     * Don't create cells with less than this amount of fluid.
    //     * Vertical cells with less than this amount will be compressed into the cell below.
    //     */
    //    private final static int MINIMUM_CELL_CONTENT = FLUID_UNITS_PER_BLOCK/24;

    public void retain(AbstractLavaSimulator sim, String desc)
    {

        //        traceLog.append(sim.getTickIndex() + " retain " + desc + System.lineSeparator());

        //TODO: remove
        //        if(System.nanoTime() - lastUpdateNanoTime >= 10000000000L)
        //        {
        //            Adversity.log.info("cell retains/sec=" + retainCallCount / (lastUpdateNanoTime/1000000000));
        //            Adversity.log.info("cell release/sec=" + releaseCallCount / (lastUpdateNanoTime/1000000000));
        //            retainCallCount = 0;
        //            releaseCallCount = 0;
        //            lastUpdateNanoTime = System.nanoTime();
        //        }
        //        retainCallCount++;

        //        Adversity.log.info("retain id=" + this.id);
        this.referenceCount++;
    }

    /**
     * Called once per tick for each cell to transitions between having and not having any fluid.
     * Maintains the list of connections with cells that have fluid.
     * Also ensures we retain all cells neighboring fluid-containing cells
     * so that they can be validated against the game world to check for breaks.
     */
    protected void updateFluidStatus(LavaSimulator sim)
    {
        if(this.getFluidAmount() > 0)
        {
            if(!this.hasFluidStatus)
            {
                this.hasFluidStatus = true;
                this.retain(sim, "updateFluidStatus self");
                this.addNeighborConnectionsStrongly(sim);
            }
        }
        else
        {
            if(this.hasFluidStatus)
            {
                this.hasFluidStatus = false;
                this.release(sim, "updateFluidStatus self");
                this.removeInvalidNeighborConnections(sim);
            }
        }
    }

    /** adds connections to cells that are not barriers */
    private void addNeighborConnectionsStrongly(LavaSimulator sim)
    {
        LavaCell other = sim.getCell(PackedBlockPos.up(this.packedBlockPos), false);
        if(other != null && !other.isBarrier())
        {
            sim.addConnection(ConnectionMap.getUpConnectionFromPackedBlockPos(this.packedBlockPos));
        }
        other = sim.getCell(PackedBlockPos.down(this.packedBlockPos), false);
        if(other != null && !other.isBarrier())
            sim.addConnection(ConnectionMap.getDownConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCell(PackedBlockPos.east(this.packedBlockPos), false);
        if(other != null && !other.isBarrier())
            sim.addConnection(ConnectionMap.getEastConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCell(PackedBlockPos.west(this.packedBlockPos), false);
        if(other != null && !other.isBarrier())
            sim.addConnection(ConnectionMap.getWestConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCell(PackedBlockPos.north(this.packedBlockPos), false);
        if(other != null && !other.isBarrier())
            sim.addConnection(ConnectionMap.getNorthConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCell(PackedBlockPos.south(this.packedBlockPos), false);
        if(other != null && !other.isBarrier())
            sim.addConnection(ConnectionMap.getSouthConnectionFromPackedBlockPos(this.packedBlockPos));
    }


    /** adds connections to cells that have fluid */
    private void addNeighborConnectionsWeakly(LavaSimulator sim)
    {
        LavaCell other = sim.getCellIfItExists(PackedBlockPos.up(this.packedBlockPos));
        if(other != null && other.fluidAmount > 0)
        {
            sim.addConnection(ConnectionMap.getUpConnectionFromPackedBlockPos(this.packedBlockPos));
        }
        other = sim.getCellIfItExists(PackedBlockPos.down(this.packedBlockPos));
        if(other != null && other.fluidAmount > 0)
            sim.addConnection(ConnectionMap.getDownConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCellIfItExists(PackedBlockPos.east(this.packedBlockPos));
        if(other != null && other.fluidAmount > 0)
            sim.addConnection(ConnectionMap.getEastConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCellIfItExists(PackedBlockPos.west(this.packedBlockPos));
        if(other != null && other.fluidAmount > 0)
            sim.addConnection(ConnectionMap.getWestConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCellIfItExists(PackedBlockPos.north(this.packedBlockPos));
        if(other != null && other.fluidAmount > 0)
            sim.addConnection(ConnectionMap.getNorthConnectionFromPackedBlockPos(this.packedBlockPos));

        other = sim.getCellIfItExists(PackedBlockPos.south(this.packedBlockPos));
        if(other != null && other.fluidAmount > 0)
            sim.addConnection(ConnectionMap.getSouthConnectionFromPackedBlockPos(this.packedBlockPos));
    }

    private void removeInvalidNeighborConnections(LavaSimulator sim)
    {
        if(this.neighborUp != NowhereConnection.INSTANCE)
            sim.removeConnectionIfInvalid(ConnectionMap.getUpConnectionFromPackedBlockPos(this.packedBlockPos));

        if(this.neighborDown != NowhereConnection.INSTANCE)
            sim.removeConnectionIfInvalid(ConnectionMap.getDownConnectionFromPackedBlockPos(this.packedBlockPos));

        if(this.neighborEast != NowhereConnection.INSTANCE)
            sim.removeConnectionIfInvalid(ConnectionMap.getEastConnectionFromPackedBlockPos(this.packedBlockPos));

        if(this.neighborWest != NowhereConnection.INSTANCE)
            sim.removeConnectionIfInvalid(ConnectionMap.getWestConnectionFromPackedBlockPos(this.packedBlockPos));

        if(this.neighborNorth != NowhereConnection.INSTANCE)
            sim.removeConnectionIfInvalid(ConnectionMap.getNorthConnectionFromPackedBlockPos(this.packedBlockPos));

        if(this.neighborSouth != NowhereConnection.INSTANCE)
            sim.removeConnectionIfInvalid(ConnectionMap.getSouthConnectionFromPackedBlockPos(this.packedBlockPos));
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

    public void release(AbstractLavaSimulator sim, String desc)
    {
        //        traceLog.append(sim.getTickIndex() + " release " + desc + System.lineSeparator());

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

    /**
     * Call when removing from collection so any retained references are no longer used.
     */
    public void setDeleted(AbstractLavaSimulator sim)
    {
        //        traceLog.append(sim.getTickIndex() + " setDeleted\n");
        this.isDeleted = true;
    }

    public boolean getNeverCools()
    {
        return this.neverCools;
    }

    public int getInteriorFloor()
    {
        return this.interiorFloorLevel;
    }

    /** 
     * Distance from the top of this cell to the bottom surface that should be used
     * for computing a retention level and determine connection drop. 
     * 
     * Represented as fluid levels.  (12 per block.)
     * Sample values:
     *  0  - this block is a barrier.
     *  6  - this block has/had a solid floor half high
     *  12 - this block does not have a solid floor within it & block below is a barrier.
     *  18 - this block does not have a solid floor within it & block below has/had a solid floor half high
     *  24 - this block and block below do not have a solid floor within them & block 2 below is a barrier
     *  MAX_FLOW_FLOOR_DISTANCE - the surface is pretty far down there, more than 2 blocks.
     */
    public byte getDistanceToFlowFloor()
    {
        return this.distanceToFlowFloor;
    }
    
    /**
     * Identical to getDistanceToFlowFloor...
     * EXCEPT:
     *      If this block is a barrier, returns negative number indicating how high above
     *      this block the flow floor is located, up to -24 (two blocks above).
     */
    public int getRelativeDistanceToFlowFloor(LavaSimulator sim)
    {
        if(this.distanceToFlowFloor == 0)
        {
            LavaCell up = this.getUpEfficiently(sim, false);
            if(up.isBarrier)
            {
               LavaCell up2 = up.getUpEfficiently(sim, false);
               if(up2.isBarrier)
               {
                   return up.distanceToFlowFloor - AbstractLavaSimulator.LEVELS_PER_BLOCK * 2;
               }
            }
            else
            {
                return up.distanceToFlowFloor - AbstractLavaSimulator.LEVELS_PER_BLOCK;
            }
            return this.getUpEfficiently(sim, false).getRelativeDistanceToFlowFloor(sim) - AbstractLavaSimulator.LEVELS_PER_BLOCK;
        }
        else
        {
            return this.distanceToFlowFloor;
        }
    }

    public boolean flowFloorIsFlowBlock()
    {
        return this.flowFloorIsFlowBlock;
    }

    /** Uses connection to avoid a hash lookup when possible */
    public LavaCell getEastEfficiently(LavaSimulator sim)
    {
        return this.neighborEast.goesNowhere() ? sim.getCell(PackedBlockPos.east(this.packedBlockPos), false) : neighborEast.getOther(this);
    }

    /** Uses connection to avoid a hash lookup when possible */
    public LavaCell getWestEfficiently(LavaSimulator sim)
    {
        return this.neighborWest.goesNowhere() ? sim.getCell(PackedBlockPos.west(this.packedBlockPos), false) : neighborWest.getOther(this);
    }

    /** Uses connection to avoid a hash lookup when possible */
    public LavaCell getNorthEfficiently(LavaSimulator sim)
    {
        return this.neighborNorth.goesNowhere() ? sim.getCell(PackedBlockPos.north(this.packedBlockPos), false) : neighborNorth.getOther(this);
    }

    /** Uses connection to avoid a hash lookup when possible */
    public LavaCell getSouthEfficiently(LavaSimulator sim)
    {
        return this.neighborSouth.goesNowhere() ? sim.getCell(PackedBlockPos.south(this.packedBlockPos), false) : neighborSouth.getOther(this);
    }

    /**
     * Caches reference to avoid a hash lookup when possible.
     */
    public LavaCell getDownEfficiently(LavaSimulator sim, boolean shouldRefreshIfExists)
    {
        if(this.neighborDown.goesNowhere())
        {
            if(this.bottomNeighbor == null || this.bottomNeighbor.isDeleted)
            {
                this.bottomNeighbor = sim.getCell(PackedBlockPos.down(this.packedBlockPos), false);
            }
            return this.bottomNeighbor;
        }
        else
        {
            return this.neighborDown.getOther(this);
        }
    }

    /**
     * Caches reference to avoid a hash lookup when possible.
     */
    public LavaCell getDownEfficientlyIfExists(LavaSimulator sim)
    {
        if(this.neighborDown.goesNowhere())
        {
            if(this.bottomNeighbor == null || this.bottomNeighbor.isDeleted)
            {
                this.bottomNeighbor = sim.getCellIfItExists(PackedBlockPos.down(this.packedBlockPos));
            }
            return this.bottomNeighbor;
        }
        else
        {
            return this.neighborDown.getOther(this);
        }
    }

    /**
     * Caches reference to avoid a hash lookup when possible.
     */
    public LavaCell getUpEfficiently(LavaSimulator sim, boolean shouldRefreshIfExists)
    {
        if(this.upNeighbor == null || this.upNeighbor.isDeleted)
        {
            this.upNeighbor = sim.getCell(PackedBlockPos.up(this.packedBlockPos), false);
        }
        return this.upNeighbor;
    }


    /**
     * How much lava can this cell accept if being added at top.
     * Used by simulation addLava method.
     * Takes floor into account if this is an empty cell witha flow bottom.
     */
    public int getCapacity()
    {
        return this.isBarrier ? 0 : Math.max(0, Math.max(AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK, this.rawRetainedLevel) - Math.max(this.interiorFloorLevel, this.fluidAmount));
    }

    /** for use by NBT loader */
    public void setInteriorFloor(byte floor)
    {
        this.interiorFloorLevel = floor;
    }
}