package grondag.adversity.feature.volcano.lava;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.feature.volcano.lava.WorldStateBuffer.BlockStateBuffer;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * TODO

 * 
 * Replace connection skiplist with hashmap, make iteration unordered
 * 
 * Make connection iteration parallel - will require locking mechanism so that cells are read/updated atomically     
 * 
 * If a lava cell is topped by another lava cell, always give visual state of 12, even if internal fluid state is less
 *   may reduce number of block updates - use metadata to distinguish the model dispatch
 * 
 * Emergent surface not smooth enough - Improve Drop/slope Calculation for flowing terrain
 * Particle damage to entities
 *
 * Handle multiple worlds
 * Handle unloaded chunks
 *
 * Code Cleanup
 * Sounds
 * Missing top faces on some flow blocks - easier to tackle this after cooling in place - too transient to catch now
 * Particle model/rendering polish
 * Lava texture needs more character, more reddish?
 */
public class LavaSimulator extends SimulationNode
{
    protected final WorldStateBuffer worldBuffer;
    protected final LavaTerrainHelper terrainHelper;

    private final ConcurrentHashMap<BlockPos, LavaCell> allCells = new ConcurrentHashMap<BlockPos, LavaCell>(16000, 0.6F, 8);

    private final HashMap<BlockPos, LavaCell> lavaCells = new HashMap<BlockPos, LavaCell>();
    private final static String LAVA_CELL_NBT_TAG = "lavacells";
    private static final int LAVA_CELL_NBT_WIDTH = 6;
    
    /** Filler lava blocks that need to be cooled with lava cells but aren't involved in the fluid simulation. */
    private final HashSet<BlockPos> lavaFillers = new HashSet<BlockPos>();
    private final static String LAVA_FILLER_NBT_TAG = "lavafillers"; 
    private static final int LAVA_FILLER_NBT_WIDTH = 3;
    
    /** Basalt blocks that are awaiting cooling */
    private final CoolingBlockMap basaltBlocks = new CoolingBlockMap();
    private final static String BASALT_BLOCKS_NBT_TAG = "basaltblock"; 
    private static final int BASALT_BLOCKS_NBT_WIDTH = 4;

    private final ConnectionMap connections = new ConnectionMap();
    
    private final ParticleManager particles = new ParticleManager();
  
    /** Set true when doing block placements so we known not to register them as newly placed lava. */
    private boolean itMe = false;

    private int tickIndex = 0;
    private final static String TICK_INDEX_NBT_TAG = "tickindex"; 

    /**
     * Blocks that need to be melted or checked for filler after placement.
     * Not saved to NBT because should be fully processed and cleared every tick.
     */
    private HashSet<BlockPos> adjustmentList = new HashSet<BlockPos>();

    /** cells that may need a block update */
    protected final HashSet<LavaCell> dirtyCells = new HashSet<LavaCell>();


    public LavaSimulator(World world)
    {
        super(NodeRoots.LAVA_SIMULATOR.ordinal());
        this.worldBuffer = new WorldStateBuffer(world);
        this.terrainHelper = new LavaTerrainHelper(world);
    }

    /**
     * Does one ticks if my current tick index is less than the new last.
     * Does two if I am more than one behind.
     */
    public void doTicks(int newLastTickIndex)
    {
        if(this.tickIndex < newLastTickIndex)
        {
            this.tickIndex++;
            this.doTick();
            
            if(this.tickIndex < newLastTickIndex)
            {
                this.tickIndex++;
                this.doTick();
            }
        }
    }
    
    private void doTick()
    {
        if((this.tickIndex & 0xFF) == 0xFF)
        {
            Adversity.log.info("Particle time this sample = " + particleTime / 1000000);
            particleTime = 0;

            Adversity.log.info("Cooling time this sample = " + coolingTime / 1000000);
            coolingTime = 0;
            
            Adversity.log.info("Validation time this sample = " + validationTime / 1000000);
            validationTime = 0;

            Adversity.log.info("Block update time this sample = " + blockUpdateTime / 1000000);
            blockUpdateTime = 0;
            Adversity.log.info("Block updates this sample = " + blockUpdatesCounter + " or " + blockUpdatesCounter / 0xFF + " per tick");
            blockUpdatesCounter  = 0;
            
            Adversity.log.info("Cache cleanup time this sample = " + cacheCleanTime / 1000000);
            cacheCleanTime = 0;

            Adversity.log.info("Step time this sample = " + stepTime / 1000000);
            stepTime = 0;

            Adversity.log.info("Connection flow proccessing time this sample = " + connectionProcessTime / 1000000 
                    + " for " + connectionProcessCount + "requests @" + ((connectionProcessCount > 0) ? connectionProcessTime / connectionProcessCount : "") + " each");
            connectionProcessCount = 0;
            connectionProcessTime = 0;

            Adversity.log.info("getFlowRate time this sample = " + LavaCellConnection.getFlowRateTime / 1000000 
                    + " for " + LavaCellConnection.getFlowRateCount 
                    + " runs @" + ((LavaCellConnection.getFlowRateCount > 0) ? LavaCellConnection.getFlowRateTime / LavaCellConnection.getFlowRateCount : "") + " each");
            LavaCellConnection.getFlowRateTime = 0;
            LavaCellConnection.getFlowRateCount = 0;
            
            Adversity.log.info("getHorizontalFlowRate time this sample = " + LavaCellConnection.getHorizontalFlowRateTime / 1000000 
                    + " for " + LavaCellConnection.getHorizontalFlowRateCount 
                    + " runs @" + ((LavaCellConnection.getHorizontalFlowRateCount > 0) ? LavaCellConnection.getHorizontalFlowRateTime / LavaCellConnection.getHorizontalFlowRateCount : "") + " each");
            LavaCellConnection.getHorizontalFlowRateTime = 0;
            LavaCellConnection.getHorizontalFlowRateCount = 0;
            
            Adversity.log.info("getVerticalFlowRate time this sample = " + LavaCellConnection.getVerticalFlowRateTime / 1000000 
                    + " for " + LavaCellConnection.getVerticalFlowRateCount 
                    + " runs @" + ((LavaCellConnection.getVerticalFlowRateCount > 0) ? LavaCellConnection.getVerticalFlowRateTime / LavaCellConnection.getVerticalFlowRateCount : "") + " each");
            LavaCellConnection.getVerticalFlowRateTime = 0;
            LavaCellConnection.getVerticalFlowRateCount = 0;
            
            Adversity.log.info("lavaCells=" + this.lavaCells.size() + " totalCells=" + this.allCells.size() 
                    + " connections=" + this.connections.size() + " basaltBlocks=" + this.basaltBlocks.size() + " loadFactor=" + this.loadFactor());
//            int totalFluid = 0;
//            for(LavaCell cell : lavaCells.values())
//            {
//                totalFluid += cell.getFluidAmount();
//            }
            

        }
        
        long startTime = System.nanoTime();
        this.doCooling();
        this.doBasaltCooling();
        this.coolingTime += (System.nanoTime() - startTime);

        //Was causing slow propagation and overflow not doing this each step just prior to flow
        //        this.connections.values().parallelStream().forEach((LavaCellConnection c) -> {c.updateFlowRate(this);;});

        startTime = System.nanoTime();

        //TODO: consider having more steps?
        
     // force processing on non-dirty connection at least once per tick
        this.doStep(true);
        
        this.doStep(false);
        this.doStep(false);
        this.doStep(false);
        this.doStep(false);
        this.doStep(false);
        this.doStep(false);
        this.doStep(false);

        this.stepTime += (System.nanoTime() - startTime);

        this.doParticles();

        this.doBlockUpdates();
        
        if((this.tickIndex & 0x1F) == 0x1F)
        {
            
            startTime = System.nanoTime();
            validateAllCells();
            this.validationTime += (System.nanoTime() - startTime);

            startTime = System.nanoTime();
            cleanCellCache();
            this.cacheCleanTime += (System.nanoTime() - startTime);
       
        }

        this.setSaveDirty(true);
    }

//    private int particleCounter = 10;

    private long particleTime;
    private void doParticles()
    {
        long startTime = System.nanoTime();
       //TODO: make configurable
        int capacity =  10 - EntityLavaParticle.getLiveParticleCount(this.worldBuffer.realWorld.getMinecraftServer());
        
        if(capacity <= 0) return;
        
        EntityLavaParticle particle = this.particles.pollFirstEligible(this);
        
        while(capacity-- > 0 && particle != null )
        {
            worldBuffer.realWorld.spawnEntityInWorld(particle);
            particle = this.particles.pollFirstEligible(this);
        }
      
//        if(particleCounter-- == 0)
//        {
//            particleCounter = 5 + Useful.SALT_SHAKER.nextInt(5);
//
//            //TODO: sort bottom up
//            for(LavaCell cell : cellsWithFluid.values().toArray(new LavaCell[0]))
//            {
//                int amount = cell.getFluidAmount();
//                if(amount > 0 && cell.isDrop(this))
//                {
//                    world.spawnEntityInWorld(new EntityLavaParticle(world, amount, 
//                            new Vec3d(cell.pos.getX() + 0.5, cell.pos.getY() - 0.1, cell.pos.getZ() + 0.5), Vec3d.ZERO));
//                    cell.changeLevel(this, -amount);
//                }
//            }
//        }

        this.particleTime += (System.nanoTime() - startTime);
    }

    private LinkedList<BlockPos> scanningBlocks = new LinkedList<BlockPos>();
    private LinkedList<BlockPos> coolingBlocks = new LinkedList<BlockPos>();

    private long coolingTime;

    //TODO make tries per tick configurable
    private static final int COOLING_SCANS_PER_TICK = 10;
    private void doCooling()
    {
        if(scanningBlocks.isEmpty())
        {
            if(coolingBlocks.isEmpty())
            {
                for(LavaCell cell : this.lavaCells.values())
                {
                    if(cell.canCool(this)) scanningBlocks.add(cell.pos);
                }
                
                scanningBlocks.addAll(this.lavaFillers);
            }
            else
            {
                final BlockPos target = coolingBlocks.removeFirst();
                
                switch(canLavaCool(target))
                {
                    case YES:
                        LavaCell cell = this.getFluidCellIfItExists(target);
                        if(cell == null || cell.canCool(this))
                        {
                            coolLava(target);
                            
                            if(cell == null)
                            {
                                this.lavaFillers.remove(target);
                            }
                            else
                            {
                                cell.changeLevel(this, -cell.getFluidAmount());
                                cell.clearBlockUpdate();
                                cell.validate(this, true);
                            }
                        }
                        break;
                        
                        
                    case INVALID:
                        // not a cell (according to world) so try removing from fillers
                        this.lavaFillers.remove(target);
                        break;
                        
                    case NO:
                    default:
                        //NOOP - try again next pass
                        break;
                   
                }
            }
        }
        else
        {
            int attempts = 0;
            while(attempts++ < COOLING_SCANS_PER_TICK && !scanningBlocks.isEmpty())
            {
                BlockPos target = scanningBlocks.removeFirst();
                
                switch(canLavaCool(target))
                {
                    case INVALID:
                        // not a cell (according to world) so try removing from fillers
                        this.lavaFillers.remove(target);
                        break;
                        
                    case YES:
                        coolingBlocks.add(target);
                        break;
                        
                    case NO:
                    default:
                        break;
                }
            }
        }
    }

    private static final int BLOCK_COOLING_ATTEMPTS_PER_TICK = 400;
    private static final int BLOCK_COOLING_DELAY_TICKS = 20;
    private void doBasaltCooling()
    {
        int lastEligibleTick = this.tickIndex - BLOCK_COOLING_DELAY_TICKS;
        int count = 0;
        
        while(count++ < BLOCK_COOLING_ATTEMPTS_PER_TICK && !this.basaltBlocks.isEmpty())
        {
            AgedBlockPos apos = this.basaltBlocks.pollFirst();
            
            if(apos.getTick() <= lastEligibleTick)
            {
                IBlockState state = this.worldBuffer.getBlockState(apos.pos);
                Block block = state.getBlock();
                if(block instanceof CoolingBlock)
                {
                    switch(((CoolingBlock)block).tryCooling(this.worldBuffer, apos.pos, state))
                    {
                    
                        case PARTIAL:
                            // will be ready to cool again after delay
                            apos.setTick(this.tickIndex);
                            basaltBlocks.add(apos);
                            break;
                            
                        case UNREADY:
                            // let it try again, put at the back of the line
                            basaltBlocks.add(apos);
                            break;
                            
                        case COMPLETE:
                        case INVALID:
                        default:
                            // NOOP - just let these fall out of collection
                            break;
                      
                    }
                }
            }
            else
            {
                // not ready yet, so place at back of queue
                basaltBlocks.add(apos);
            }
        }
        
        
    }
    
    
    private enum CanLavaCool
    {
        YES,
        NO,
        INVALID
    }
    
    /**
     * Returns value to show if lava can cool based on world state alone. Does not consider age.
     */
    private CanLavaCool canLavaCool(BlockPos pos)
    {
        Block block = worldBuffer.getBlockState(pos).getBlock();
        
        if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || block == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            int hotNeighborCount = 0;
            BlockPos.MutableBlockPos nPos = new BlockPos.MutableBlockPos();
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                Vec3i vec = face.getDirectionVec();
                nPos.setPos(pos.getX() + vec.getX(), pos.getY() + vec.getY(), pos.getZ() + vec.getZ());
                
                block = worldBuffer.getBlockState(nPos).getBlock();
                if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || block == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                {
                    // don't allow top to cool until bottom does
                    if(face == EnumFacing.DOWN) return CanLavaCool.NO;
                    
                    hotNeighborCount++;
                }
            }
            
            return hotNeighborCount < 4 ? CanLavaCool.YES : CanLavaCool.NO;
        }
        else
        {
            return CanLavaCool.INVALID;
        }
    }
    
    private void coolLava(BlockPos pos)
    {
        final IBlockState priorState = this.worldBuffer.getBlockState(pos);
        Block currentBlock = priorState.getBlock();
        NiceBlock newBlock = null;
        if(currentBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            newBlock = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
        }
        else if(currentBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            newBlock = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK;
        }

        if(newBlock != null)
        {
//            Adversity.log.info("Cooling lava @" + pos.toString());
            //should not need these any more due to world buffer
//            this.itMe = true;
            this.worldBuffer.setBlockState(pos, newBlock.getDefaultState().withProperty(NiceBlock.META, priorState.getValue(NiceBlock.META)), priorState);
//            this.itMe = false;
            this.basaltBlocks.add(new AgedBlockPos(pos, this.tickIndex));
        }
    }

    private long stepTime;
    private long connectionProcessTime;
    private int connectionProcessCount;
    
    public void doStep(boolean force)
    {
        long startTime = System.nanoTime();
        connectionProcessCount += this.connections.size();
        this.connections.getSortedValues().stream().forEach((LavaCellConnection c) -> c.doStep(this, force));
        this.connectionProcessTime += (System.nanoTime() - startTime);
    }

    private long validationTime;

    private void validateAllCells()
    {
        //TODO: make parallel
        for(LavaCell cell : this.allCells.values().toArray(new LavaCell[0]))
        {
            cell.validate(this, false);
        }

    }

    private long cacheCleanTime;

    private void cleanCellCache()
    {
        this.allCells.values().parallelStream().forEach((LavaCell c) -> {
            if(!c.isRetained() && c.getFluidAmount() == 0) 
            {
                this.allCells.remove(c.pos);
            }
        });
//        Iterator<Entry<BlockPos, LavaCell>> it = this.allCells.entrySet().iterator();
//
//        while(it.hasNext())
//        {
//            Entry<BlockPos, LavaCell> next = it.next();
//            if(!next.getValue().isRetained())
//            {
//                it.remove();
//            }
//        }

    }

    /**
     * Retrieves existing cell or creates new if not found.
     * For existing cells, will validate vs. world if validateExisting = true;
     */
    public LavaCell getCell(BlockPos pos, boolean validateExisting)
    {
        LavaCell result = allCells.get(pos);

        if(result == null)
        {
            result = new LavaCell(this, pos.toImmutable());
            allCells.put(result.pos, result);
            result.validate(this, true);
        }
        else if(validateExisting)
        {
            result.validate(this, true);
        }
        return result;
    }

    /** returns a cell only if it contains fluid */
    public LavaCell getCellIfItExists(BlockPos pos)
    {
        return this.allCells.get(pos);
    }
    
    /** returns a cell only if it contains fluid */
    public LavaCell getFluidCellIfItExists(BlockPos pos)
    {
        return this.lavaCells.get(pos);
    }
    
    public LavaCellConnection getConnection(BlockPos pos1, BlockPos pos2)
    {
        return connections.get(new CellConnectionPos(pos1, pos2));
    }

    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Also called by random tick on cooling blocks so that they can't get permanently orphaned
     */
    public void registerCoolingBlock(World worldIn, BlockPos pos)
    {
        if(itMe) return;
        this.basaltBlocks.add(new AgedBlockPos(pos, this.tickIndex));
        this.setSaveDirty(true);
    }


    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if we are currently placing blocks.
     */
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            this.lavaFillers.add(pos);
        }
        else if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            int worldLevel = IFlowBlock.getFlowHeightFromState(state);
            LavaCell target = this.getCell(pos, false);
            if(target.getLastVisibleLevel() != worldLevel)
            {           
                target.validate(this, true);
                this.setSaveDirty(true);
            }
        }
    }

    /**
     * Update simulation from world when blocks are removed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        LavaCell target = this.getCell(pos, false);
        if(target.getFluidAmount() > 0)
        {
            target.validate(this, true);
        }
        this.setSaveDirty(true);
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
            if(!lavaCells.containsKey(cell.pos))
            {
                cell.retain("updateFluidStatus self");
                this.lavaCells.put(cell.pos, cell);
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    LavaCell other = this.getCell(cell.pos.add(face.getDirectionVec()), false);
                    other.retain("updateFluidStatus " + face.toString() + " from " + this.hashCode());
                    if(!other.isBarrier())
                    {
                        this.addConnection(cell.pos, other.pos);
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
            if(lavaCells.containsKey(cell.pos))
            {
                lavaCells.remove(cell.pos);
                cell.release("updateFluidStatus self");
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    BlockPos otherPos = cell.pos.add(face.getDirectionVec());
                    
                    //cell should exist but don't create it if not
                    LavaCell other = this.getCellIfItExists(otherPos);
                    if(other != null)
                    {
                        other.release("updateFluidStatus " + face.toString() + " from " + this.hashCode());
                        if(other.getFluidAmount() == 0)
                        {
                            // Remove connection if neither has any fluid
                            this.removeConnection(cell.pos, otherPos);
                        }
                    }
                    else
                    {
                        // if cell was somehow missing don't assume connection is
                        this.removeConnection(cell.pos, otherPos);
                    }

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
     * Should only force resynch with world when you know you removed a barrier and simulation
     * needs to know the cell is now open.  Otherwise if this addition is occurs
     * after an earlier one but before block update resynch will cause earlier addition to be lost.
     */
    public void addLava(BlockPos pos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
//        Adversity.log.info("addLava amount=" + amount + " @" + pos.toString());

        int available = amount;

       
        LavaCell target = this.getCell(pos.down(), shouldResynchToWorldBeforeAdding);
        int capacity = target.getCapacity();
        int flow = Math.min(capacity, available);
        if(flow > 0)
        {
            target.changeLevel(this, flow);
            available -= flow;
        }

        if(available > 0)
        {
            target = this.getCell(pos, shouldResynchToWorldBeforeAdding);
            capacity = target.getCapacity();
            flow = Math.min(capacity, available);
            if(flow > 0)
            {
                target.changeLevel(this, flow);
                available -= flow;
            }

            if(available > 0)
            {
                target = this.getCell(pos.up(), shouldResynchToWorldBeforeAdding);
                
                //add lava at pressure if necessary
               if(target.getCapacity() > 0 || target.getFluidAmount() > 0)
               {
                   target.changeLevel(this, available);
                   available = 0;
               }
               else
               {
                    // try to add a pressure in the primary target cell
                    target = this.getCell(pos, shouldResynchToWorldBeforeAdding);
                    if(target.getFluidAmount() > 0)
                    {
                        target.changeLevel(this, available);
                        available = 0;
                    }
                    else
                    {
                        Adversity.log.info("LAVA EATING IS A THING! Amount=" + available + " @" + pos.toString());
                    }       
                }
            }
        }
    }

    public void queueParticle(BlockPos pos, int amount)
    {
//        Adversity.log.info("queueParticle amount=" + amount +" @"+ pos.toString());

        this.particles.addLavaForParticle(this, pos, amount);
    }

    public void addConnection(BlockPos pos1, BlockPos pos2)
    {
        this.connections.createConnectionIfNotPresent(this, new CellConnectionPos(pos1, pos2));
    }

    public void removeConnection(BlockPos pos1, BlockPos pos2)
    {
        this.connections.remove(new CellConnectionPos(pos1, pos2));
    }

    private void provideBlockUpdates()
    {
        for(LavaCell cell : this.dirtyCells)
        {
            cell.provideBlockUpdate(this, this.adjustmentList);
        }
        dirtyCells.clear();

    }

    private static int blockUpdatesCounter;

    private long blockUpdateTime;
    public void doBlockUpdates()
    {
        //        Adversity.log.info("LavaSim doBlockUpdates");
        long startTime = System.nanoTime();

        provideBlockUpdates();
        doAdjustments();


        
        this.itMe = true;
        blockUpdatesCounter += this.worldBuffer.applyBlockUpdates(1);
        this.itMe = false;

        this.blockUpdateTime += (System.nanoTime() - startTime);
    }

    private void doAdjustments()
    {
        //TODO: Use mutable blockpos here - gets called frequently 

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

        for(BlockPos target : targets)
        {
            for(int y = -2; y <= 2; y++)
            {
                BlockPos p = target.add(0, y, 0);
                if(!adjustHeightBlockIfNeeded(p));
                {
                    BlockStateBuffer update = IFlowBlock.adjustFillIfNeeded(this.worldBuffer, p);
                    
                    // Update set of lava filler blocks that need cooling if we are adding or removing lava filler
                    // and update cooling lava if it is any other type of filler.
                    if(update != null)
                    {
                        if(update.getNewState().getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                        {
                            this.lavaFillers.add(update.getBlockPos());
                        }
                        else
                        {
                            this.lavaFillers.remove(update.getBlockPos());
                            
                            if(update.getNewState().getBlock() instanceof CoolingBlock)
                            {
                                this.basaltBlocks.add(new AgedBlockPos(update.getBlockPos(), this.getTickIndex()));
                            }
                        }
                        this.worldBuffer.addUpdate(update);
                    }
                }
            }
        }
    }

    /**
     * Melts static height blocks if geometry would be different from current.
     * And turns full cube dynamic blocks into static cube blocks.
     * Returns true if is a height block, even if no adjustment was needed.
     */
    private boolean adjustHeightBlockIfNeeded(BlockPos targetPos)
    {

        if(targetPos == null) return false;

        final IBlockState priorState = this.worldBuffer.getBlockState(targetPos);
        if(!(priorState.getBlock() instanceof NiceBlock)) return false;

        NiceBlock block = (NiceBlock)priorState.getBlock();

        if(!IFlowBlock.isFlowHeight(block)) return false;

        boolean isFullCube = IFlowBlock.shouldBeFullCube(priorState, this.worldBuffer, targetPos);


        if(isFullCube)
        {
            if(block == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
            {
                this.worldBuffer.setBlockState(targetPos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, priorState.getValue(NiceBlock.META)), priorState);
            }
        }
        else if (block == NiceBlockRegistrar.COOL_STATIC_BASALT_HEIGHT_BLOCK 
                || block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK )
        {
            this.worldBuffer.setBlockState(targetPos, NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getDefaultState()
                    .withProperty(NiceBlock.META, priorState.getValue(NiceBlock.META)), priorState);
        }

        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        allCells.clear();
        lavaCells.clear();
        lavaFillers.clear();
        basaltBlocks.clear();
        connections.clear();
        adjustmentList.clear();
        dirtyCells.clear();
        
        this.tickIndex = nbt.getInteger(TICK_INDEX_NBT_TAG);
        
        this.worldBuffer.readFromNBT(nbt);
        this.particles.readFromNBT(nbt);

        // LOAD LAVA CELLS
        int[] saveData = nbt.getIntArray(LAVA_CELL_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % LAVA_CELL_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Lava blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                LavaCell cell = new LavaCell(this, new BlockPos(saveData[i++], saveData[i++], saveData[i++]), saveData[i++]);
                if(saveData[i] == Integer.MAX_VALUE)
                {
                    cell.setLastFlowTick(this.tickIndex);
                    cell.setNeverCools(true);
                }
                else
                {
                    cell.setLastFlowTick(saveData[i]);
                }
                i++;
                cell.setFloor(saveData[i++]);
                cell.clearBlockUpdate();
                this.allCells.put(cell.pos, cell);
            }
            
            // wait until all cells are added to collection, otherwise will recreate them all from the world recusively
            // Careful here: allCells is concurrent, so have to iterate a snapshot of it or will keep adding new non-lava cells as lava cells
            for(LavaCell cell : this.allCells.values().toArray(new LavaCell[0]))
            {
                this.updateFluidStatus(cell, true);
                cell.updateRetainedLevel(this);
            }
            
            Adversity.log.info("Loaded " + lavaCells.size() + " lava cells.");
        }
        
        // LOAD FILLER CELLS
        saveData = nbt.getIntArray(LAVA_FILLER_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % LAVA_FILLER_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Filler blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                this.lavaFillers.add(new BlockPos(saveData[i++], saveData[i++], saveData[i++]));
            }
            Adversity.log.info("Loaded " + lavaFillers.size() + " filler cells.");
        }
        
        // LOAD BASALT BLOCKS
        saveData = nbt.getIntArray(BASALT_BLOCKS_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % BASALT_BLOCKS_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Cooling basalt blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                this.basaltBlocks.add(new AgedBlockPos(new BlockPos(saveData[i++], saveData[i++], saveData[i++]), saveData[i++]));
            }
            Adversity.log.info("Loaded " + basaltBlocks.size() + " cooling basalt blocks.");
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        
        nbt.setInteger(TICK_INDEX_NBT_TAG, this.tickIndex);
        
        // SAVE LAVA CELLS
        {
            Adversity.log.info("Saving " + lavaCells.size() + " lava cells.");
            int[] saveData = new int[lavaCells.size() * LAVA_CELL_NBT_WIDTH];
            int i = 0;
    
            for(LavaCell cell: lavaCells.values())
            {
                saveData[i++] = cell.pos.getX();
                saveData[i++] = cell.pos.getY();
                saveData[i++] = cell.pos.getZ();
                saveData[i++] = cell.getFluidAmount();
                saveData[i++] = cell.getNeverCools() ? Integer.MAX_VALUE : cell.getLastFlowTick();
                saveData[i++] = cell.getFloor();
            }         
            nbt.setIntArray(LAVA_CELL_NBT_TAG, saveData);
        }
        
        // SAVE FILLER CELLS
        {
            Adversity.log.info("Saving " + lavaFillers.size() + " filler cells.");
            int[] saveData = new int[lavaFillers.size() * LAVA_FILLER_NBT_WIDTH];
            int i = 0;
            for(BlockPos pos: lavaFillers)
            {
                saveData[i++] = pos.getX();
                saveData[i++] = pos.getY();
                saveData[i++] = pos.getZ();
            }       
            nbt.setIntArray(LAVA_FILLER_NBT_TAG, saveData);
        }
        
        // SAVE BASALT BLOCKS
        {
            Adversity.log.info("Saving " + basaltBlocks.size() + " cooling basalt blocks.");
            int[] saveData = new int[basaltBlocks.size() * BASALT_BLOCKS_NBT_WIDTH];
            int i = 0;
            for(AgedBlockPos apos: basaltBlocks.values())
            {
                saveData[i++] = apos.pos.getX();
                saveData[i++] = apos.pos.getY();
                saveData[i++] = apos.pos.getZ();
                saveData[i++] = apos.getTick();
            }       
            nbt.setIntArray(BASALT_BLOCKS_NBT_TAG, saveData);
            
            this.worldBuffer.writeToNBT(nbt);
            this.particles.writeToNBT(nbt);
        }

    }

    public int getTickIndex()
    {
        return this.tickIndex;
    }

    /**
     * Signal to let volcano know should switch to cooling mode.
     * 1 or higher means overloaded.
     */
    public float loadFactor()
    {
        return Math.max((float)this.connections.size() / 30000F, (float)this.lavaCells.size() / 10000F);
    }


}