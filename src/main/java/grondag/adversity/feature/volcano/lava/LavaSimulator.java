package grondag.adversity.feature.volcano.lava;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LavaSimulator extends SimulationNode
{
    protected final World world;
    protected final LavaTerrainHelper terrainHelper;

    
    protected final HashMap<BlockPos, LavaSimCell> allCells = new HashMap<BlockPos, LavaSimCell>();
    private final HashSet<LavaSimCell> updatedCells = new HashSet<LavaSimCell>();
    protected final HashSet<LavaSimCell> cellsWithFluid = new HashSet<LavaSimCell>();
    
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
    protected final HashSet<LavaSimCell> dirtyCells = new HashSet<LavaSimCell>();
    
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

//        if(--ticksUntilNextValidation == 0)
//        {
//            Adversity.log.info("LavaSim doStep validatingAllCells, cell count=" + this.allCells.size() );
//            validateAllCells();
//            ticksUntilNextValidation = VALIDATION_TICKS;
//        }
        
        if(cellsWithFluid.size() > 0)
        {
            Adversity.log.info("LavaSim doStep, cell count=" + cellsWithFluid.size() );
        }
        
        for(LavaSimCell cell : cellsWithFluid)
        {
            cell.doStep(this, seconds);
        }
        
        if(updatedCells.size() > 0)
        {
            Adversity.log.info("LavaSim updatedCells, cell count=" + updatedCells.size() );
            
            for(LavaSimCell cell : this.updatedCells)
            {
                cell.applyUpdates(this);
            }
            
            this.updatedCells.clear();
            
            this.setSaveDirty(true);
        }
        
//        if(--ticksUntilCacheCleanup == 0)
//        {
//            Adversity.log.info("LavaSim doStep cleanCellCache, cell starting count=" + this.allCells.size() );
//            cleanCellCache();
//            ticksUntilCacheCleanup = CACHE_TICKS;
//            Adversity.log.info("LavaSim doStep cleanCellCache, cell ending count=" + this.allCells.size() );
//        }
        
    }
    
    private void validateAllCells()
    {
        //TODO: make parallel
        for(LavaSimCell cell : this.allCells.values())
        {
            cell.validate(this, false);
        }
    }
    
    private void cleanCellCache()
    {
       //TODO: make parallel
        Iterator<Entry<BlockPos, LavaSimCell>> it = this.allCells.entrySet().iterator();
        while(it.hasNext())
        {
            Entry<BlockPos, LavaSimCell> next = it.next();
            
            if(next.getValue().clearNeighborCache())
            {
                it.remove();
            }
        }
    }
    
    protected LavaSimCell getCell(BlockPos pos)
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
        
        LavaSimCell result = allCells.get(pos);
        
        if(result == null)
        {
            result = new LavaSimCell(this, pos);
            allCells.put(pos, result);
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
        LavaSimCell target = this.getCell(pos);
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
        LavaSimCell target = this.getCell(pos);
        if(target.getCurrentLevel() > 0)
        {
            target.changeLevel(this, - target.getCurrentLevel());
            target.applyUpdates(this);
            target.clearBlockUpdate();
            this.setSaveDirty(true);
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
        
        LavaSimCell target = this.getCell(pos.down());
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
    protected void notifyCellChange(LavaSimCell cell)
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
    
    private Queue<LavaBlockUpdate> getBlockUpdates()
    {
        for(LavaSimCell cell : this.dirtyCells)
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
            LavaSimCell cell = new LavaSimCell(this, new BlockPos(saveData[i++], saveData[i++], saveData[i++]), Float.intBitsToFloat(saveData[i++]));
            cell.clearBlockUpdate();;
            this.allCells.put(cell.pos, cell);
            this.cellsWithFluid.add(cell);
        }
  
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
       
        int[] saveData = new int[cellsWithFluid.size() * 4];
        int i = 0;
        
        for(LavaSimCell cell: cellsWithFluid)
        {
            saveData[i++] = cell.pos.getX();
            saveData[i++] = cell.pos.getY();
            saveData[i++] = cell.pos.getZ();
            saveData[i++] = Float.floatToIntBits(cell.getCurrentLevel());
        }       
        
        nbt.setIntArray(TAG_SAVE_DATA, saveData);
        
    }
}
