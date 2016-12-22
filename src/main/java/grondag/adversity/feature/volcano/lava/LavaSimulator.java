package grondag.adversity.feature.volcano.lava;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import grondag.adversity.Adversity;
import grondag.adversity.config.Config;
import grondag.adversity.library.Useful;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LavaSimulator
{
    protected final World world;
    protected final TerrainHelper terrainHelper;
//    protected final BlockPos origin;
//    public final int xOffset;
//    public final int zOffset;
    
//    private final FluidCell [][][] cells;
//    
//    private final static int RADIUS = 256;
//    private final static int ARRAY_LENGTH = RADIUS * 2 + 1;
    
    protected final HashMap<BlockPos, LavaSimCell> allCells = new HashMap<BlockPos, LavaSimCell>();
    private final HashSet<LavaSimCell> updatedCells = new HashSet<LavaSimCell>();
    protected final HashSet<LavaSimCell> cellsWithFluid = new HashSet<LavaSimCell>();
    
    /**
     * Blocks that need to be melted or checked for filler after placement.
     * Not saved to NBT because should be fully processed and cleared every tick.
     */
    private HashSet<BlockPos> adjustmentList = new HashSet<BlockPos>();
    
    /** cells that may need a block update */
    protected final HashSet<LavaSimCell> dirtyCells = new HashSet<LavaSimCell>();
    
    private final LinkedList<LavaBlockUpdate> blockUpdates = new LinkedList<LavaBlockUpdate>();

    public LavaSimulator(World world)
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
        }
        
        for(LavaSimCell cell : this.updatedCells)
        {
            cell.applyUpdates(this);
        }
        
        this.updatedCells.clear();
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
            IBlockState state = world.getBlockState(pos);
            if(terrainHelper.isLavaSpace(state))
            {
                result = new LavaSimCell(this, pos);
                allCells.put(pos, result);
            }
            else
            {
                result = BarrierCell.INSTANCE;
            }
        }
        return result;
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
                    this.world.setBlockState(update.pos, IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), update.level));
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
}
