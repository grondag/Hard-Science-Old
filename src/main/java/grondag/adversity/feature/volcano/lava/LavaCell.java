package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;
import static grondag.adversity.Adversity.DEBUG_MODE;
import grondag.adversity.feature.volcano.lava.LavaCellConnection.BottomType;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class LavaCell
{
    public static final byte LEVELS_PER_BLOCK = FlowHeightState.BLOCK_LEVELS_INT;
    public static final byte LEVELS_PER_BLOCK_AND_A_HALF = LEVELS_PER_BLOCK + LEVELS_PER_BLOCK / 2;
    public static final int FLUID_UNITS_PER_LEVEL = 1000;
    public static final int FLUID_UNITS_PER_BLOCK = FLUID_UNITS_PER_LEVEL * LEVELS_PER_BLOCK;
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
     * Can be > FLUID_UNITS_PER_BLOCK if block above should also retain fluid,
     */
    private int retainedLevel;

    /** for tracking block updates */
    private int lastVisibleLevel = 0;

    public final long packedBlockPos;

    private final int id;

    private boolean isBarrier;
    
    /** 
     * True if this cell represents a flow height block. (Solid or liquid.)
     * Updated during every validate.
     */
    private boolean isFlowHeightBlock;
    
    private static final byte FLOW_FLOOR_DISTANCE_REALLY_FAR = Byte.MAX_VALUE;
    private static final byte FLOW_FLOOR_DISTANCE_UNKNOWN = -1;
    
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
    }
    
    public void changeLevel(LavaSimulator sim, int amount)
    {
//        if(this.id == 1104 || this.id == 8187)
//            Adversity.log.info("boop");
                
        if(amount != 0)
        {

            this.lastFlowTick = sim.getTickIndex();

            if(amount > 0)
            {
                if(this.fluidAmount == 0)
                {
                    //TODO - already checked this earlier in get flow so any way to prevent a recheck?
                    // If this is an empty drop cell, queue a drop particle instead of adding the lava
                    // Note - can't do this if the cell somehow has lava - particle will just be reabsorbed.
                    if(this.getBottomType(sim) == BottomType.DROP)
                    {
//                        Adversity.log.info("LavaCell id=" + this.id + " with level =" + this.fluidAmount + " changeLevel diverted to particle: amount=" + amount +" @"
//                                + PackedBlockPos.unpack(this.packedBlockPos).toString());
                        
                        sim.queueParticle(this.packedBlockPos, amount);
                        return;
                    }
                    else
                    {
                        // When fluid is first added to a cell with a floor - we melt the floor and include it in the lava.
                        // Necessary because we don't have any kind of multipart capability yet to retain pre-existing block.
                        if(this.interiorFloorLevel > 0) this.fluidAmount = FLUID_UNITS_PER_LEVEL * this.interiorFloorLevel;
//                        this.fluidAmount = this.floorLevel;
                    }
                }
            }

//            Adversity.log.info("changeLevel for cell " + this.id + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
//                    + " delta=" + amount + " priorfluidAmount=" + this.fluidAmount + " lastVisibleLevel=" + this.lastVisibleLevel);
            
            this.fluidAmount += amount;
            
            // Don't want to do these next two if spawned a particle and exited - because we're still an  empty cell - no change.
            isBlockUpdateCurrent = false;
            this.makeAllConnectionsDirty();
            
            if(this.fluidAmount < 0)
            {
                Adversity.log.info("Negative cell level detected: " + this.fluidAmount + " cellID=" + this.id + " pos=" 
                        + PackedBlockPos.unpack(this.packedBlockPos).toString());
                this.fluidAmount = 0;
            }

            /**
             * If we have a floor and we've somehow drained below it, floor must be lowered to the new fluid level because
             * the solid surface it represents can no longer exist.
             */
//            if(this.fluidAmount < this.floorLevel) this.floorLevel = this.fluidAmount;
            
            if(this.interiorFloorLevel > 0 && this.fluidAmount < FLUID_UNITS_PER_LEVEL * this.interiorFloorLevel)
            {
                this.interiorFloorLevel = (byte) (this.fluidAmount / FLUID_UNITS_PER_LEVEL);
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

//        if(this.id == 1104 || this.id == 8187)
//            Adversity.log.info("boop");
        
        
        int currentVisible = this.getCurrentVisibleLevel();
        if(this.lastVisibleLevel != currentVisible)
        {
//            Adversity.log.info("Providing block update for cell " + this.id + " @" + PackedBlockPos.unpack(this.packedBlockPos).toString()
//                    + " currentVisibleLevel=" + currentVisible + " lastVisibleLevel=" + this.lastVisibleLevel);

            LavaSimulator.blockUpdatesProvisionCounter++;
            
            final IBlockState priorState = sim.worldBuffer.getBlockState(this.packedBlockPos);
            if(currentVisible == 0)
            {
                if(priorState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    sim.worldBuffer.setBlockState(this.packedBlockPos, Blocks.AIR.getDefaultState(), priorState);
                }
            }
            else
            {
                sim.worldBuffer.setBlockState(this.packedBlockPos, 
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
        if(result > LEVELS_PER_BLOCK) result = LEVELS_PER_BLOCK;
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

//        if(this.id == 5881)// || this.id == 8187)
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
                ? (!isLavaInWorld && worldVisibleLevel == LEVELS_PER_BLOCK)
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
                    this.fluidAmount = worldVisibleLevel * FLUID_UNITS_PER_LEVEL;
                    this.clearBlockUpdate();
                }
            }
            else
            {
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
                this.fluidAmount = worldVisibleLevel * FLUID_UNITS_PER_LEVEL;
                this.lastFlowTick = sim.getTickIndex();
                this.clearBlockUpdate();
                
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
                this.fluidAmount = 0;
                this.clearBlockUpdate();

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
                    // weakly because we don't have lava
                    this.addNeighborConnectionsWeakly(sim);
                }


                if(this.interiorFloorLevel != worldVisibleLevel)
                {
                    // should force update of retained level if floor is new
                    updateRetainedLevel = true;
                    this.interiorFloorLevel = (byte) worldVisibleLevel;
                }
            }
            else if(!this.isBarrier)
            {
                this.isBarrier = true;
                this.interiorFloorLevel = LEVELS_PER_BLOCK;
                this.retainedLevel = 0;
                
                // Make us a barrier.
                // Remove connections to neighboring cells if there were any.
                if(this.isFirstValidationComplete)
                {
                  this.removeInvalidNeighborConnections(sim);
                }
            }
        }
        
        if(updateRetainedLevel) 
        {
            this.updateFloor(sim);
            this.updateRetention(sim);
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
        
        //handle special case when at bottom of world somehow
        if(PackedBlockPos.getY(this.packedBlockPos) == 0)
        {
            this.distanceToFlowFloor = FLOW_FLOOR_DISTANCE_REALLY_FAR;
            this.flowFloorIsFlowBlock = false;
        }
        
        int floorBelow =  getInteriorFloor(sim, PackedBlockPos.down(this.packedBlockPos));
        
        // If we have an internal floor in this block, we can use it, 
        // IF it is supported by a barrier block below (because of melting.)
        
        
        if (this.interiorFloorLevel != 0 && (floorBelow & INTERIOR_FLOOR_RESULT_LEVEL_MASK) == LEVELS_PER_BLOCK)
        {
            if(DEBUG_MODE && !this.isFlowHeightBlock)
                Adversity.log.info("Inconsistency detected : non-zero interior flow in a non-flow block");

            this.distanceToFlowFloor = (byte) (LEVELS_PER_BLOCK - interiorFloorLevel);
            this.flowFloorIsFlowBlock = true;
            return;
        }
        
        //if we get here, we don't have a floor in this block or it isn't supported, so have to look down.
        if(floorBelow > 0)
        {
            this.distanceToFlowFloor = (byte) (LEVELS_PER_BLOCK * 2 - (floorBelow & INTERIOR_FLOOR_RESULT_LEVEL_MASK));
            this.flowFloorIsFlowBlock = (floorBelow & INTERIOR_FLOOR_RESULT_FLOW_MASK) == INTERIOR_FLOOR_RESULT_FLOW_MASK;
            return;
        }
        
        //abort if next block down would be below the world
        if(PackedBlockPos.getY(this.packedBlockPos) == 1)
        {
            this.distanceToFlowFloor = FLOW_FLOOR_DISTANCE_REALLY_FAR;
            this.flowFloorIsFlowBlock = false;
            return;
        }
        
        //block down was a bust, try one more
        floorBelow =  getInteriorFloor(sim, PackedBlockPos.down(this.packedBlockPos, 2));
        
        if(floorBelow > 0)
        {
            this.distanceToFlowFloor = (byte) (LEVELS_PER_BLOCK * 3 - (floorBelow & INTERIOR_FLOOR_RESULT_LEVEL_MASK));
            this.flowFloorIsFlowBlock = (floorBelow & INTERIOR_FLOOR_RESULT_FLOW_MASK) == INTERIOR_FLOOR_RESULT_FLOW_MASK;
            return;
        }
        
        // any floor is pretty far down there, and we don't know if it is a flow block
        this.distanceToFlowFloor = FLOW_FLOOR_DISTANCE_REALLY_FAR;
        this.flowFloorIsFlowBlock = false;
        return;
    }

    private void updateRetention(LavaSimulator sim)
    {
        if(this.isBarrier || this.distanceToFlowFloor == FLOW_FLOOR_DISTANCE_REALLY_FAR)
        {
            this.retainedLevel = 0;
        }
        else if(this.flowFloorIsFlowBlock)
        {
            
            //TODO: BUG - should not use drop here
            int drop = Math.max(
                    Math.max(this.neighborEast.getOtherDistanceToFlowFloor(this), this.neighborWest.getOtherDistanceToFlowFloor(this)), 
                    Math.max(this.neighborNorth.getOtherDistanceToFlowFloor(this), this.neighborSouth.getOtherDistanceToFlowFloor(this))
                ) - this.getDistanceToFlowFloor();
            
            if(drop > LEVELS_PER_BLOCK) drop = LEVELS_PER_BLOCK;
            
            if(drop <= 0)
            {
                this.retainedLevel = FLUID_UNITS_PER_BLOCK;
            }
            else
            {
                // should result in half block retention for steep slopes graduation to full block for flat areas
                this.retainedLevel = Math.max(0, (LEVELS_PER_BLOCK_AND_A_HALF - this.distanceToFlowFloor - drop / 2)) * FLUID_UNITS_PER_BLOCK;
            }
        }
        else if(sim.terrainHelper.isLavaSpace(sim.worldBuffer.getBlockState(PackedBlockPos.down(this.packedBlockPos))))
        {
            if(sim.terrainHelper.isLavaSpace(sim.worldBuffer.getBlockState(PackedBlockPos.down(PackedBlockPos.down(this.packedBlockPos)))))
            {
                //if two blocks below is also open/lava, then will have no retained level
                this.retainedLevel = 0;
                return;
            }
            else
            {
                BlockPos downPos = PackedBlockPos.unpack(PackedBlockPos.down(this.packedBlockPos));
                // If two blocks below is a barrier, then will have a retained level
                // when the retained level below is > 1.
                //TODO: optimize for integer math
                //TODO: have terrain helper use packed block coords?
                this.retainedLevel = Math.max(0, (int)((sim.terrainHelper.computeIdealBaseFlowHeight(downPos) - 1F) * FLUID_UNITS_PER_BLOCK));
            }
        }
        else
        {
            this.retainedLevel = (int)(sim.terrainHelper.computeIdealBaseFlowHeight(PackedBlockPos.unpack(this.packedBlockPos)) * FLUID_UNITS_PER_BLOCK);
        }


        if(this.retainedLevel > FLUID_UNITS_PER_BLOCK)
        {
            // if retained level > full block, want to clamp it at an equilibrium point > than normal block max to support stable surface above
            this.retainedLevel  = this.retainedLevel - (int)((this.retainedLevel - FLUID_UNITS_PER_BLOCK) * LavaCellConnection.INVERSE_PRESSURE_FACTOR);
        }
    }
    
    /** bits in the return value from getInteriorFloor that indicate level */
    private static final byte INTERIOR_FLOOR_RESULT_LEVEL_MASK = 0xF;
    /** bit in the return value from getInteriorFloor that, if set, indicates it is a flow block. */
    private static final byte INTERIOR_FLOOR_RESULT_FLOW_MASK = 0x10;
    
    /**
     * Logic to determine if cell/block at the given location contains a solid floor.
     * Returns value 0-12 in the floor result portion.
     * 0 indicates no floor. 12 indicates a barrier.
     * For barriers, the value of bit in FLOW_MASK is set to 1 if it is a flow block.
     * To be consistent, the flow bit is also set for values 1-to-11.
     */
    private static byte getInteriorFloor(LavaSimulator sim, long packedPosition)
    {

        LavaCell target = sim.getCellIfItExists(packedPosition);
        if(target != null)
        {
            if(target.isBarrier) 
            {
                return target.isFlowHeightBlock 
                        ? LEVELS_PER_BLOCK | INTERIOR_FLOOR_RESULT_FLOW_MASK 
                        : LEVELS_PER_BLOCK;
            }
            
            //handle special case when at bottom of world somehow
            if(PackedBlockPos.getY(packedPosition) == 0)
            {
                return 0;
            }
            
            if(target.interiorFloorLevel > 0 && PackedBlockPos.getY(packedPosition) > 0)
            {
                //found a flow floor, but need to confirm block below it is a barrier.
                if((getInteriorFloor(sim, PackedBlockPos.down(packedPosition)) & INTERIOR_FLOOR_RESULT_LEVEL_MASK) == LEVELS_PER_BLOCK)
                {
                    //block below is a barrier, so floor is valid
                    return (byte) (target.interiorFloorLevel | INTERIOR_FLOOR_RESULT_FLOW_MASK);
                }
                else
                {
                    //block below is not a barrier, so floor doesn't count because it would melt
                    return 0;
                }
            }
            else
            {
                return 0;
            }
        }
        else
        {
            // Simulation hasn't captured bottom state as a cell, so have to check world directly.
            IBlockState state = sim.worldBuffer.getBlockState(packedPosition);
            if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || LavaTerrainHelper.canLavaDisplace(state))
            {
                // Not a barrier or floor of any kind
                // Note: if it is lava, does not have a floor because we didn't find a cell for it. (orphaned lava)
                return 0;
            }
            else
            {
                int bottomFloor = IFlowBlock.getFlowHeightFromState(state);
                if(bottomFloor == 0) 
                {
                    // Not a flow block, treat as barrier
                    return LEVELS_PER_BLOCK;
                }
                else if(bottomFloor == FlowHeightState.BLOCK_LEVELS_INT)
                {
                    // Flow-type barrier, won't melt, no confirmation needed.
                    return LEVELS_PER_BLOCK | INTERIOR_FLOOR_RESULT_FLOW_MASK;
                }
                else
                {
                    // Solid flow block, but confirm block below it is solid before we use as the floor
                    if((getInteriorFloor(sim, PackedBlockPos.down(packedPosition)) & INTERIOR_FLOOR_RESULT_LEVEL_MASK) == LEVELS_PER_BLOCK)
                    {
                        //block below is a barrier, so floor is valid
                        return (byte) (bottomFloor | INTERIOR_FLOOR_RESULT_FLOW_MASK);
                    }
                    else
                    {
                        //block below is not a barrier, so floor doesn't count because it would melt
                        return 0;
                    }
                }
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
        return !this.neverCools && this.fluidAmount > 0 && sim.getTickIndex() - this.getLastFlowTick() > 200000;
    }

//    static int[] EXITS = new int[6];
  
    public BottomType getBottomType(LavaSimulator sim)
    {
        LavaCell down = this.getDownEfficiently(sim, false);
        
//        if(this.neighborDown == NowhereConnection.INSTANCE) 
//            {
////                EXITS[0]++;
//                return BottomType.SUPPORTING;
//            }
        
        int bottomFluid = down.getFluidAmount();
        
        if(bottomFluid > 0)
        {
            
            if(bottomFluid > LavaCell.FLUID_UNITS_PER_BLOCK)
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
            // Use getDistanceToFlowFloor instead of interior floor bc properly handles melting
            // IOW - will not show a floor unless it is supported.
            int downDist = down.getDistanceToFlowFloor();
            if(downDist == 0)
            {
                // barrier should not occur frequently because connection should not exist long if so
                // but need to handle for transient situations
//                    EXITS[4]++;
                return BottomType.SUPPORTING;
            }
            else if(downDist < LEVELS_PER_BLOCK)
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

    public void retain(String desc)
    {

//        builder.append("retain " + desc + System.lineSeparator());

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
                this.retain("updateFluidStatus self");
                this.addNeighborConnectionsStrongly(sim);
            }
        }
        else
        {
            if(this.hasFluidStatus)
            {
                this.hasFluidStatus = false;
                this.release("updateFluidStatus self");
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
    
    public void release(String desc)
    {
//        builder.append("release " + desc + System.lineSeparator());

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
    public void setDeleted()
    {
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
     * Solid Flow block floors only count if they are full height (and thus cannot melt)
     * OR they have a solid block under them. This is because they will melt if filled with lava
     * and so the floor won't hold unless we want to allow floating lava in the world. (nope)
     * 
     * Represented as fluid levels.  (12 per block.)
     * Sample values:
     *  FLOW_FLOOR_DISTANCE_UNKNOWN  - unknown how far, needs to be recomputed
     *  0  - this block is a barrier.
     *  6  - this block has/had a solid floor half high
     *  12 - this block does not have a solid floor within it & block below is a barrier.
     *  18 - this block does not have a solid floor within it & block below has/had a solid floor half high
     *  24 - this block and block below do not have a solid floor within them & block 2 below is a barrier
     *  24 - this block DOES have a solid floor within it but the block below is not a barrier and my floor will melt if filled.
     *  MAX_FLOW_FLOOR_DISTANCE - the surface is pretty far down there, more than 2 blocks.
     */
    public byte getDistanceToFlowFloor()
    {
        return this.distanceToFlowFloor;
    }
    
    public boolean flowFloorIsFlowBlock()
    {
        return this.flowFloorIsFlowBlock;
    }
    
    /**
     * Caches reference to avoid a hash lookup when possible.
     */
    public LavaCell getDownEfficiently(LavaSimulator sim, boolean shouldRefreshIfExists)
    {
        if(this.bottomNeighbor == null || this.bottomNeighbor.isDeleted)
        {
            this.bottomNeighbor = sim.getCell(PackedBlockPos.down(this.packedBlockPos), false);
        }
        return this.bottomNeighbor;
    }
    
    /**
     * Caches reference to avoid a hash lookup when possible.
     */
    public LavaCell getDownEfficientlyIfExists(LavaSimulator sim)
    {
        if(this.bottomNeighbor == null || this.bottomNeighbor.isDeleted)
        {
            this.bottomNeighbor = sim.getCellIfItExists(PackedBlockPos.down(this.packedBlockPos));
        }
        return this.bottomNeighbor;
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
        return this.isBarrier ? 0 : Math.max(0, Math.max(FLUID_UNITS_PER_BLOCK, this.retainedLevel) - Math.max(this.interiorFloorLevel, this.fluidAmount));
    }

    /** for use by NBT loader */
    public void setInteriorFloor(byte floor)
    {
        this.interiorFloorLevel = floor;
    }
}