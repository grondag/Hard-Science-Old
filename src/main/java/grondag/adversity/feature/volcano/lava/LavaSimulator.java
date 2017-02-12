package grondag.adversity.feature.volcano.lava;


import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.feature.volcano.lava.LavaCellConnection.BottomType;
import grondag.adversity.feature.volcano.lava.ParticleManager.ParticleInfo;
import grondag.adversity.library.PackedBlockPos;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * TODO
 * 
 * Base terrain retention calc - ignore flow blocks
 * Flat flow calc - slope the edges of the spread on a flat surface until one level deep
 * 
 * 
 * VALIDATE FIX: Floating flow blocks.  May be due to downward flows skipping over meltable cells because they aren't supported
 * thus causing the cell with an interior floor to be orphaned.  See updateFloor and changeLevel and possibly getCellForLavaAddition
 * Easiest fix is probably to ensure that all covered height blocks are full height meta on cooling
 * Or maybe just scrap the checks for melting and allow vertical flow to handle unsupported melting when it occurs?
 * 
 * VALIDATE FIX: appears downward flow is not fast enough - getting strangely suspended blocks that eventually go away
 * 
 * VALIDATE FIX: melt static basalt when lava flows on/near it
 * 
 * VALIDATE: Determine and implement connection processing order
 * Bucket connections into verticals and the horizontals by drop
 * TEST: is it more efficient to sort and process vertically? Top to bottom? Bottom to top?
 * If will be sorted, use previous connection sort order to reduce sort times.
 * If connection processing will be concurrent, add locking mechanism for flowAcross that won't cause deadlocks
 * 
 * Handle block break/neighbor change events for lava and basalt blocks to invalidate sim/worldbuffer state
 * 
 * Find way to avoid processing static lava in volcano core
 * 
 * Handle multiple worlds or limit to a single world
 * 
 * Handle unloaded chunks
 *   
 * Make LavaCell concurrency more robust
 * Concurrency / performance
 * 
 * Particle damage to entities
 *
 *
 * Code Cleanup
 * Sounds
 * Missing top faces on some flow blocks - probably a hash collision problem - may not be fixable with occlusion on
 * Particle model/rendering polish
 * Lava texture needs more character, more reddish?
 * 
 * Have volcano place lava more quickly when game clock skips ahead.
 * Smoke
 * Ash
 * Haze
 * 
 */
public class LavaSimulator extends SimulationNode
{
    protected final WorldStateBuffer worldBuffer;
    protected final LavaTerrainHelper terrainHelper;
    
    public static final ForkJoinPool LAVA_THREAD_POOL = new ForkJoinPool();

    private final ConcurrentHashMap<Long, LavaCell> allCells = new ConcurrentHashMap<Long, LavaCell>(16000, 0.6F, 8);
    private final static String LAVA_CELL_NBT_TAG = "lavacells";
    private static final int LAVA_CELL_NBT_WIDTH = 5;
    
    //TODO: consider switching back to BlockPos - if going to be going back to world or checks would avoid reinstantiation each time
    /** Filler lava blocks that need to be cooled with lava cells but aren't involved in the fluid simulation. */
    private final Set<Long> lavaFillers = ConcurrentHashMap.newKeySet();
    private final static String LAVA_FILLER_NBT_TAG = "lavafillers"; 
    private static final int LAVA_FILLER_NBT_WIDTH = 2;
    
    /** Basalt blocks that are awaiting cooling */
    private final Set<AgedBlockPos> basaltBlocks = ConcurrentHashMap.newKeySet();
    
    private final static String BASALT_BLOCKS_NBT_TAG = "basaltblock"; 
    private static final int BASALT_BLOCKS_NBT_WIDTH = 4;

    private final ConnectionMap connections = new ConnectionMap();
    
    private final ParticleManager particles = new ParticleManager();
    
    /** Set true when doing block placements so we known not to register them as newly placed lava. */
    private boolean itMe = false;

    private int tickIndex = 0;
    private final static String TICK_INDEX_NBT_TAG = "tickindex"; 

    public LavaSimulator(World world)
    {
        super(NodeRoots.LAVA_SIMULATOR.ordinal());
        this.worldBuffer = new WorldStateBuffer(world);
        this.terrainHelper = new LavaTerrainHelper(world);
    }

    /**
     * Updates fluid simulation for one game tick, provided the game clock has advanced at least one tick since last call.
     * Tick index is used internally to track which cells have changed and to control frequency of upkeep tasks.
     * Due to computationally intensive nature, does not do more work if game clock has advanced more than one tick.
     * To make lava flow more quickly, place more lava when clock advances.
     */
    public void doTick(int newLastTickIndex)
    {
        if(this.tickIndex < newLastTickIndex)
        {
            this.tickIndex++;
            this.doTickWork();
        }
    }
    
    private static long fluidUpdateTime = 0;
    
    private void doTickWork()
    {
        if((this.tickIndex & 0xFF) == 0xFF)
        {
            Adversity.log.info("Particle time this sample = " + particleTime / 1000000);
            Adversity.log.info("Live particle count = " + EntityLavaParticle.getLiveParticleCount(this.worldBuffer.realWorld.getMinecraftServer()));
            particleTime = 0;

            Adversity.log.info("Cooling time this sample = " + coolingTime / 1000000);
            coolingTime = 0;
            
            Adversity.log.info("Validation time this sample = " + validationTime / 1000000);
            validationTime = 0;
            
            Adversity.log.info("Cache cleanup time this sample = " + cacheCleanTime / 1000000);
            cacheCleanTime = 0;

            Adversity.log.info("Block update provision time this sample = " + blockUpdateProvisionTime / 1000000 
                    + " for " + blockUpdatesProvisionCounter + " updates @ " + ((blockUpdatesProvisionCounter > 0) ? (float)blockUpdateProvisionTime / blockUpdatesProvisionCounter : "n/a") + " each");
            blockUpdateProvisionTime = 0;
            blockUpdatesProvisionCounter  = 0;

            Adversity.log.info("Block update application time this sample = " + blockUpdateApplicationTime / 1000000 
                    + " for " + blockUpdatesApplicationCounter + " updates @ " + ((blockUpdatesApplicationCounter > 0) ? (float)blockUpdateApplicationTime / blockUpdatesApplicationCounter : "n/a") + " each");
            blockUpdateApplicationTime = 0;
            blockUpdatesApplicationCounter  = 0;
            
            Adversity.log.info("Step time this sample = " + stepTime / 1000000);
            stepTime = 0;

            Adversity.log.info("Connection flow proccessing time this sample = " + connectionProcessTime / 1000000 
                    + " for " + connectionProcessCount + " links @ " + ((connectionProcessCount > 0) ? (float)connectionProcessTime / connectionProcessCount : "n/a") + " each");
            connectionProcessCount = 0;
            connectionProcessTime = 0;
            
            Adversity.log.info("Fluid status update time this sample = " + fluidUpdateTime / 1000000);
            fluidUpdateTime = 0;

//            Adversity.log.info("getFlowRate time this sample = " + LavaCellConnection.getFlowRateTime / 1000000 
//                    + " for " + LavaCellConnection.getFlowRateCount 
//                    + " runs @" + ((LavaCellConnection.getFlowRateCount > 0) ? LavaCellConnection.getFlowRateTime / LavaCellConnection.getFlowRateCount : "") + " each");
//            LavaCellConnection.getFlowRateTime = 0;
//            LavaCellConnection.getFlowRateCount = 0;
//            
//            Adversity.log.info("getHorizontalFlowRate time this sample = " + LavaCellConnection.getHorizontalFlowRateTime / 1000000 
//                    + " for " + LavaCellConnection.getHorizontalFlowRateCount 
//                    + " runs @" + ((LavaCellConnection.getHorizontalFlowRateCount > 0) ? LavaCellConnection.getHorizontalFlowRateTime / LavaCellConnection.getHorizontalFlowRateCount : "") + " each");
//            LavaCellConnection.getHorizontalFlowRateTime = 0;
//            LavaCellConnection.getHorizontalFlowRateCount = 0;
//            
//            Adversity.log.info("getVerticalFlowRate time this sample = " + LavaCellConnection.getVerticalFlowRateTime / 1000000 
//                    + " for " + LavaCellConnection.getVerticalFlowRateCount 
//                    + " runs @" + ((LavaCellConnection.getVerticalFlowRateCount > 0) ? LavaCellConnection.getVerticalFlowRateTime / LavaCellConnection.getVerticalFlowRateCount : "") + " each");
//            LavaCellConnection.getVerticalFlowRateTime = 0;
//            LavaCellConnection.getVerticalFlowRateCount = 0;
            
            Adversity.log.info("totalCells=" + this.allCells.size() 
                    + " connections=" + this.connections.size() + " basaltBlocks=" + this.basaltBlocks.size() + " loadFactor=" + this.loadFactor());
//            int totalFluid = 0;
//            for(LavaCell cell : lavaCells.values())
//            {
//                totalFluid += cell.getFluidAmount();
//            }
            

        }
     
        long startTime = System.nanoTime();

        //TODO: consider having more steps?
        
        // force processing on non-dirty connection at least once per tick
        this.doFirstStep();
        this.doStep();
        this.doStep();
        this.doStep();
        this.doStep();
        this.doStep();
        this.doStep();
        // update sort keys on last pass for resort next tick
        this.doLastStep();

        this.stepTime += (System.nanoTime() - startTime);

        startTime = System.nanoTime();
        this.allCells.values().parallelStream().forEach(c -> c.updateFluidStatus(this));
        fluidUpdateTime  += (System.nanoTime() - startTime);
        
        this.doParticles();

        this.doBlockUpdates();
        
        
        int tickSelector = this.tickIndex & 0xF;
     
        // do these on alternate ticks to help avoid ticks that are too long
        if(tickSelector == 4)
        {
            startTime = System.nanoTime();
            this.doLavaCooling();
            this.coolingTime += (System.nanoTime() - startTime);
        }
        else if(tickSelector == 8)
        {
            startTime = System.nanoTime();
            this.doBasaltCooling();
            this.coolingTime += (System.nanoTime() - startTime);
        }
        else if(tickSelector == 12)
        {
            startTime = System.nanoTime();
            validateAllCells();
            connections.validateConnections(this);
            this.validationTime += (System.nanoTime() - startTime);
        }
        else if(tickSelector == 0)
        {
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
        
        Collection<ParticleInfo> particles = this.particles.pollEligible(this, capacity);
        
        if(particles != null && !particles.isEmpty())
        {
            for(ParticleInfo p : particles)
            {
            
                LavaCell cell = this.getCell(p.packedBlockPos, false);
                
                // abort on strangeness
                if(cell.isBarrier()) continue;
                
                // if particle destination somehow has fluid, just put it there
                if(cell.getFluidAmount() > 0)
                {
                    cell.changeLevel(this, p.getFluidUnits());
                }
                
                // go direct to ground if not too far - don't spawn an entity
                else if(cell.getDistanceToFlowFloor() < LavaCell.FLOW_FLOOR_DISTANCE_REALLY_FAR)
                {
                    cell = this.getCellForLavaAddition(p.packedBlockPos, false);
                    LavaCell down = cell.getDownEfficiently(this, false);
                    
//                    if(down != null && !down.isBarrier() && down.getFluidAmount() == 0)
//                        Adversity.log.info("wut?");
                    
                    if(cell != null) cell.changeLevel(this, p.getFluidUnits());
                }
                
                // Spawn in world, discarding particles that have aged out and aren't big enough to form a visible lava block
                else if(p.getFluidUnits() >= LavaCell.FLUID_UNITS_PER_LEVEL)
                {
                    EntityLavaParticle elp = new EntityLavaParticle(this.worldBuffer.realWorld, p.getFluidUnits(), 
                          new Vec3d(
                                  PackedBlockPos.getX(p.packedBlockPos) + 0.5, 
                                  PackedBlockPos.getY(p.packedBlockPos) + 0.4, 
                                  PackedBlockPos.getZ(p.packedBlockPos) + 0.5
                              ),
                          Vec3d.ZERO);
                    
                    worldBuffer.realWorld.spawnEntityInWorld(elp);
                }
            }
        }
      
        this.particleTime += (System.nanoTime() - startTime);
    }


    private long coolingTime;

    private void doLavaCooling()
    {
       LAVA_THREAD_POOL.submit( () ->
           this.allCells.values().parallelStream()
                .filter(c -> c.getFluidAmount() > 0 && canLavaCool(c.packedBlockPos) && c.canCool(this))
                .collect(Collectors.toList())
                .parallelStream().forEach(c -> 
                {
                    coolLava(c.packedBlockPos);
                    c.changeLevel(this, -c.getFluidAmount());
                    c.clearBlockUpdate(this);
                    c.validate(this);
                })).join();
       
       LAVA_THREAD_POOL.submit( () ->
               this.lavaFillers.parallelStream().forEach( p -> 
                    {
                        if(canLavaCool(p))
                        {
                            coolLava(p);
                            this.lavaFillers.remove(p);
                        }
                    })).join();
    }

    private static final int BLOCK_COOLING_DELAY_TICKS = 20;

    
    private void doBasaltCooling()
    {
        final int lastEligibleTick = this.tickIndex - BLOCK_COOLING_DELAY_TICKS;

        LAVA_THREAD_POOL.submit( () -> this.basaltBlocks.parallelStream().forEach(apos ->
        {
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
                            break;
                            
                        case UNREADY:
                            // do nothing and try again later
                            break;
                            
                        case COMPLETE:
                        case INVALID:
                        default:
                            //notify to remove from collection
                            basaltBlocks.remove(apos);
                    }
                }
                else
                {
                    basaltBlocks.remove(apos);
                }
            };
        })).join();       
    }
    
    /**
     * Returns value to show if lava can cool based on world state alone. Does not consider age.
     */
    private boolean canLavaCool(long packedBlockPos)
    {
        BlockPos pos = PackedBlockPos.unpack(packedBlockPos);
        
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
                    if(face == EnumFacing.DOWN) return false;
                    
                    hotNeighborCount++;
                }
            }
            
            return hotNeighborCount < 4;
        }
        else
        {
            // Might be invisible lava (not big enough to be visible in world)
            return true;
        }
    }
    
    private void coolLava(long packedBlockPos)
    {
        final IBlockState priorState = this.worldBuffer.getBlockState(packedBlockPos);
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
            this.worldBuffer.setBlockState(packedBlockPos, newBlock.getDefaultState().withProperty(NiceBlock.META, priorState.getValue(NiceBlock.META)), priorState);
//            this.itMe = false;
            this.basaltBlocks.add(new AgedBlockPos(PackedBlockPos.unpack(packedBlockPos), this.tickIndex));
        }
    }

    private long stepTime;
    private long connectionProcessTime;
    private int connectionProcessCount;
    
    public void doFirstStep()
    {
        long startTime = System.nanoTime();
        connectionProcessCount += this.connections.size();
        final int size = this.connections.size();
        LavaCellConnection[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doFirstStep(this);
        }

//        this.connections.values().stream().forEach((LavaCellConnection c) -> c.doFirstStep(this));
        this.connectionProcessTime += (System.nanoTime() - startTime);
    }

    public void doStep()
    {
        long startTime = System.nanoTime();
        connectionProcessCount += this.connections.size();
        final int size = this.connections.size();
        LavaCellConnection[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doStep(this);
        }
        
//        this.connections.values().stream().forEach((LavaCellConnection c) -> c.doStep(this));
        this.connectionProcessTime += (System.nanoTime() - startTime);
    }
    
    public void doLastStep()
    {
        long startTime = System.nanoTime();
        connectionProcessCount += this.connections.size();
        final int size = this.connections.size();
        LavaCellConnection[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doStep(this);
//            values[i].updateSortKey();
        }
        
//        this.connections.values().stream().forEach((LavaCellConnection c) -> c.doStep(this));
        this.connectionProcessTime += (System.nanoTime() - startTime);
    }
    
    private long validationTime;
    private void validateAllCells()
    {
        this.allCells.values().parallelStream().forEach(c -> c.validate(this));
    }

    private long cacheCleanTime;

    private void cleanCellCache()
    {
        //TODO: make parallel a config option
        LAVA_THREAD_POOL.submit(() ->
            this.allCells.values().parallelStream().forEach((LavaCell c) -> 
            {
                if(!c.isRetained() && c.getFluidAmount() == 0) 
                {
                    c.setDeleted(this);
                    this.allCells.remove(c.packedBlockPos);
                }
            }
        )).join();
       
    }

    /**
     * Retrieves existing cell or creates new if not found.
     * For existing cells, will validate vs. world if validateExisting = true;
     */
    public LavaCell getCell(BlockPos pos, boolean validateExisting)
    {
        return this.getCell(PackedBlockPos.pack(pos), validateExisting);
    }
    
    /**
     * Retrieves existing cell or creates new if not found.
     * For existing cells, will validate vs. world if validateExisting = true;
     */
    public LavaCell getCell(long packedBlockPos, boolean validateExisting)
    {
        LavaCell result = allCells.get(packedBlockPos);

        if(result == null)
        {
            boolean needsValidation = false;
            synchronized(allCells)
            {
                //confirm hasn't been added by another thread
                result = allCells.get(packedBlockPos);
                
                if(result == null)
                {
                    result = new LavaCell(this, packedBlockPos);
                    needsValidation = true;
                    allCells.put(packedBlockPos, result);
                }
            }
            //moving validation outside synch to prevent deadlock with connection collection
            if(needsValidation) result.validate(this);
        }
        else if(validateExisting)
        {
            result.validate(this);
        }
        return result;
    }

    /**
     * Finds closest vertically aligned non-drop lava cell at location.
     * If location is a barrier will attempt cell above. If that cell is also a barrier, will return null.
     * If location is a drop, will go down until it finds a non-drop cell.
     */
    public LavaCell getCellForLavaAddition(long packedBlockPos, boolean shouldResynchToWorldIfExists)
    {
        LavaCell candidate = this.getCell(packedBlockPos, shouldResynchToWorldIfExists);
        LavaCell previousCandidate = null;
        if(candidate.isBarrier() && PackedBlockPos.getY(candidate.packedBlockPos) < 255)
        {
            candidate = candidate.getUpEfficiently(this, shouldResynchToWorldIfExists);
        }
        else
        {
            
            while(candidate.getDistanceToFlowFloor() > LavaCell.LEVELS_PER_BLOCK && candidate.getFluidAmount() == 0)
            {
                previousCandidate = candidate;
                candidate = candidate.getDownEfficiently(this, shouldResynchToWorldIfExists);
            }
        }
        
        if(candidate.getCapacity() > 0)
        {
            return candidate;
        }
        else if(previousCandidate != null && previousCandidate.getCapacity() > 0)
        {
            return previousCandidate;
        }
        else
        {
            return null;
        }
    }
    
    public LavaCell getCellForLavaAddition(BlockPos pos, boolean shouldResynchToWorldIfExists)
    {
        return this.getCellForLavaAddition(PackedBlockPos.pack(pos), shouldResynchToWorldIfExists);
    }
    
    /** returns a cell only if it contains fluid */
    public LavaCell getCellIfItExists(long packedPos)
    {
        return this.allCells.get(packedPos);
    }
    
    public LavaCellConnection getConnection(long packedConnectionPos)
    {
        return connections.get(packedConnectionPos);
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
            this.lavaFillers.add(PackedBlockPos.pack(pos));
        }
        else if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            int worldLevel = IFlowBlock.getFlowHeightFromState(state);
            LavaCell target = this.getCell(pos, false);
            if(target.getLastVisibleLevel() != worldLevel)
            {           
                target.validate(this);
                this.setSaveDirty(true);
            }
        }
    }

    /** used by world update to notify when fillers are placed */
    public void trackLavaFiller(BlockPos pos)
    {
        this.lavaFillers.add(PackedBlockPos.pack(pos));
    }
    
    /** used by world update to notify when fillers are placed */
    public void trackCoolingBlock(BlockPos pos)
    {
        this.basaltBlocks.add(new AgedBlockPos(pos, this.tickIndex));
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
            target.validate(this);
        }
        this.setSaveDirty(true);
    }

    /**
     * Update simulation from world when a block next to a lava block is changed.
     * Does this by creating or validating (if already existing) cells for 
     * the notified block and all adjacent blocks.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public void notifyLavaNeighborChange(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        LavaCell center = this.getCell(pos, true);
        if(center != null)
        {
            this.getCell(PackedBlockPos.up(center.packedBlockPos), true);
            this.getCell(PackedBlockPos.down(center.packedBlockPos), true);
            this.getCell(PackedBlockPos.east(center.packedBlockPos), true);
            this.getCell(PackedBlockPos.west(center.packedBlockPos), true);
            this.getCell(PackedBlockPos.north(center.packedBlockPos), true);
            this.getCell(PackedBlockPos.south(center.packedBlockPos), true);
        }
        this.setSaveDirty(true);
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
        LavaCell target = this.getCellForLavaAddition(pos, shouldResynchToWorldBeforeAdding);
        if(target == null)
        {
            Adversity.log.info("Attept to place lava in a barrier block was ignored! Amount=" + amount + " @" + pos.toString());
        }
        else
        {
            target.changeLevel(this, amount);
        }
    }

    public void queueParticle(long packedBlockPos, int amount)
    {
//        Adversity.log.info("queueParticle amount=" + amount +" @"+ pos.toString());
        this.particles.addLavaForParticle(this, packedBlockPos, amount);
    }

    public void addConnection(long packedConnectionPos)
    {
        this.connections.createConnectionIfNotPresent(this, packedConnectionPos);
    }

    public void removeConnectionIfInvalid(long packedConnectionPos)
    {
        this.connections.removeIfInvalid(this, packedConnectionPos);
    }

    public static int blockUpdatesProvisionCounter;
    private static long blockUpdateProvisionTime;
    
    private static int blockUpdatesApplicationCounter;
    private static long blockUpdateApplicationTime;
    
    public void doBlockUpdates()
    {
        //        Adversity.log.info("LavaSim doBlockUpdates");
        long startTime = System.nanoTime();

        //TODO: make parallel a config option
        LAVA_THREAD_POOL.submit(() ->
            this.allCells.values().parallelStream().forEach(c -> c.provideBlockUpdateIfNeeded(this))).join();

        blockUpdateProvisionTime += (System.nanoTime() - startTime);
        
        startTime = System.nanoTime();
        this.itMe = true;
        
        blockUpdatesApplicationCounter += this.worldBuffer.applyBlockUpdates(1, this);;
        this.itMe = false;
        blockUpdateApplicationTime += (System.nanoTime() - startTime);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        allCells.clear();
        lavaFillers.clear();
        basaltBlocks.clear();
        connections.clear();
        
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
                LavaCell cell = new LavaCell(this, (saveData[i++] & 0xFFFFFFFFL) | ((long)saveData[i++] << 32), saveData[i++]);
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

                cell.setInteriorFloor((byte) (saveData[i] & 0xF));
                cell.setRawRetainedLevel(saveData[i] >> 4);
                i++;
                
                cell.clearBlockUpdate(this);
                
                if(Adversity.DEBUG_MODE)
                {
                    if(this.allCells.put(cell.packedBlockPos, cell) != null)
                        Adversity.log.info("Duplicate cell position on NBT load");
                }
                else
                {
                    this.allCells.put(cell.packedBlockPos, cell);
                }
            }
            
            // wait until all cells are added to collection, otherwise may recreate them all from the world recusively
            // Careful here: allCells is concurrent, so have to iterate a snapshot of it or will iterate through 
            // non-lava cells added by connections and try to make them into lava cells.
            for(LavaCell cell : this.allCells.values().toArray(new LavaCell[0]))
            {
                cell.updateFluidStatus(this);
                cell.validate(this);
            }
            
            Adversity.log.info("Loaded " + allCells.size() + " lava cells.");
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
                this.lavaFillers.add((saveData[i++] & 0xFFFFFFFFL) | ((long)saveData[i++] << 32));
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
            
            Collection<LavaCell> saveList = this.allCells.values().parallelStream().filter(c -> c.getFluidAmount() > 0).collect(Collectors.toList());
            
            Adversity.log.info("Saving " + saveList.size() + " lava cells.");
            int[] saveData = new int[saveList.size() * LAVA_CELL_NBT_WIDTH];
            int i = 0;
    
            for(LavaCell cell: saveList)
            {
                saveData[i++] = (int)(cell.packedBlockPos & 0xFFFFFFFFL);
                saveData[i++] = (int)(cell.packedBlockPos >> 32);
                saveData[i++] = cell.getFluidAmount();
                saveData[i++] = cell.getNeverCools() ? Integer.MAX_VALUE : cell.getLastFlowTick();
                saveData[i++] = (cell.getInteriorFloor() & 0xF) | (cell.getRawRetainedLevel(this) << 4);
            }         
            nbt.setIntArray(LAVA_CELL_NBT_TAG, saveData);
        }
        
        // SAVE FILLER CELLS
        {
            Adversity.log.info("Saving " + lavaFillers.size() + " filler cells.");
            int[] saveData = new int[lavaFillers.size() * LAVA_FILLER_NBT_WIDTH];
            int i = 0;
            for(long packedPos: lavaFillers)
            {
                saveData[i++] = (int) (packedPos & 0xFFFFFFFFL);
                saveData[i++] = (int) (packedPos >> 32);
            }       
            nbt.setIntArray(LAVA_FILLER_NBT_TAG, saveData);
        }
        
        // SAVE BASALT BLOCKS
        {
            Adversity.log.info("Saving " + basaltBlocks.size() + " cooling basalt blocks.");
            int[] saveData = new int[basaltBlocks.size() * BASALT_BLOCKS_NBT_WIDTH];
            int i = 0;
            for(AgedBlockPos apos: basaltBlocks)
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
        return Math.max((float)this.connections.size() / 20000F, (float)this.allCells.size() / 10000F);
    }


}