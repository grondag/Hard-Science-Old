package grondag.adversity.feature.volcano.lava;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidTracker
{
    public static FluidTracker egregiousHack;
    
    protected final World world;
    protected final TerrainHelper terrainHelper;
//    protected final BlockPos origin;
//    public final int xOffset;
//    public final int zOffset;
    
//    private final FluidCell [][][] cells;
//    
//    private final static int RADIUS = 256;
//    private final static int ARRAY_LENGTH = RADIUS * 2 + 1;
    
    protected final HashMap<BlockPos, FluidCell> allCells = new HashMap<BlockPos, FluidCell>();
    private final HashSet<FluidCell> updatedCells = new HashSet<FluidCell>();
    protected final HashSet<FluidCell> cellsWithFluid = new HashSet<FluidCell>();
    
    /** cells that may need a block update */
    protected final HashSet<FluidCell> dirtyCells = new HashSet<FluidCell>();
    
    protected final HashSet<FluidParticle> allParticles = new HashSet<FluidParticle>();
    
    private final LinkedList<LavaBlockUpdate> blockUpdates = new LinkedList<LavaBlockUpdate>();

    /**
     * Particles that have moved to a new block space and thus require check for collision.
     */
    protected final HashSet<FluidParticle> movedParticles = new HashSet<FluidParticle>();

    
    public FluidTracker(World world)
    {
        this.world = world;
        this.terrainHelper = new TerrainHelper(world);
//        this.origin = origin;
//        this.xOffset = RADIUS - origin.getX();
//        this.zOffset = RADIUS - origin.getZ();
//        this.cells = new FluidCell[ARRAY_LENGTH][256][ARRAY_LENGTH];
    }
    
    public void doStep(double seconds)
    {
        //update particles
//        for(FluidParticle particle : this.allParticles)
//        {
//            particle.doStep(seconds);
//        }
//        
//        //Check particles for collision.
//        for(FluidParticle particle : this.movedParticles)
//        {
//            //Add lava from collided particles and remove them from sim.
//            
//            //TODO: handle horizontal collisions - won't happen now because all are pure vertical drops
//        }
        
        for(FluidCell cell : cellsWithFluid)
        {
            cell.doStep(this, seconds);
        }
        
        for(FluidCell cell : this.updatedCells)
        {
            cell.applyUpdates(this);
        }
        
        this.updatedCells.clear();
    }
    
    protected FluidCell getCell(BlockPos pos)
    {
//        int arrayX = pos.getX() + xOffset;
//        int arrayZ = pos.getZ() + zOffset;
//        
//        //Will get called by boundary lookups - 
//        //just ignore these, effective creates a barrier at boundary
//        if(arrayX >= ARRAY_LENGTH || arrayX < 0 || arrayZ >= ARRAY_LENGTH || arrayZ < 0)
//        {
//            return null;
//        }
        
        FluidCell result = allCells.get(pos);
        
        if(result == null)
        {
            IBlockState state = world.getBlockState(pos);
            if(terrainHelper.isLavaSpace(state))
            {
                result = new FluidCell(this, pos);
                allCells.put(pos, result);
            }
            else
            {
                result = BarrierCell.INSTANCE;
            }
        }
        return result;
    }
    
   
    
    protected void addLava(BlockPos pos, float amount)
    {
        //TODO
    }
    
    
    /**
     * Cells should call this when their level changes.
     */
    protected void notifyCellChange(FluidCell cell)
    {
        if(cell.getDelta() == 0)
        {
            //was changed, and now no longer needs to be changed
            this.updatedCells.remove(cell);
        }
        else
        {
            this.updatedCells.add(cell);
        }
    }
    
    public Queue<LavaBlockUpdate> getBlockUpdates()
    {
        for(FluidCell cell : this.dirtyCells)
        {
            cell.provideBlockUpdate(this, this.blockUpdates);
        }
        dirtyCells.clear();
        return this.blockUpdates;
    }

}
