package grondag.adversity.feature.volcano.lava;


import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.feature.volcano.lava.LavaCellConnection.BottomType;
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
    
    public static final ForkJoinPool LAVA_THREAD_POOL = new ForkJoinPool();

    private final ConcurrentHashMap<BlockPos, LavaCell> allCells = new ConcurrentHashMap<BlockPos, LavaCell>(16000, 0.6F, 8);
    private final static String LAVA_CELL_NBT_TAG = "lavacells";
    private static final int LAVA_CELL_NBT_WIDTH = 6;
    
    /** Filler lava blocks that need to be cooled with lava cells but aren't involved in the fluid simulation. */
    private final Set<BlockPos> lavaFillers = ConcurrentHashMap.newKeySet();
    private final static String LAVA_FILLER_NBT_TAG = "lavafillers"; 
    private static final int LAVA_FILLER_NBT_WIDTH = 3;
    
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
        this.doStep();

        this.stepTime += (System.nanoTime() - startTime);

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
        
        Collection<EntityLavaParticle> particles = this.particles.pollEligible(this, capacity);
        
        if(particles != null && !particles.isEmpty())
        {
            particles.stream().forEach(p ->  worldBuffer.realWorld.spawnEntityInWorld(p));
        }
      
        this.particleTime += (System.nanoTime() - startTime);
    }


    private long coolingTime;

    private void doLavaCooling()
    {
       LAVA_THREAD_POOL.submit( () ->
           this.allCells.values().parallelStream()
                .filter(c -> c.getFluidAmount() > 0 && canLavaCool(c.pos) && c.canCool(this))
                .collect(Collectors.toList())
                .parallelStream().forEach(c -> 
                {
                    coolLava(c.pos);
                    c.changeLevel(this, -c.getFluidAmount());
                    c.clearBlockUpdate();
                    c.validate(this, true);
                }));
       
       LAVA_THREAD_POOL.submit( () ->
               this.lavaFillers.parallelStream().forEach( p -> 
                    {
                        if(canLavaCool(p))
                        {
                            coolLava(p);
                            this.lavaFillers.remove(p);
                        }
                    }));
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
            };
        }));       
    }
    
    /**
     * Returns value to show if lava can cool based on world state alone. Does not consider age.
     */
    private boolean canLavaCool(BlockPos pos)
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
                    if(face == EnumFacing.DOWN) return false;
                    
                    hotNeighborCount++;
                }
            }
            
            return hotNeighborCount < 4;
        }
        else
        {
            return false;
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
    
    public void doFirstStep()
    {
        long startTime = System.nanoTime();
        connectionProcessCount += this.connections.size();
        this.connections.values().stream().forEach((LavaCellConnection c) -> c.doFirstStep(this));
        this.connectionProcessTime += (System.nanoTime() - startTime);
    }

    public void doStep()
    {
        long startTime = System.nanoTime();
        connectionProcessCount += this.connections.size();
        this.connections.values().stream().forEach((LavaCellConnection c) -> c.doStep(this));
        this.connectionProcessTime += (System.nanoTime() - startTime);
    }
    
    private long validationTime;
    private void validateAllCells()
    {
        this.allCells.values().parallelStream().forEach(c -> c.validate(this,false));
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
                    this.allCells.remove(c.pos);
                }
            }
        ));
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

    /**
     * Finds closest vertically aligned non-drop lava cell at location.
     * If location is a barrier will attempt cell above. If that cell is also a barrier, will return null.
     * If location is a drop, will go down until it finds a non-drop cell.
     */
    public LavaCell getCellForLavaAddition(BlockPos pos, boolean shouldResynchToWorldIfExists)
    {
        LavaCell candidate = this.getCell(pos, shouldResynchToWorldIfExists);
        if(candidate.isBarrier() && candidate.pos.getY() < 255)
        {
            candidate = candidate.getUpEfficiently(this, shouldResynchToWorldIfExists);
        }
        else
        {
            while(candidate.getBottomType() == BottomType.DROP)
            {
                candidate = candidate.getDownEfficiently(this, shouldResynchToWorldIfExists);
            }
        }
       return candidate.isBarrier() ? null : candidate;
    }
    
    /** returns a cell only if it contains fluid */
    public LavaCell getCellIfItExists(BlockPos pos)
    {
        return this.allCells.get(pos);
    }
    
    /** returns a cell only if it contains fluid */
    public LavaCell getFluidCellIfItExists(BlockPos pos)
    {
        LavaCell result = this.allCells.get(pos);
        if(result != null && result.getFluidAmount() > 0)
        {
            return result;
        }
        else
        {
            return null;
        }
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

    /** used by world update to notify when fillers are placed */
    public void trackLavaFiller(BlockPos pos)
    {
        this.lavaFillers.add(pos);
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
            target.validate(this, true);
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
            this.allCells.values().parallelStream().forEach(c -> c.provideBlockUpdateIfNeeded(this)));

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
                cell.updateFluidStatus(this, true);
                cell.updateRetainedLevel(this);
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
            
            Collection<LavaCell> saveList = this.allCells.values().parallelStream().filter(c -> c.getFluidAmount() > 0).collect(Collectors.toList());
            
            Adversity.log.info("Saving " + saveList.size() + " lava cells.");
            int[] saveData = new int[saveList.size() * LAVA_CELL_NBT_WIDTH];
            int i = 0;
    
            for(LavaCell cell: saveList)
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
        return Math.max((float)this.connections.size() / 30000F, (float)this.allCells.size() / 20000F);
    }


}