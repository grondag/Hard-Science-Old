package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

    private final static float PRESSURE_FACTOR = 1.05F;
//    private final static float PRESSURE_FACTOR_INVERSE = 1/1.05F;
    
    //TODO: make configurable
    private final static float MIN_FLOW = 0.001F;
    
    private boolean isBarrier;
    
//    private boolean isDeleted;
    
    @Override 
    public int hashCode()
    {
        return this.id;
    }
    
    /**
     * Creates a lava cell without any notification or addition to collections.
     */
    public LavaCell(LavaSimulator tracker, BlockPos pos)
    {
     this(tracker, pos, 0);
    }
    
    /**
     * Creates a lava cell without any notification or addition to collections.
     */
    public LavaCell(LavaSimulator tracker, BlockPos pos, float level)
    {
        this.pos = pos;
        this.currentLevel = level;
                
        
        if(pos.getX() == 70 && pos.getY() == 79 && pos.getZ() == 110)
            Adversity.log.info("boop");
        
        this.id = nextCellID++;
        
        if(tracker == null || pos == null) return;
    }
    
//    /**
//     * True if rests on a solid surface.  
//     * Particles that collide with this cell should add to it.
//     */
//    public boolean isCellOnGround(LavaSimulator tracker)
//    {
//        return this.retainedLevel > 0 && !tracker.terrainHelper.isLavaSpace(tracker.world.getBlockState(pos.down()));
//    }
    
    protected LavaCell getNeighbor(LavaSimulator tracker, EnumFacing face)
    {  
        //TODO: performance?  
        //Suspect DOWN and UP are most accessed - could store cells in 256-tall vertical arrays within an x,z hash table
        //Vertical access would then be very fast without need to cache references
        return tracker.getCell(pos.add(face.getDirectionVec()));
    }
    
    public void doStep(LavaSimulator tracker, double seconds)
    {
        //TODO: remove
        if(this.retainedLevel < 0)
        {
            Adversity.log.info("DERP!");
        }
        
        float available = this.currentLevel - this.retainedLevel;
        float amountDonated = 0;
        
        if(available <= 0) return;
        
//        Adversity.log.info("LavaSimCell doStep id=" + this.id + " w/ level=" + this.currentLevel + " @" + this.pos.toString());

        //fall down if possible
        LavaCell down = this.getNeighbor(tracker, EnumFacing.DOWN);
        if(down.canAcceptFluidParticles(tracker))
        {
//            Adversity.log.info("LavaSimCell fall down id=" + this.id + " amount=" + available + " @" + this.pos.toString());
            tracker.world.spawnEntityInWorld(new EntityLavaParticle(tracker.world, this.currentLevel, 
                    new Vec3d(this.pos.getX() + 0.5, this.pos.getY() - 0.1, this.pos.getZ() + 0.5), Vec3d.ZERO));
            this.changeLevel(tracker, -available);
            return;
        }
        
        //flow down if possible
        
        //get pressure from cell above
        LavaCell up = this.getNeighbor(tracker, EnumFacing.UP);
        float pressureFromAbove = up.getDownwardPressure();
                            
        //output down if vertical pressure is sufficient

        
        if(down.canAcceptFluidDirectly(tracker) && down.currentLevel < this.getDownwardPressure())
        {
            float amount = Math.min(available, this.getDownwardPressure() - down.currentLevel);
            available -= amount;
            amountDonated += amount;
            this.changeLevel(tracker, -amount);
            down.changeLevel(tracker, amount);
            
//            Adversity.log.info("LavaSimCell flow down id=" + this.id + " verticalPressure=" + pressureFromAbove + " amount=" + amount + " @" + this.pos.toString());
        }
        
        if(available < MIN_FLOW) return;

        LavaCell east = this.getNeighbor(tracker, EnumFacing.EAST);
        LavaCell west = this.getNeighbor(tracker, EnumFacing.WEST);
        LavaCell north = this.getNeighbor(tracker, EnumFacing.NORTH);
        LavaCell south = this.getNeighbor(tracker, EnumFacing.SOUTH);
        
        // NOTE: THESE GET REUSED WITHOUT REINITIALIZATION BELOW
        // Is okay because will be empty if we get there
        // because if any side can accept particles we are done.
        LavaCell[] sides = new LavaCell[4];
        int sideCount = 0;
        
        //fall to sides if possible
        {
            if(east.canAcceptFluidParticles(tracker)) sides[sideCount++] = east;
            if(west.canAcceptFluidParticles(tracker)) sides[sideCount++] = west;
            if(north.canAcceptFluidParticles(tracker)) sides[sideCount++] = north;
            if(south.canAcceptFluidParticles(tracker)) sides[sideCount++] = south;
        
            if(sideCount > 0)
            {
                for(int i = 0; i < sideCount; i++)
                {
                    tracker.world.spawnEntityInWorld(new EntityLavaParticle(tracker.world, available / sideCount, 
                            new Vec3d(sides[i].pos.getX() + 0.5, sides[i].pos.getY() + 0.1, sides[i].pos.getZ() + 0.5), Vec3d.ZERO));
                }
                
//                Adversity.log.info("LavaSimCell particle(s) sideways id=" + this.id + " amount=" + available + " @" + this.pos.toString());

                this.changeLevel(tracker, -available);
                return;
            }
        }
        
        // NB: WILL NOT GET TO THIS POINT IF CAN OUTPUT A PARTICLE TO ANY SIDE
 
        // Find sides that would be lower than our level,
        // NOPE, REMOVED(...even after we equalize vertical pressure).
        float sideTotal = 0;
        {
//            float forecastedNewLevel = Math.min(verticalPressure, this.currentLevel - amountDonated);
            float forecastedNewLevel = this.currentLevel - amountDonated;
            
            if(east.canAcceptFluidDirectly(tracker) && east.currentLevel < forecastedNewLevel)
            {
                sides[sideCount++] = east;
                sideTotal += east.currentLevel;
            }
            if(west.canAcceptFluidDirectly(tracker) && west.currentLevel < forecastedNewLevel)
            {
                sides[sideCount++] = west;
                sideTotal += west.currentLevel;
            }
            if(north.canAcceptFluidDirectly(tracker) && north.currentLevel < forecastedNewLevel)
            {
                sides[sideCount++] = north;
                sideTotal += north.currentLevel;
            }
            if(south.canAcceptFluidDirectly(tracker) && south.currentLevel < forecastedNewLevel)
            {
                sides[sideCount++] = south;
                sideTotal += south.currentLevel;
            }
        }
        
        if(sideCount > 0)
        {
            // Sort outputs by level lowest to highest.
            // This is going to get called many times so using specialized 
            // sort methods to exploit small number of elements.
            
            if(sideCount < 3)
            {
                if(sideCount == 2)
                {
                    if(sides[0].currentLevel > sides[1].currentLevel)
                    {
                        LavaCell temp = sides[0];
                        sides[0] = sides[1];
                        sides[1] = temp;
                    }
                }
            }
            else if(sideCount == 3)
            {
                this.sort3Cells(sides);
            }
            else
            {
                this.sort4Cells(sides);
            }
            
            /** Mean of this cell with adjacent cells of level lower than my forecasted level.
             *  Will donate to cells with a level less than this.
             *  Also is the lowest that my level will go.
             */
            float targetMean = (this.currentLevel + sideTotal) / (1 + sideCount);
            float donorTotal = Math.min(this.currentLevel - targetMean, available);;
        
            int donorCount = 0;
            while( donorCount < sideCount && sides[donorCount].currentLevel < targetMean)
            {
                donorTotal+= sides[donorCount].currentLevel;
                donorCount++;
            }
            
            float donorNewMean = donorTotal / donorCount;
            
            for(int i = 0; i < donorCount; i++)
            {
                float donation = donorNewMean - sides[i].currentLevel;
                if(donation > 0)
                {
                    this.changeLevel(tracker, -donation);
                    sides[i].changeLevel(tracker, donation);
                    available -= donation;
                    amountDonated += donation;
//                    Adversity.log.info("LavaSimCell flow sideways FromID=" + this.id + " toID=" + sides[i].id + " verticalPressure=" + pressureFromAbove + " amount=" + amountDonated + " @" + this.pos.toString());
                }
            }
        }
       
        if(available <= 0) return;
        
        if(!up.isBarrier && this.currentLevel - amountDonated > pressureFromAbove)
        {
            float amount = Math.min(available, (this.currentLevel - amountDonated - pressureFromAbove));
            //prevent creating new fluid blocks above with tiny amounts of fluid
            //TODO: make threshold configurable
            if(up.currentLevel == 0 && amount < 0.001) return;
//            Adversity.log.info("equalizing up from=" + this.id + " to=" + up.id + "forecastedLevelB4Up=" + (this.currentLevel - amountDonated) +" amount=" + amount + " verticalPressure=" + pressureFromAbove);
            this.changeLevel(tracker, -amount);
            up.changeLevel(tracker, amount);
            available -= amount;
            amountDonated += amount;
        }
       
    }
    
    /**
     * Returns the pressure that this cell exerts on the cell below due to the influence of gravity.
     * Our fluid is slightly compressible to enable flowing upwards through pipe-like openings.
     */
    private float getDownwardPressure()
    {
        if(this.isBarrier || this.currentLevel <= 0)
        {
            return 1;
        }
        else if(this.currentLevel < 1)
        {
            return this.currentLevel * (PRESSURE_FACTOR - 1) + 1;
        }
        else
        {
            return this.currentLevel * PRESSURE_FACTOR;
        }
    }
    
    /**
     * Optimized sort for three element array of cells
     */
    private void sort3Cells(LavaCell[] cells)
    {
        LavaCell temp;
        
        if (cells[0].currentLevel < cells[1].currentLevel)
        {
            if (cells[1].currentLevel > cells[2].currentLevel)
            {
                if (cells[0].currentLevel < cells[2].currentLevel)
                {
                    temp = cells[1];
                    cells[1] = cells[2];
                    cells[2] = temp;
                }
                else
                {
                    temp = cells[0];
                    cells[0] = cells[2];
                    cells[2] = cells[1];
                    cells[1] = temp;
                }
            }
        }
        else
        {
            if (cells[1].currentLevel < cells[2].currentLevel)
            {
                if (cells[0].currentLevel < cells[2].currentLevel)
                {
                    temp = cells[0];
                    cells[0] = cells[1];
                    cells[1] = temp;
                }
                else
                {
                    temp = cells[0];
                    cells[0] = cells[1];
                    cells[1] = cells[2];
                    cells[2] = temp;
                }
            }
            else
            {
                temp = cells[0];
                cells[0] = cells[2];
                cells[2] = temp;
            }
        }
    }
    
    /**
     * Optimized sort for four element array of cells
     */
    private void sort4Cells(LavaCell[] cells)
    {
        LavaCell low1, high1, low2, high2, middle1, middle2, lowest, highest;
        
        if (cells[0].currentLevel < cells[1].currentLevel)
        {
            low1 = cells[0];
            high1 = cells[1];
        }
        else 
        {
            low1 = cells[1];
            high1 = cells[0];
        }
        if (cells[2].currentLevel < cells[3].currentLevel)
        {
            low2 = cells[2];
            high2 = cells[3];
        }
        else
        {
            low2 = cells[3];
            high2 = cells[2];
        }
        if (low1.currentLevel < low2.currentLevel)
        {
            lowest = low1;
            middle1 = low2;
        }
        else
        {
            lowest = low2;
            middle1 = low1;
        }
        
        if (high1.currentLevel > high2.currentLevel)
        {
            highest = high1;
            middle2 = high2;
        }
        else
        {
            highest = high2;
            middle2 = high1;
        }

        if (middle1.currentLevel < middle2.currentLevel)
        {
            cells[0] = lowest;
            cells[1] = middle1;
            cells[2] = middle2;
            cells[3] = highest;
        }
        else
        {
            cells[0] = lowest;
            cells[1] = middle2;
            cells[2] = middle1;
            cells[3] = highest;
        }
    }
    
    /**
     * True if fluid can be added directly to this cell.
     * Will return true if already has fluid 
     * or if directly above the ground or another cell that is full. 
     */
    public boolean canAcceptFluidDirectly(LavaSimulator tracker)
    {
        if (this.isBarrier) return false;
        
        if (this.currentLevel > 0) return true;
        
        LavaCell down = this.getNeighbor(tracker, EnumFacing.DOWN);
        return down.isBarrier || down.currentLevel >= 1;
    }
    
    /**
     * True if fluid particles can be created in this cell.
     * True if not a barrier and can't accept fluid directly.
     */
    public boolean canAcceptFluidParticles(LavaSimulator tracker)
    {   
        return !this.isBarrier && !this.canAcceptFluidDirectly(tracker);
    }
    
    public void changeLevel(LavaSimulator tracker, float amount)
    {
        this.changeLevel(tracker, amount, true);
    }
    
    /**
     * Set notifySimulator = false if going to call applyUpdate directly
     */
    public void changeLevel(LavaSimulator tracker, float amount, boolean notifySimulator)
    {
        
//        if(this.id == 44)
//                    Adversity.log.info("changeLevel cell=" + this.id + " amount=" + amount);
//        
        if(amount != 0)
        {
            this.delta += amount;
            if(notifySimulator) tracker.notifyCellChange(this);
        }
    }
    
    public void applyUpdates(LavaSimulator tracker)
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
                tracker.updateFluidStatus(this, this.currentLevel > 0);
            }
            
            if(wasDirty)
            {
                if(this.getVisibleLevel() == this.lastVisibleLevel) 
                {
                    tracker.dirtyCells.remove(this);
                }
                
            }
            else if(this.getVisibleLevel() != this.lastVisibleLevel)
            {
                tracker.dirtyCells.add(this);
            }
            

        }
    }
    
    public void provideBlockUpdate(LavaSimulator tracker, Collection<LavaBlockUpdate> updateList)
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
        if(this.currentLevel == 0) 
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
        if(pos.getX() == 70 && pos.getY() == 78 && pos.getZ() == 110)
            Adversity.log.info("boop");
        
        if(pos.getX() == 70 && pos.getY() == 79 && pos.getZ() == 110)
            Adversity.log.info("boop");
        
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
                    this.retainedLevel = Math.min(1, sim.terrainHelper.computeIdealBaseFlowHeight(pos));
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
   
//    public void addConnection(LavaCellConnection connection)
//    {
//        this.connections.add(connection);
//    }
//    
//    public void removeConnection(LavaCellConnection connection)
//    {
//        this.connections.remove(connection);
//    }
//    
//    public void clearConnections()
//    {
//        this.connections.clear();
//    }
//    
//    public Set<LavaCellConnection> getConnections()
//    {
//        return Collections.<LavaCellConnection>unmodifiableSet(this.connections);
//    }
    
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
    
    //TODO: remove
    private static int retainCallCount = 0;
    private static int releaseCallCount = 0;
    private static long lastUpdateNanoTime = System.nanoTime();
    private StringBuilder builder = new StringBuilder();
    
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
}