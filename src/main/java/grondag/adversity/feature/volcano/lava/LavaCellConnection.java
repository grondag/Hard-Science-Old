package grondag.adversity.feature.volcano.lava;

import java.util.concurrent.ThreadLocalRandom;

public abstract class LavaCellConnection
{
    protected static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued coordinate (x or z) */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued coordinate (x or z) */
    public final LavaCell secondCell;
    
    public final int id;
    
    public final CellConnectionPos pos;
    
    protected final AbstractCellBinder binder;
    
    public final int rand = ThreadLocalRandom.current().nextInt();
    
    protected int flowThisTick = 0;
    protected int lastFlowTick = 0;
    
//    protected int sortDrop;
    
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
 
    public static LavaCellConnection create(LavaCell firstCell, LavaCell secondCell, CellConnectionPos pos)
    {
        if(pos.isVertical())
        {
            return new LavaCellConnectionVertical(firstCell, secondCell, pos);
        }
        else
        {
            return new LavaCellConnectionHorizontal(firstCell, secondCell, pos);
        }
    }
    
    protected LavaCellConnection(LavaCell firstCell, LavaCell secondCell, CellConnectionPos pos)
    {

        this.pos = pos;
        
 
        // TODO: remove
//        if((firstCell.pos.getX() == 70 && firstCell.pos.getY() == 79 && firstCell.pos.getZ() == 110) ||(secondCell.pos.getX() == 70 && secondCell.pos.getY() == 79 && secondCell.pos.getZ() == 110))
//            Adversity.log.info("boop");
        
        this.id = nextConnectionID++;
                
        if(pos.isVertical())
        {
            if(firstCell.pos.getY() < secondCell.pos.getY())
            {
                this.firstCell = firstCell;
                this.secondCell = secondCell;
            }
            else
            {
                this.secondCell = firstCell;
                this.firstCell = secondCell;
            }
        }
        else if(firstCell.pos.getX() == secondCell.pos.getX())
        {
            if(firstCell.pos.getZ() < secondCell.pos.getZ())
            {
                this.firstCell = firstCell;
                this.secondCell = secondCell;
            }
            else
            {
                this.secondCell = firstCell;
                this.firstCell = secondCell;
            }
        }
        else 
        {
            if(firstCell.pos.getX() < secondCell.pos.getX())
            {
                this.firstCell = firstCell;
                this.secondCell = secondCell;
            }
            else
            {
                this.secondCell = firstCell;
                this.firstCell = secondCell;
            }
        }
        
        switch(pos.axis)
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
        
//        this.updateSortDrop();
    }
    
    /** for use by empty version */
    protected LavaCellConnection(LavaCell firstCell, LavaCell secondCell)
    {
        this.binder = null;
        this.pos = null;
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
        this.binder.unbind(this);
    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Zero if there is no difference.
     * Horizontal cells above the ground have a drop of 0.
     * Vertical cells have a drop of 1.
     * Higher drop means higher priority for flowing. 
     */
//    public abstract int getDrop();
    
    /**
     * Drop can change on validation, but is also used for sorting.
     * To maintain validity of sort index for retrieval, need to preserve drop
     * value until sort can be properly updated.
     * @return
     */
//    public int getSortDrop()
//    {
//        return this.sortDrop;
//    }
    
    /** seems to be needed for perfect consistency between HashMap and TreeSet */
    @Override
    public int hashCode()
    {
        return this.id;
    }
    
//    public void updateSortDrop()
//    {
//        this.sortDrop = this.getDrop();
//    }
   
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