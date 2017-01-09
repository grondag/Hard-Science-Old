package grondag.adversity.feature.volcano.lava;


import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * FIX/TEST
 * Filler block placement - some being missed?
 * Handle lastFlowTick overrun
 * 
 * FEATURES
 * Handle flowing terrain
 *      Update Drop Calculation
 * Reintegrate with volcano
 * Particle damage to entities
 *
 * Handle multiple worlds
 * Handle unloaded chunks
 *
 * Performance / parallelism
 *      Integer vs. FP quantization (trial)
 *      Better/faster connection sorting

 * Code Cleanup
 * Sounds
 * Missing top faces on some flow blocks - easier to tackle this after cooling in place - too transient to catch now
 * Particle model/rendering polish
 * Lava texture needs more character, more reddish?
 */
public class LavaSimulator extends SimulationNode
{
    protected final World world;
    protected final LavaTerrainHelper terrainHelper;

    private int totalFluidRegistered = 0;
    
    protected final HashMap<BlockPos, LavaCell> allCells = new HashMap<BlockPos, LavaCell>();
    
    private final HashMap<BlockPos, LavaCell> cellsWithFluid = new HashMap<BlockPos, LavaCell>();
    
    private final ConnectionMap connections = new ConnectionMap();
    
    private final LinkedList<CellConnectionPos> newConnections = new LinkedList<CellConnectionPos>();
    
    /** Set true when doing block placements so we known not to register them as newly placed lava. */
    private boolean itMe = false;
    
    /** prevent synchoniziation with existing world fluid during loading */
    protected boolean isLoading = false;
    
    private int tickIndex = 0;
    
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
    
    
    public void doTick()
    {
        this.tickIndex++;
        
        this.doCooling();
        
        if((this.tickIndex & 0xFF) == 0xFF)
        {
            Adversity.log.info("Particle time this sample = " + particleTime / 1000000);
            particleTime = 0;
            
            Adversity.log.info("Validation time this sample = " + validationTime / 1000000);
            validationTime = 0;
            
            Adversity.log.info("Block update time this sample = " + blockUpdateTime / 1000000);
            blockUpdateTime = 0;
            
            Adversity.log.info("Cache cleanup time this sample = " + cacheCleanTime / 1000000);
            cacheCleanTime = 0;
            
            Adversity.log.info("Step time this sample = " + stepTime / 1000000);
            stepTime = 0;
            
            Adversity.log.info("New connection proccessing time this sample = " + connectionRequestTime / 1000000 
                    + " for " + connectionRequestCount + "requests @" + ((connectionRequestCount > 0) ? connectionRequestTime / connectionRequestCount : "") + " each");
            connectionRequestCount = 0;
            connectionRequestTime = 0;
            
            Adversity.log.info("Connection sort proccessing time this sample = " + sortTime / 1000000 
                    + " for " + sortCount + "requests @" + ((sortCount > 0) ? sortTime / sortCount : "") + " each");
            sortCount = 0;
            sortTime = 0;
            
            Adversity.log.info("Connection flow proccessing time this sample = " + connectionProcessTime / 1000000 
                    + " for " + connectionProcessCount + "requests @" + ((connectionProcessCount > 0) ? connectionProcessTime / connectionProcessCount : "") + " each");
            connectionProcessCount = 0;
            connectionProcessTime = 0;
        }
        
        long startTime = System.nanoTime();
        if(--ticksUntilNextValidation == 0)
        {
//            Adversity.log.info("LavaSim doStep validatingAllCells, cell count=" + this.allCells.size() );
            validateAllCells();
            ticksUntilNextValidation = VALIDATION_TICKS;
        }
        this.validationTime += (System.nanoTime() - startTime);
        
//        if(cellsWithFluid.size() > 0)
//        {
//            Adversity.log.info("LavaSim doStep, cell count=" + cellsWithFluid.size() );
//        }
      
        //Was causing slow propagation and overflow not doing this each step just prior to flow
//        this.connections.values().parallelStream().forEach((LavaCellConnection c) -> {c.updateFlowRate(this);;});
        
        startTime = System.nanoTime();
        
        this.doStep();
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
        
        startTime = System.nanoTime();
        if(--ticksUntilCacheCleanup == 0)
        {
//            Adversity.log.info("LavaSim doStep cleanCellCache, cell starting count=" + this.allCells.size() );
            cleanCellCache();
            ticksUntilCacheCleanup = CACHE_TICKS;
            Adversity.log.info("LavaSim doStep cleanCellCache, cell ending count=" + this.allCells.size() );
            
//            this.allCells.values().forEach(cell -> {Adversity.log.info("hash=" + cell.hashCode() + "pos=" + cell.pos.toString() + " level=" + cell.getCurrentLevel());});
//            this.connections.values().forEach(connection -> {Adversity.log.info("firstHash=" + connection.firstCell.hashCode() + " firstPos=" + connection.firstCell.pos.toString() + " firstLevel=" + connection.firstCell.getCurrentLevel() + "secondHash=" + connection.secondCell.hashCode() + " secondPos=" + connection.secondCell.pos.toString() + " secondLevel=" + connection.secondCell.getCurrentLevel());});

            int totalFluid = 0;
            for(LavaCell cell : cellsWithFluid.values())
            {
                totalFluid += cell.getFluidAmount();
            }
            Adversity.log.info("Total fluid in cells = " + totalFluid / LavaCell.FLUID_UNITS_PER_BLOCK + "  Total registered fluid =" + totalFluidRegistered / LavaCell.FLUID_UNITS_PER_BLOCK);
        }
        
        this.cacheCleanTime += (System.nanoTime() - startTime);

    }
    
    private int particleCounter = 10;
    
    private long particleTime;
    private void doParticles()
    {
        long startTime = System.nanoTime();
        if((this.tickIndex & 0xFF) == 0xFF)
        
        if(particleCounter-- == 0)
        {
            particleCounter = 5 + Useful.SALT_SHAKER.nextInt(5);
            
            //TODO: sort bottom up
            for(LavaCell cell : cellsWithFluid.values().toArray(new LavaCell[0]))
            {
                int amount = cell.getFluidAmount();
                if(amount > 0 && cell.isDrop(this))
                {
                    world.spawnEntityInWorld(new EntityLavaParticle(world, amount, 
                        new Vec3d(cell.pos.getX() + 0.5, cell.pos.getY() - 0.1, cell.pos.getZ() + 0.5), Vec3d.ZERO));
                        cell.changeLevel(this, -amount);
                }
            }
        }
    
        this.particleTime += (System.nanoTime() - startTime);
    }
    
    private int coolingCounter = 21;
    
    private LinkedList<LavaCell> coolingCells = new LinkedList<LavaCell>();
    
    private long coolingTime;
    
    private void doCooling()
    {
        long startTime = System.nanoTime();
        if((this.tickIndex & 0xFF) == 0xFF)
        {
            Adversity.log.info("Cooling time this sample = " + coolingTime / 1000000);
            coolingTime = 0;
        }
     
        coolingCounter--;        
        
        if(!coolingCells.isEmpty())
        {
            //max shouldn't be needed b/c will be empty b4 we get to zero, but safer
            int chance = 1 + coolingCells.size() / 20;
            
            int count = Math.min(coolingCells.size(), chance - Useful.SALT_SHAKER.nextInt(20));
            
            if(count > 0)
            {
                for(int i = 0; i < count; i++)
                {
                    LavaCell cell = coolingCells.removeFirst();
                    int amount = cell.getFluidAmount();
                    if(amount > 0 && this.getTickIndex() - cell.getLastFlowTick() > 200)
                    {
                        boolean hotNeighborFound = false;
                        for(EnumFacing face : EnumFacing.VALUES)
                        {
                            LavaCell neighbor = this.cellsWithFluid.get(cell.pos.add(face.getDirectionVec()));
                            if(neighbor != null && neighbor.getFluidAmount() > 0 && this.getTickIndex() - neighbor.getLastFlowTick() < 200)
                            {
                                hotNeighborFound = true;
                                break;
                            }
                        }
                        if(!hotNeighborFound)
                        {
                            int visibleLevel = cell.getVisibleLevel();
                            cell.changeLevel(this, -amount);
                            cell.clearBlockUpdate();
                            if(visibleLevel > 0)
                            {
                                coolLava(cell.pos.up(2), true);
                                coolLava(cell.pos.up(), true);
                                coolLava(cell.pos, false);
                                cell.validate(this, true);
                            }
                        }
                    }
                }
            }
        }
        else if(coolingCounter <= 0)
        {
            coolingCounter = 21;
            
            LavaCell[] cells = cellsWithFluid.values().toArray(new LavaCell[0]);
            Arrays.sort(cells, new Comparator<LavaCell>() 
            {
                @Override
                public int compare(LavaCell o1, LavaCell o2)
                {
                    return Integer.compare(o1.getLastFlowTick(), o2.getLastFlowTick());
                }
            });
            
            for( LavaCell cell : cells)
            {
                if(this.getTickIndex() - cell.getLastFlowTick() > 200)
                    coolingCells.add(cell);
                else
                    break;
            }
        }
     
        this.coolingTime += (System.nanoTime() - startTime);
    }
    
    private void coolLava(BlockPos pos, boolean fillerOnly)
    {
        IBlockState state = this.world.getBlockState(pos);
        Block currentBlock = state.getBlock();
        NiceBlock newBlock = null;
        if(currentBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            newBlock = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
        }
        else if(currentBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK && !fillerOnly)
        {
            newBlock = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK;
        }
        
        if(newBlock != null)
        {
            this.itMe = true;
            this.world.setBlockState(pos, newBlock.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
            this.itMe = false;
        }
    }
    
    private long stepTime;
    private long connectionRequestTime;
    private int connectionRequestCount;
    private long sortTime;
    private int sortCount;
    private long connectionProcessTime;
    private int connectionProcessCount;
    
    public void doStep()
    {

        long startTime = System.nanoTime();
        connectionRequestCount += newConnections.size();
        this.processNewConnectionRequests();
        this.connectionRequestTime += (System.nanoTime() - startTime);
        
        if(this.connections.size() > 0)
        {
            startTime = System.nanoTime();
            sortCount += this.connections.size();
            
            LavaCellConnection[] links = this.connections.values().toArray(new LavaCellConnection[0]);
            Arrays.sort(links, new Comparator<LavaCellConnection>() {
                @Override
                public int compare(LavaCellConnection o1, LavaCellConnection o2)
                {
                    return Integer.compare(o1.getSortKey(), o2.getSortKey());
                }});
            
            this.sortTime += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            connectionProcessCount += links.length;
            
            for(LavaCellConnection c : links)
            {
                if(c.firstCell.isBarrier() || c.secondCell.isBarrier()
                      || (c.firstCell.getFluidAmount() == 0 && c.secondCell.getFluidAmount() == 0))
                {
                    c.releaseCells();
                    this.connections.remove(new CellConnectionPos(c.firstCell.pos, c.secondCell.pos));
                }
                else
                {
                    c.doStep(this);
                }
            }
            
            this.connectionProcessTime += (System.nanoTime() - startTime);
            //use iterator and hold changes in other collections
//            Iterator<Entry<CellConnectionPos, LavaCellConnection>> it = this.connections.entrySet().iterator();
//            while(it.hasNext())
//            {
//                Entry<CellConnectionPos, LavaCellConnection> next = it.next();
//                LavaCellConnection connection = next.getValue();
//                //remove connections to barriers or between cells with no fluid
//                if(connection.firstCell.isBarrier() || connection.secondCell.isBarrier()
//                        || (connection.firstCell.getCurrentLevel() == 0 && connection.secondCell.getCurrentLevel() == 0))
//                {
//                    connection.releaseCells();
//                    it.remove();
//                }
//                else
//                {
//                    connection.doStep(this);
//                }
//            }

        }
        
        //TODO: handle particles with connection-oriented model

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
    
    /**
     * Retrieves existing cell or creates new if not found.
     * For existing cells, will validate vs. world if validateExisting = true;
     */
    public LavaCell getCell(BlockPos pos, boolean validateExisting)
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
        else if(validateExisting)
        {
            result.validate(this, true);
        }
        return result;
    }
    
    public LavaCellConnection getConnection(BlockPos pos1, BlockPos pos2)
    {
        return connections.get(new CellConnectionPos(pos1, pos2));
    }
    
   
    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if we are currently placing blocks.
     */
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;
        
        int worldLevel = IFlowBlock.getFlowHeightFromState(state);
        LavaCell target = this.getCell(pos, false);
        if(target.getVisibleLevel() != worldLevel)
        {           
            totalFluidRegistered += worldLevel * LavaCell.FLUID_UNITS_PER_LEVEL;
            target.validate(this, true);
        }
        this.setSaveDirty(true);
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
        totalFluidRegistered -= target.getFluidAmount();
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
            if(!cellsWithFluid.containsKey(cell.pos))
            {
                cell.retain("updateFluidStatus self");
                this.cellsWithFluid.put(cell.pos, cell);
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    LavaCell other = cell.getNeighbor(this, face);
                    other.retain("updateFluidStatus " + face.toString() + " from " + this.hashCode());
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
                cell.release("updateFluidStatus self");
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    LavaCell other = cell.getNeighbor(this, face);
                    other.release("updateFluidStatus " + face.toString() + " from " + this.hashCode());
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
    public void addLava(BlockPos pos, int amount)
    {
//        Adversity.log.info("addLava amount=" + amount + " @" + pos.toString());
        
        int available = amount;
        
        LavaCell target = this.getCell(pos.down(), true);
        int capacity = target.getCapacity();
        int flow = Math.min(capacity, available);
        if(flow > 0)
        {
            target.changeLevel(this, flow);
            available -= flow;
        }
        
        if(available > 0)
        {
            target = this.getCell(pos, true);
            capacity = target.getCapacity();
            flow = Math.min(capacity, available);
            if(flow > 0)
            {
                target.changeLevel(this, flow);
                available -= flow;
            }
            
            if(available > 0)
            {
                target = this.getCell(pos.up(), true);
                capacity = target.getCapacity();
                flow = Math.min(capacity, available);
                if(flow > 0)
                {
                    target.changeLevel(this, flow);
                    available -= flow;
                }
            }
            
            if(available > 0)
            {
                Adversity.log.info("LAVA EATING IS A THING! Amount=" + available + " @" + pos.toString());
            }
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
                LavaCell cell1 = this.getCell(pos.getLowerPos(), false);
                LavaCell cell2 = this.getCell(pos.getUpperPos(), false);
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

    private static int blockUpdatesCounter;
    private static int updateReportCounter = 60;
    
    private long blockUpdateTime;
    public void doBlockUpdates()
    {
//        Adversity.log.info("LavaSim doBlockUpdates");
        long startTime = System.nanoTime();
        
        Queue<LavaBlockUpdate> blockUpdates = getBlockUpdates();
        
        blockUpdatesCounter += blockUpdates.size();
        
        if(updateReportCounter <= 0)
        {
            Adversity.log.info("Lava expAvg block updates per tick = " + blockUpdatesCounter/60);
            updateReportCounter = 60;
            blockUpdatesCounter = 0;
        }
        else
        {
            updateReportCounter--;
        }
        
        this.itMe = true;
        LavaBlockUpdate update = blockUpdates.poll();       
        while(update != null)
        {   if(update.level == 0)
            {
                if(world.getBlockState(update.pos).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    world.setBlockToAir(update.pos);
                }
            }
            else
            {
                this.world.setBlockState(update.pos, IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), update.level));
            }
            this.adjustmentList.add(update.pos);
            update = blockUpdates.poll();
        }
        this.itMe = false;
        
        //doBlockUpdates();
        
        doAdjustments();

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
                    IFlowBlock.adjustFillIfNeeded(this.world, p);
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
                || block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK )
        {

            //            Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
            //                    + worldObj.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
            //                    + NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getRegistryName() + " @ " + targetPos.toString());


            this.world.setBlockState(targetPos, NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getDefaultState()
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
        cellsWithFluid.clear();
        connections.clear();
        newConnections.clear();
        adjustmentList.clear();
        dirtyCells.clear();
        blockUpdates.clear();
        totalFluidRegistered = 0;
        this.tickIndex = 0;
        
        int[] saveData = nbt.getIntArray(TAG_SAVE_DATA);
        
        //confirm correct size
        if(saveData == null || saveData.length % 6 != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Lava blocks may not be updated properly.");
            return;
        }
        
        int i = 0;
        this.isLoading = true;
        while(i < saveData.length)
        {
            LavaCell cell = this.getCell(new BlockPos(saveData[i++], saveData[i++], saveData[i++]), false);
          
            cell.changeLevel(this, saveData[i++] - cell.getFluidAmount());
            cell.setLastFlowTick(saveData[i++]);
            cell.setFloor(saveData[i++]);
            this.tickIndex = Math.max(this.tickIndex, cell.getLastFlowTick());
            this.totalFluidRegistered += cell.getFluidAmount();
            
        }
        this.isLoading = false;
  
        Adversity.log.info("Loaded " + cellsWithFluid.size() + " lava cells.");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        Adversity.log.info("Saving " + cellsWithFluid.size() + " lava cells.");
        int[] saveData = new int[cellsWithFluid.size() * 6];
        int i = 0;
        
        for(LavaCell cell: cellsWithFluid.values())
        {
            saveData[i++] = cell.pos.getX();
            saveData[i++] = cell.pos.getY();
            saveData[i++] = cell.pos.getZ();
            saveData[i++] = cell.getFluidAmount();
            saveData[i++] = cell.getLastFlowTick();
            saveData[i++] = cell.getFloor();
        }       
        
        nbt.setIntArray(TAG_SAVE_DATA, saveData);
        
    }
    
    public int getTickIndex()
    {
        return this.tickIndex;
    }


    @Override
    public boolean isSaveDirty()
    {
        return super.isSaveDirty();
    }
    
    
}