package grondag.adversity.feature.volcano.lava;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.Useful;
import net.minecraft.util.EnumFacing;

public abstract class LavaCellConnection
{
    protected static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued coordinate (x or z) */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued coordinate (x or z) */
    public final LavaCell secondCell;
    
    public final int id;
    
    public final long packedConnectionPos;
    
    protected final AbstractCellBinder binder;
    
    public final int rand = ThreadLocalRandom.current().nextInt();
    
    protected int flowThisTick = 0;
    protected int lastFlowTick = 0;
    
    protected long sortKey;
    
    protected boolean isDirty = false;
 
    public final static int PRESSURE_PER_LEVEL = LavaCell.FLUID_UNITS_PER_BLOCK / 20;
    
    /** smallest flow into a block that already contains fluid */
    public final static int MINIMUM_INTERNAL_FLOW_UNITS = PRESSURE_PER_LEVEL / 10;
    public final static int MINIMUM_INTERNAL_FLOW_UNITS_X2 = MINIMUM_INTERNAL_FLOW_UNITS * 2;
    
    /** smallest flow into a block that has no fluid already - applies to horizontal flow only */
    public final static int MINIMUM_EXTERNAL_FLOW_UNITS = PRESSURE_PER_LEVEL;
    public final static int MINIMUM_EXTERNAL_FLOW_UNITS_X2 = MINIMUM_EXTERNAL_FLOW_UNITS * 2;
    
    public final static int UNITS_PER_ONE_BLOCK_WITH_PRESSURE = LavaCell.FLUID_UNITS_PER_BLOCK + PRESSURE_PER_LEVEL;
    public final static int UNITS_PER_TWO_BLOCKS = LavaCell.FLUID_UNITS_PER_BLOCK * 2 + PRESSURE_PER_LEVEL;
    public final static float INVERSE_PRESSURE_FACTOR = (float)LavaCell.FLUID_UNITS_PER_BLOCK/UNITS_PER_ONE_BLOCK_WITH_PRESSURE;
    
    //TODO: make configurable?
    /** Maximum flow through any block connection in a single tick. 
     * Not changing this to vary with pressure because most lava flows are 
     * along the surface and higher-velocity flows (down a slope) will also
     * have a smaller cross-section due to retained height calculations.
     */
    public final static int MAX_HORIZONTAL_FLOW_PER_TICK = LavaCell.FLUID_UNITS_PER_BLOCK / 10;
    public final static int MAX_UPWARD_FLOW_PER_TICK = MAX_HORIZONTAL_FLOW_PER_TICK / 2;
    public final static int MAX_DOWNWARD_FLOW_PER_TICK = MAX_HORIZONTAL_FLOW_PER_TICK * 2;
    
    /** classifies the bottom cell of vertical connections */
    public static enum BottomType
    {
        DROP,
        SUPPORTING,
        PARTIAL
    }
 
    public static LavaCellConnection create(LavaCell firstCell, LavaCell secondCell, long packedConnectionPos)
    {
        if(PackedBlockPos.getExtra(packedConnectionPos) == EnumFacing.Axis.Y.ordinal())
        {
            return new LavaCellConnectionVertical(firstCell, secondCell, packedConnectionPos);
        }
        else
        {
            return new LavaCellConnectionHorizontal(firstCell, secondCell, packedConnectionPos);
        }
    }
    
    protected LavaCellConnection(LavaCell firstCell, LavaCell secondCell, long packedConnectionPos)
    {

        this.packedConnectionPos = packedConnectionPos;
        
 
        // TODO: remove
//        if((firstCell.pos.getX() == 70 && firstCell.pos.getY() == 79 && firstCell.pos.getZ() == 110) ||(secondCell.pos.getX() == 70 && secondCell.pos.getY() == 79 && secondCell.pos.getZ() == 110))
//            Adversity.log.info("boop");
        
        this.id = nextConnectionID++;
        this.firstCell = firstCell;
        this.secondCell = secondCell;
        firstCell.retain("");
        secondCell.retain("");
                
        switch(EnumFacing.Axis.values()[PackedBlockPos.getExtra(packedConnectionPos)])
        {
            case X:
                this.binder = CellBinderX.INSTANCE;
                break;
                
            case Y:
                this.binder = CellBinderY.INSTANCE;
                break;
                
            case Z:
            default:
                this.binder = CellBinderZ.INSTANCE;
                break;
        }
        binder.bind(this);
        
        this.updateSortKey();
    }
    
    /** for use by empty version */
    protected LavaCellConnection(LavaCell firstCell, LavaCell secondCell)
    {
        this.binder = null;
        this.packedConnectionPos = 0;
        this.firstCell = firstCell;
        this.secondCell = secondCell;
        this.id = nextConnectionID++;
    }
    
    public LavaCell getOther(LavaCell cellIAlreadyHave)
    {
        if(cellIAlreadyHave == this.firstCell)
        {
            return this.secondCell;
        }
        else
        {
            return this.firstCell;
        }
    }
    
    /**
     * For horizontal cells, returns the distanceToFlowFloor of other cell.
     * For nowhere and vertical connections, always returns zero.
     */
    public int getOtherDistanceToFlowFloor(LavaCell cellIAlreadyHave)
    {
        return 0;
    }
    
    abstract protected int getFlowRate(LavaSimulator sim);
  
    /**
     *  Resets lastFlowTick and forces run at least once a tick.
     */
    public void doFirstStep(LavaSimulator sim)
    {
            this.isDirty = false;
            this.flowThisTick = 0;
            this.lastFlowTick = sim.getTickIndex();
            this.doStepWork(sim);
    }
    
    public void doStep(LavaSimulator sim)
    {
        if(this.isDirty)
        {
            this.isDirty = false;
            this.doStepWork(sim);
        }
    }
    
    /**
     * Guts of doStep.
     */
    private void doStepWork(LavaSimulator sim)
    {
        // barriers don't need processing - generally shouldn't happen but safeguard in case it does
        if(this.firstCell.isBarrier() || this.secondCell.isBarrier()) return;
        
        int flow = this.getFlowRate(sim);
        
        if(flow != 0) this.flowAcross(sim, flow);
    }
    
    public void setDirty()
    {
        this.isDirty = true;
    }
    
    public void flowAcross(LavaSimulator sim, int flow)
    {
        this.flowThisTick += flow;
        this.firstCell.changeLevel(sim, -flow);
        this.secondCell.changeLevel(sim, flow);
    }
    
    /**
     * Call when removing this connection so that cell references can be removed if appropriate.
     */
    public void releaseCells()
    {
        this.firstCell.release("");
        this.secondCell.release("");
        this.binder.unbind(this);
    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Measured in fluid units
     * For horizontal connections:
     *      Zero if there is no difference or if either block is a barrier.
     *      Higher drop means higher priority for flowing. 
     * For vertical connections:
     *      Drop is a full block by convention and not intended to be used.
     * Nowhere (barrier) connections always have a drop of 0.         
     */
    public abstract int getSortDrop();
    
    /**
     * Drop can change on validation, but is also used for sorting.
     * To maintain validity of sort index for retrieval, need to preserve drop
     * value until sort can be properly updated.
     * @return
     */
    public long getSortKey()
    {
        return this.sortKey;
    }
    
    /** seems to be needed for perfect consistency between HashMap and TreeSet */
    @Override
    public int hashCode()
    {
        return this.id;
    }
    
    public void updateSortKey()
    {
        // axis - Y or not Y - Y first (lower)  1 bit
        long key = PackedBlockPos.getExtra(this.packedConnectionPos) == EnumFacing.Axis.Y.ordinal()
            ? 0 : 0x1L << 62;
        
        // elevation - higher first  (lower)    8 bits
        key |= ((255L - PackedBlockPos.getY(this.packedConnectionPos)) << 54);
        
        // drop - higher drops come first       16 bits
        key |= ((long)((0xFFFF - this.getSortDrop()) & 0xFFFF) << 38);
        
        // random                               6 bits
        key |= ((Useful.longHash(this.packedConnectionPos) & 0x3F) << 32);
        
        // uniqueness  32 bits
        key |= this.id;
        
        this.sortKey = key;
    }
   
    private static abstract class AbstractCellBinder
    {
        public abstract void bind(LavaCellConnection con);
        public abstract void unbind(LavaCellConnection con);
    }
    
    private static class CellBinderX extends AbstractCellBinder
    {

        private static final CellBinderX INSTANCE = new CellBinderX();
        
        @Override
        public void bind(LavaCellConnection con)
        {
            con.firstCell.bindEast(con);
            con.secondCell.bindWest(con);
        }

        @Override
        public void unbind(LavaCellConnection con)
        {
            con.firstCell.unbindEast();
            con.secondCell.unbindWest();            
        }
    }
    
    private static class CellBinderY extends AbstractCellBinder
    {
        private static final CellBinderY INSTANCE = new CellBinderY();

        @Override
        public void bind(LavaCellConnection con)
        {
            con.firstCell.bindUp(con);
            con.secondCell.bindDown(con);
        }

        @Override
        public void unbind(LavaCellConnection con)
        {
            con.firstCell.unbindUp();
            con.secondCell.unbindDown();            
        }

//        public final static int BOTTOM_FLAG_EMPTY = 0;
//        public final static int BOTTOM_FLAG_PARTIAL = 1;
//        public final static int BOTTOM_FLAG_SUPPORTING = 2;
        
    }
    
    private static class CellBinderZ extends AbstractCellBinder
    {
        private static final CellBinderZ INSTANCE = new CellBinderZ();
        
        @Override
        public void bind(LavaCellConnection con)
        {
            con.firstCell.bindSouth(con);
            con.secondCell.bindNorth(con);
        }

        @Override
        public void unbind(LavaCellConnection con)
        {
            con.firstCell.unbindSouth();
            con.secondCell.unbindNorth();            
        }
    }
}