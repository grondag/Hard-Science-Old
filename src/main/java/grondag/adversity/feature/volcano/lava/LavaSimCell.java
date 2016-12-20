package grondag.adversity.feature.volcano.lava;

import java.util.Collection;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LavaSimCell
{
    private float currentLevel = 0; // 1.0 is one full block of fluid at surface pressure
    
    private static int nextCellID = 0;
    
    //alias for readability
    private static final LavaSimCell BARRIER_CELL = BarrierCell.INSTANCE;
    
    /** 
     * If this is at or near surface, level will not drop below this - to emulate surface tension/viscosity.
     * Can be > 1 if block above should also retain fluid,
     */
    private float retainedLevel;
    
    private float delta = 0;
    
    /** for tracking block updates */
    private float lastVisibleLevel = 0;
//    
//    /** if this had a solid flow-block floor, the 
//    private float floorLevel;
    
//    private float pressure; // == currentLevel / actualVolume
    
    public final BlockPos pos;
    
    private final int id;
    
    private LavaSimCell[] neighbors = new LavaSimCell[EnumFacing.VALUES.length];
    
    private final static float PRESSURE_FACTOR = 1.05F;
//    private final static float PRESSURE_FACTOR_INVERSE = 1/1.05F;
    
    
    private boolean isDeleted;
    
    @Override 
    public int hashCode()
    {
        return this.id;
    }
    
    public LavaSimCell(LavaSimulator tracker, BlockPos pos)
    {
        this.pos = pos;
                
        this.id = nextCellID++;
        
        if(tracker == null || pos == null) return;
        
        if(!tracker.terrainHelper.isLavaSpace(tracker.world.getBlockState(pos.down())))
        {
            this.retainedLevel = tracker.terrainHelper.computeIdealBaseFlowHeight(pos);
        }
        else
        {
            LavaSimCell below = getNeighbor(tracker, EnumFacing.DOWN);
            if(below != BARRIER_CELL && below.retainedLevel > 1)
            {
                this.retainedLevel = below.retainedLevel - 1F;
            }
            else
            {
                this.retainedLevel = 0;
            }
        }
    }
    
    /**
     * True if rests on a solid surface.  
     * Particles that collide with this cell should add to it.
     */
    public boolean isCellOnGround(LavaSimulator tracker)
    {
        return this.retainedLevel > 0 && !tracker.terrainHelper.isLavaSpace(tracker.world.getBlockState(pos.down()));
    }
    
    private LavaSimCell getNeighbor(LavaSimulator tracker, EnumFacing face)
    {
        LavaSimCell result = this.neighbors[face.ordinal()];
        if(result == null || result.isDeleted)
        {
            result = tracker.getCell(pos.add(face.getDirectionVec()));
            this.neighbors[face.ordinal()] = result;
        }
        return result;
    }
    
    public void doStep(LavaSimulator tracker, double seconds)
    {
        float available = this.currentLevel - this.retainedLevel;
        
        if(available <= 0) return;
        
        //fall down if possible
        LavaSimCell down = this.getNeighbor(tracker, EnumFacing.DOWN);
        if(down.canAcceptFluidParticles(tracker))
        {
            new EntityLavaParticle(this.currentLevel, new Vec3d(this.pos.getX() + 0.5, this.pos.getY() - 0.1, this.pos.getZ() + 0.5), Vec3d.ZERO);
            this.changeLevel(tracker, -available);
            return;
        }
        
        LavaSimCell east = this.getNeighbor(tracker, EnumFacing.EAST);
        LavaSimCell west = this.getNeighbor(tracker, EnumFacing.WEST);
        LavaSimCell north = this.getNeighbor(tracker, EnumFacing.NORTH);
        LavaSimCell south = this.getNeighbor(tracker, EnumFacing.SOUTH);

        LavaSimCell[] outputs = new LavaSimCell[6];
        int outputCount = 0;
        
        //fall to sides if possible
        if(east.canAcceptFluidParticles(tracker)) outputs[outputCount++] = east;
        if(west.canAcceptFluidParticles(tracker)) outputs[outputCount++] = west;
        if(north.canAcceptFluidParticles(tracker)) outputs[outputCount++] = north;
        if(south.canAcceptFluidParticles(tracker)) outputs[outputCount++] = south;
  
        if(outputCount > 0)
        {
            for(int i = 0; i < outputCount; i++)
            {
                new EntityLavaParticle(available / outputCount, new Vec3d(outputs[i].pos.getX() + 0.5, outputs[i].pos.getY() + 0.1, outputs[i].pos.getZ() + 0.5), Vec3d.ZERO);
            }
            
            this.changeLevel(tracker, -available);
            return;
        }
        
        //get pressure from cell above
        LavaSimCell up = this.getNeighbor(tracker, EnumFacing.UP);
        float verticalPressure = up == BARRIER_CELL 
                ? 1
                : Math.max(1, up.currentLevel);
                            
        //output down if vertical pressure is sufficient
        float remainingLevel = this.currentLevel;
        
        if(down.canAcceptFluidDirectly(tracker) && down.currentLevel < verticalPressure * PRESSURE_FACTOR)
        {
            float amount = Math.min(available, verticalPressure * PRESSURE_FACTOR - down.currentLevel);
            available -= amount;
            remainingLevel -= amount;
            this.changeLevel(tracker, -amount);
            down.changeLevel(tracker, amount);
        }
        
        if(available <= 0) return;
        
        //equalize with sides that have a lower level
        float outputLevels = 0;
        
        if(east.canAcceptFluidParticles(tracker) && east.currentLevel < remainingLevel)
        {

            outputLevels += east.currentLevel;
            outputs[outputCount++] = east;
        }
        
        if(west.canAcceptFluidParticles(tracker) && west.currentLevel < remainingLevel)
        {

            outputLevels += west.currentLevel;
            outputs[outputCount++] = west;
        }
        
        if(north.canAcceptFluidParticles(tracker) && north.currentLevel < remainingLevel)
        {

            outputLevels += north.currentLevel;
            outputs[outputCount++] = north;
        }
        
        if(south.canAcceptFluidParticles(tracker) && south.currentLevel < remainingLevel)
        {

            outputLevels += south.currentLevel;
            outputs[outputCount++] = south;
        }
        
        //if will have average level > vertical pressure, equalize pressure up
        float averageLevel =  (outputLevels + remainingLevel) / (outputCount + 1);
        
        if(up != BARRIER_CELL && averageLevel > verticalPressure)
        {
            float amount = Math.min(available, (averageLevel - verticalPressure) * (outputCount + 1));
            available -= amount;
            remainingLevel -= amount;
            this.changeLevel(tracker, -amount);
            down.changeLevel(tracker, amount);
            
            averageLevel = verticalPressure;
        }
        
        if(available <= 0) return;

        if(outputCount > 1)
        {
            for(int i = 0; i < outputCount; i++)
            {
                float amount = Math.min(available, (averageLevel - outputs[i].currentLevel));
                available -= amount;
                remainingLevel -= amount;
                this.changeLevel(tracker, -amount);
                outputs[i].changeLevel(tracker, amount);
            }
        }
        
    }
    
    /**
     * True if fluid can be added directly to this cell.
     * Will return true if on the ground and has capacity, or if has fluid and has capacity,
     * or if sits on top of a surface cell that is full.
     */
    public boolean canAcceptFluidDirectly(LavaSimulator tracker)
    {
        if (this == BARRIER_CELL) return false;
        
        return this.currentLevel > 0 
                || this.isCellOnGround(tracker)
                || this.getNeighbor(tracker, EnumFacing.DOWN).currentLevel >= 1;
    }
    
    /**
     * True if fluid particles can be created in this cell.
     * Will return true if above the ground or fluid surface and is not a barrier,
     */
    public boolean canAcceptFluidParticles(LavaSimulator tracker)
    {
        if (this == BARRIER_CELL) return false;
        
        return this.currentLevel == 0 
                && !this.isCellOnGround(tracker)
                && this.getNeighbor(tracker, EnumFacing.DOWN).currentLevel < 1;
    }
    
    public void changeLevel(LavaSimulator tracker, float amount)
    {
        if(amount != 0)
        {
            this.delta += amount;
            tracker.notifyCellChange(this);
        }
    }
    
    public void applyUpdates(LavaSimulator tracker)
    {
        if(this.delta != 0)
        {
            if(this.currentLevel == 0)
            {
                tracker.cellsWithFluid.add(this);
            }
            
            if(this.currentLevel == this.lastVisibleLevel)
            {
                tracker.dirtyCells.add(this);
            }
            
            this.currentLevel += delta;
            
            if(this.currentLevel == 0)
            {
                tracker.cellsWithFluid.add(this);
            }
            
            if(this.currentLevel == this.lastVisibleLevel)
            {
                tracker.dirtyCells.remove(this);
            }
        }
    }
    
    public void provideBlockUpdate(LavaSimulator tracker, Collection<LavaBlockUpdate> updateList)
    {
        if(this.lastVisibleLevel != this.currentLevel)
        {
            updateList.add(new LavaBlockUpdate(pos, currentLevel));
            this.lastVisibleLevel = this.currentLevel;
        }
    }
    
    public float getCurrentLevel()
    {
        return this.currentLevel;
    }
 
    public float getDelta()
    {
        return this.delta;
    }
    
    public void delete(LavaSimulator tracker)
    {
        tracker.cellsWithFluid.remove(this);
        tracker.allCells.remove(this);
        this.isDeleted = true;
    }
    
}
