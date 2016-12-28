package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import grondag.adversity.Adversity;
import grondag.adversity.config.Config;
import grondag.adversity.feature.volcano.BlockManager.BlockPlacement;
import grondag.adversity.library.Useful;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.PersistenceManager;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LavaSimulator extends SimulationNode
{
    protected final World world;
    protected final LavaTerrainHelper terrainHelper;

    
    protected final HashMap<BlockPos, LavaCell> allCells = new HashMap<BlockPos, LavaCell>();
    private final HashSet<LavaCell> updatedCells = new HashSet<LavaCell>();
    private final HashMap<BlockPos, LavaCell> cellsWithFluid = new HashMap<BlockPos, LavaCell>();
    
    private final TreeMap<CellConnectionPos, LavaCellConnection> connections = new TreeMap<CellConnectionPos, LavaCellConnection>();
    
    private final LinkedList<CellConnectionPos> newConnections = new LinkedList<CellConnectionPos>();
    
    //TODO: make config
    private static final int VALIDATION_TICKS = 20;
    private int ticksUntilNextValidation = VALIDATION_TICKS;
    
    //TODO: make config
    private static final int CACHE_TICKS = 60;
    private int ticksUntilCacheCleanup = CACHE_TICKS;
    
    /**
     * Blocks that need to be melted or checked for filler after placement.
     * Not saved to NBT because should be fully processed and cleared every tick.
     */
    private HashSet<BlockPos> adjustmentList = new HashSet<BlockPos>();
    
    /** cells that may need a block update */
    protected final HashSet<LavaCell> dirtyCells = new HashSet<LavaCell>();
    
    private final LinkedList<LavaBlockUpdate> blockUpdates = new LinkedList<LavaBlockUpdate>();
    
    protected final static String TAG_SAVE_DATA = "lavacells";

    public LavaSimulator(World world)
    {
        super(NodeRoots.LAVA_SIMULATOR.ordinal());
        this.world = world;
        this.terrainHelper = new LavaTerrainHelper(world);
    }
    
    public void doStep(double seconds)
    {

        if(--ticksUntilNextValidation == 0)
        {
//            Adversity.log.info("LavaSim doStep validatingAllCells, cell count=" + this.allCells.size() );
            validateAllCells();
            ticksUntilNextValidation = VALIDATION_TICKS;
        }
        
//        if(cellsWithFluid.size() > 0)
//        {
//            Adversity.log.info("LavaSim doStep, cell count=" + cellsWithFluid.size() );
//        }
//      
        
        this.processNewConnectionRequests();
        
        if(this.connections.size() > 0)
        {
//            Adversity.log.info("LavaSim connection processing, count=" + connections.size() );

            //use iterator and hold changes in other collections
            Iterator<Entry<CellConnectionPos, LavaCellConnection>> it = this.connections.entrySet().iterator();
            while(it.hasNext())
            {
                Entry<CellConnectionPos, LavaCellConnection> next = it.next();
                LavaCellConnection connection = next.getValue();
                //remove connections to barriers or between cells with no fluid
                if(connection.firstCell.isBarrier() || connection.secondCell.isBarrier()
                        || (connection.firstCell.getCurrentLevel() == 0 && connection.secondCell.getCurrentLevel() == 0))
                {
                    connection.releaseCells();
                    it.remove();
                }
                else
                {
                    connection.doStep(this, seconds);
                }
            }

        }
        
//        if(updatedCells.size() > 0)
//        {
//            Adversity.log.info("LavaSim updatedCells, cell count=" + updatedCells.size() );
//            
//            for(LavaCell cell : this.updatedCells)
//            {
//                cell.applyUpdates(this);
//            }
//            
//            this.updatedCells.clear();
//            
//            this.setSaveDirty(true);
//        }
        
        //TODO: handle particles with connection-oriented model
        
        if(--ticksUntilCacheCleanup == 0)
        {
//            Adversity.log.info("LavaSim doStep cleanCellCache, cell starting count=" + this.allCells.size() );
            cleanCellCache();
            ticksUntilCacheCleanup = CACHE_TICKS;
            Adversity.log.info("LavaSim doStep cleanCellCache, cell ending count=" + this.allCells.size() );
            
//            this.allCells.values().forEach(cell -> {Adversity.log.info("hash=" + cell.hashCode() + "pos=" + cell.pos.toString() + " level=" + cell.getCurrentLevel());});
//            this.connections.values().forEach(connection -> {Adversity.log.info("firstHash=" + connection.firstCell.hashCode() + " firstPos=" + connection.firstCell.pos.toString() + " firstLevel=" + connection.firstCell.getCurrentLevel() + "secondHash=" + connection.secondCell.hashCode() + " secondPos=" + connection.secondCell.pos.toString() + " secondLevel=" + connection.secondCell.getCurrentLevel());});

            float totalFluid = 0;
            for(LavaCell cell : cellsWithFluid.values())
            {
                totalFluid += cell.getCurrentLevel();
            }
            Adversity.log.info("Total fluid = " + totalFluid);
        }
        
    }
    
    private void validateAllCells()
    {
        //TODO: make parallel
        for(LavaCell cell : this.allCells.values().toArray(new LavaCell[0]))
        {
            cell.validate(this, false);
        }
    }
    
    private void cleanCellCache()
    {
       
//        this.allCells.values().parallelStream().forEach((LavaCell c) -> {if(c.getCurrentLevel() > 0) c.clearNeighborCache();});


        Iterator<Entry<BlockPos, LavaCell>> it = this.allCells.entrySet().iterator();
        
        while(it.hasNext())
        {
            Entry<BlockPos, LavaCell> next = it.next();
            if(!next.getValue().isRetained())
            {
                it.remove();
            }
        }

    }
    
    protected LavaCell getCell(BlockPos pos)
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
        
        LavaCell result = allCells.get(pos);
        
        if(result == null)
        {
            result = new LavaCell(this, pos);
            allCells.put(pos, result);
            result.validate(this, true);
        }
        return result;
    }
    
   
    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        LavaCell target = this.getCell(pos);
        int worldLevel = IFlowBlock.getFlowHeightFromState(state);
        if(target.getVisibleLevel() != worldLevel)
        {
            target.changeLevel(this, (worldLevel - target.getVisibleLevel()) / FlowHeightState.BLOCK_LEVELS_FLOAT);
            target.applyUpdates(this);
            target.clearBlockUpdate();
            this.setSaveDirty(true);
        }
    }
    
    /**
     * Update simulation from world when blocks are removed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        LavaCell target = this.getCell(pos);
        if(target.getCurrentLevel() > 0)
        {
            target.changeLevel(this, - target.getCurrentLevel(), false);
            target.applyUpdates(this);
            target.clearBlockUpdate();
            this.setSaveDirty(true);
        }
    }
    
    /**
     * Call when a cell transitions between having and not having any fluid.
     * Maintains the list of connections with cells that have fluid.
     * Also ensures we retain all cells neighboring fluid-containing cells
     * so that they can be validated against the game world to check for breaks.
     */
    protected void updateFluidStatus(LavaCell cell, boolean hasFluid)
    {
        if(hasFluid)
        {
//            Adversity.log.info("fluid status retain");
            if(!cellsWithFluid.containsKey(cell.pos))
            {
                cell.retain();
                this.cellsWithFluid.put(cell.pos, cell);
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    LavaCell other = cell.getNeighbor(this, face);
                    other.retain();
                    if(!other.isBarrier())
                    {
                        this.requestNewConnection(cell.pos, other.pos);
                    }
                }
            }
            else
            {
                Adversity.log.info("updateFluidStatus called with true for cellid=" + cell.hashCode() + " already in cellsWithFluid collection");
            }
        }
        else
        {
//            Adversity.log.info("fluid status release");
            if(cellsWithFluid.containsKey(cell.pos))
            {
                cellsWithFluid.remove(cell.pos);
                cell.release();
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    LavaCell other = cell.getNeighbor(this, face);
                    other.release();
                }
            }
            else
            {
                Adversity.log.info("updateFluidStatus called with false for cellid=" + cell.hashCode() + " not already in cellsWithFluid collection");
            }
        }
    }
    
    /**
     * Adds lava in or on top of the given cell.
     * TODO: handle when not all the lava can be used.
     */
    public void addLava(BlockPos pos, float amount)
    {
        Adversity.log.info("addLava amount=" + amount + " @" + pos.toString());
        
        float available = amount;
        
        LavaCell target = this.getCell(pos.down());
        if(target.canAcceptFluidDirectly(this) && target.getCurrentLevel() < 1)
        {
            float capacity = Math.min(amount, 1 - target.getCurrentLevel());
            target.changeLevel(this, capacity);
            available -= capacity;
        }
        
        if(available > 0)
        {
            target = this.getCell(pos);
            if(target.canAcceptFluidDirectly(this) && target.getCurrentLevel() < 1)
            {
                float capacity = Math.min(amount, 1 - target.getCurrentLevel());
                target.changeLevel(this, capacity);
                available -= capacity;
            }
            
            if(available > 0)
            {
                target = this.getCell(pos.up());
                if(target.canAcceptFluidDirectly(this) && target.getCurrentLevel() < 1)
                {
                    float capacity = Math.min(amount, 1 - target.getCurrentLevel());
                    target.changeLevel(this, capacity);
                    available -= capacity;
                }
            }
        }
    }
    
    
    /**
     * Cells should call this when their level changes.
     */
    protected void notifyCellChange(LavaCell cell)
    {
        //TODO: prevent duplicate notification when there are multiple changes in a step?
        
        Adversity.log.info("notifyCellChange cell=" + cell.hashCode());
        if(cell.getDelta() == 0)
        {
            //was changed, and now no longer needs to be changed
            this.updatedCells.remove(cell);
            Adversity.log.info("notifyCellChange cell removed");
        }
        else
        {
            Adversity.log.info("notifyCellChange cell added");
            this.updatedCells.add(cell);
        }
    }
    
    public void requestNewConnection(BlockPos pos1, BlockPos pos2)
    {
        this.newConnections.add(new CellConnectionPos(pos1, pos2));
    }
    
    private void processNewConnectionRequests()
    {
        while(!newConnections.isEmpty())
        {
            CellConnectionPos pos = newConnections.removeFirst();
            
            if(!this.connections.containsKey(pos))
            {
                LavaCell cell1 = this.getCell(pos.getLowerPos());
                LavaCell cell2 = this.getCell(pos.getUpperPos());
                this.connections.put(pos, new LavaCellConnection(cell1, cell2));
            }
        }
    }
    
    private Queue<LavaBlockUpdate> getBlockUpdates()
    {
        for(LavaCell cell : this.dirtyCells)
        {
            cell.provideBlockUpdate(this, this.blockUpdates);
        }
        dirtyCells.clear();
        return this.blockUpdates;
    }

    public void doBlockUpdates()
    {
//        Adversity.log.info("LavaSim doBlockUpdates");

        Queue<LavaBlockUpdate> blockUpdates = getBlockUpdates();
            
            LavaBlockUpdate update = blockUpdates.poll();       
            while(update != null)
            {   if(update.level == 0 && world.getBlockState(update.pos).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    world.setBlockToAir(update.pos);
                }
                else
                {
                    this.world.setBlockState(update.pos, IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), update.level));
                }
                this.adjustmentList.add(update.pos);
                update = blockUpdates.poll();
            }
            
            
            //doBlockUpdates();
            
            doAdjustments();
    }
    
    private void doAdjustments()
    {
        HashSet<BlockPos> targets = new HashSet<BlockPos>();
    
        for(BlockPos changed : adjustmentList)
        {
            targets.add(changed.east());
            targets.add(changed.west());
            targets.add(changed.north());
            targets.add(changed.south());
            targets.add(changed.north().east());
            targets.add(changed.south().east());
            targets.add(changed.north().west());
            targets.add(changed.south().west());
        }

        adjustmentList.clear();
        
        HashSet<BlockPos> updated = new HashSet<BlockPos>();
        
        for(BlockPos target : targets)
        {
            for(int y = -4; y <= 4; y++)
            {
                BlockPos p = target.add(0, y, 0);
                if(!adjustHeightBlockIfNeeded(p));
                {
                    IFlowBlock.adjustFillIfNeeded(this.world, p, updated);
                }
            }
        }
        
//        for(BlockPos uPos : updated)
//        {
//          //TODO: handle cooling
//            coolingBlocks.add(uPos, ticksActive + Config.volcano().coolingLagTicks);
//        }
    }
    
    /**
     * Recursively elevates lava blocks on line between fromPos and origin, including origin,
     * starting at posting startPos, based on distance to origin. From block is assumed to be one high.
     * EachOrigin should be distance high, up to the max. 
     * If lava block is already as high as distance would indicate, does not affect it.
     */
    private void raiseLavaToOrigin(BlockPos fromPos, BlockPos startPos, BlockPos origin)
    {
        //methods below wants 3d not 3i, and also need to go from middle of blocks
        Vec3d from3d = new Vec3d(0.5 + fromPos.getX(), 0.5 + fromPos.getY(), 0.5 + fromPos.getZ());
        Vec3d to3d = new Vec3d(0.5 + origin.getX(), 0.5 + origin.getY(), 0.5 + origin.getZ());
        
        int distanceSquaredToOrigin = Useful.squared(origin.getX() - startPos.getX()) 
                + Useful.squared(origin.getZ() - startPos.getZ());
        
        Vec3d direction = to3d.subtract(from3d);
        for(int i = 0; i < HorizontalFace.values().length; i++)
        {
            BlockPos testPos = startPos.add(HorizontalFace.values()[i].directionVector);
            
            //block has to be closer to origin than the starting position
            if(distanceSquaredToOrigin > Useful.squared(origin.getX() - testPos.getX()) 
                    + Useful.squared(origin.getZ() - testPos.getZ()))
            {
                //Use AABB slightly larger than block to handle case of 45deg angle
                //Otherwise would need special handling for diagonals and adjacent in that case.
                AxisAlignedBB box = new AxisAlignedBB(-0.1 + testPos.getX(), testPos.getY(), -0.1 + testPos.getZ(),
                1.1 + testPos.getX(), 1 + testPos.getY(), 1.1 + testPos.getZ());
                if(Useful.doesRayIntersectAABB(from3d, direction, box))
                {
                    IBlockState state = this.world.getBlockState(testPos);
                    if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                    {
                        int distance = (int) Math.round(Math.sqrt(fromPos.distanceSq(testPos)));
                        int newHeight = Math.max(IFlowBlock.getFlowHeightFromState(state), Math.min(distance + 1,  FlowHeightState.BLOCK_LEVELS_INT));
                        this.world.setBlockState(testPos, IFlowBlock.stateWithDiscreteFlowHeight(state, newHeight));
                        adjustmentList.add(testPos);
    
                        if(!testPos.equals(origin))
                        {
                            raiseLavaToOrigin(fromPos, testPos, origin);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Melts static height blocks if geometry would be different from current.
     * And turns full cube dynamic blocks into static cube blocks.
     * Returns true if is a height block, even if no adjustement was needed.
     */
    private boolean adjustHeightBlockIfNeeded(BlockPos targetPos)
    {

        if(targetPos == null) return false;

        //        Adversity.log.info("meltExposedBasalt @" + targetPos.toString());        

        IBlockState state = this.world.getBlockState(targetPos);
        if(!(state.getBlock() instanceof NiceBlock)) return false;

        NiceBlock block = (NiceBlock)state.getBlock();

        if(!IFlowBlock.isFlowHeight(block)) return false;

        boolean isFullCube = IFlowBlock.shouldBeFullCube(state, this.world, targetPos);


        if(isFullCube)
        {
            if(block == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
            {
                //                Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
                //                        + this.world.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
                //                        + NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getRegistryName() + " @ " + targetPos.toString());
                this.world.setBlockState(targetPos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
            }
        }
        else if (block == NiceBlockRegistrar.COOL_STATIC_BASALT_HEIGHT_BLOCK 
                || block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK 
                || block == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
        {

            //            Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
            //                    + worldObj.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
            //                    + NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getRegistryName() + " @ " + targetPos.toString());


            this.world.setBlockState(targetPos, NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                    .withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
            
            //TODO: handle cooling
//            this.coolingBlocks.add(targetPos, ticksActive + Config.volcano().coolingLagTicks);
        }

        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        allCells.clear();
        updatedCells.clear();
        cellsWithFluid.clear();
        connections.clear();
        newConnections.clear();
        adjustmentList.clear();
        dirtyCells.clear();
        blockUpdates.clear();
        
        int[] saveData = nbt.getIntArray(TAG_SAVE_DATA);
        
        //to be valid, must have a multiple of two
        if(saveData == null || saveData.length % 4 != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Lava blocks may not be updated properly.");
            return;
        }
        
        int i = 0;
        while(i < saveData.length)
        {
            LavaCell cell = new LavaCell(this, new BlockPos(saveData[i++], saveData[i++], saveData[i++]), Float.intBitsToFloat(saveData[i++]));
            
            // protect against corrupt saves
            if(cell.getCurrentLevel() > 0 && !this.cellsWithFluid.containsKey(cell.pos))
            {
                cell.validate(this, true);
                this.allCells.put(cell.pos, cell);
                this.updateFluidStatus(cell, true);
            }
        }
  
        Adversity.log.info("Loaded " + cellsWithFluid.size() + " lava cells.");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        Adversity.log.info("Saving " + cellsWithFluid.size() + " lava cells.");
        int[] saveData = new int[cellsWithFluid.size() * 4];
        int i = 0;
        
        for(LavaCell cell: cellsWithFluid.values())
        {
            saveData[i++] = cell.pos.getX();
            saveData[i++] = cell.pos.getY();
            saveData[i++] = cell.pos.getZ();
            saveData[i++] = Float.floatToIntBits(cell.getCurrentLevel());
        }       
        
        nbt.setIntArray(TAG_SAVE_DATA, saveData);
        
    }
}