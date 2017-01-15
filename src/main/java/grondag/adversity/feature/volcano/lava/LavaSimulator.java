package grondag.adversity.feature.volcano.lava;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import grondag.adversity.Adversity;
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
 * FIX/TEST
 * Reduce latency of significant block updates while still throttling overall block update rate
 * Preserve particle queue
 * Handle lastFlowTick overrun (maybe reposition on reload)
 * Performance / parallelism
 *  Handle changes to sort keys on connections due to world updates
 * Negative cell levels
 * 
 * FEATURES
 * Improve Drop/slope Calculation for flowing terrain
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

    private int totalFluidRegistered = 0;

    protected final HashMap<BlockPos, LavaCell> allCells = new HashMap<BlockPos, LavaCell>();

    private final HashMap<BlockPos, LavaCell> lavaCells = new HashMap<BlockPos, LavaCell>();
    private final static String LAVA_CELL_NBT_TAG = "lavacells";
    private static final int LAVA_CELL_NBT_WIDTH = 6;
    
    /** Filler cells that need to be cooled but aren't involved in the fluid simulation. */
    private final HashSet<BlockPos> fillerCells = new HashSet<BlockPos>();
    private final static String FILLER_CELL_NBT_TAG = "fillercells"; 
    private static final int FILLER_CELL_NBT_WIDTH = 3;

    private final ConnectionMap connections = new ConnectionMap();
    
    private final ParticleManager particles = new ParticleManager();

    private final HashSet<CellConnectionPos> newConnections = new HashSet<CellConnectionPos>();
    private final HashSet<CellConnectionPos> deadConnections = new HashSet<CellConnectionPos>();
    
    /** Set true when doing block placements so we known not to register them as newly placed lava. */
    private boolean itMe = false;

    /** prevent synchoniziation with existing world fluid during loading */
    protected boolean isLoading = false;

    private int tickIndex = 0;

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


    public void doTick()
    {
        this.tickIndex++;

        this.doCooling();

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

            Adversity.log.info("New connection proccessing time this sample = " + connectionRequestTime / 1000000 
                    + " for " + connectionRequestCount + "requests @" + ((connectionRequestCount > 0) ? connectionRequestTime / connectionRequestCount : "") + " each");
            connectionRequestCount = 0;
            connectionRequestTime = 0;

            Adversity.log.info("Connection flow proccessing time this sample = " + connectionProcessTime / 1000000 
                    + " for " + connectionProcessCount + "requests @" + ((connectionProcessCount > 0) ? connectionProcessTime / connectionProcessCount : "") + " each");
            connectionProcessCount = 0;
            connectionProcessTime = 0;
            
            Adversity.log.info("Connection removal time this sample = " + connectionRemovalTime / 1000000 
                    + " for " + connectionRemovalCount + "requests @" + ((connectionRemovalCount > 0) ? connectionRemovalTime / connectionRemovalCount : "") + " each");
            connectionRemovalCount = 0;
            connectionRemovalTime = 0;
            
            Adversity.log.info("total cell count=" + this.allCells.size() );
            int totalFluid = 0;
            for(LavaCell cell : lavaCells.values())
            {
                totalFluid += cell.getFluidAmount();
            }
            Adversity.log.info("Total fluid in cells = " + (float)totalFluid / LavaCell.FLUID_UNITS_PER_BLOCK + "  Total registered fluid =" + (float)totalFluidRegistered / LavaCell.FLUID_UNITS_PER_BLOCK);
            

        }

              //        if(cellsWithFluid.size() > 0)
        //        {
        //            Adversity.log.info("LavaSim doStep, cell count=" + cellsWithFluid.size() );
        //        }

        //Was causing slow propagation and overflow not doing this each step just prior to flow
        //        this.connections.values().parallelStream().forEach((LavaCellConnection c) -> {c.updateFlowRate(this);;});

        long startTime = System.nanoTime();

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
        
        if((this.tickIndex & 0x1F) == 0x1F)
        {
            
            startTime = System.nanoTime();
            //            Adversity.log.info("LavaSim doStep validatingAllCells, cell count=" + this.allCells.size() );
            validateAllCells();
            this.validationTime += (System.nanoTime() - startTime);

            startTime = System.nanoTime();
    
            //            Adversity.log.info("LavaSim doStep cleanCellCache, cell starting count=" + this.allCells.size() );
            cleanCellCache();
       
        }

        this.cacheCleanTime += (System.nanoTime() - startTime);

    }

//    private int particleCounter = 10;

    private long particleTime;
    private void doParticles()
    {
        long startTime = System.nanoTime();
       //TODO: make configurable
        int capacity =  10 - EntityLavaParticle.getLiveParticleCount();
        
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
    private static final int COOLING_SCANS_PER_TICK = 4;
    private void doCooling()
    {
        long startTime = System.nanoTime();

        if(scanningBlocks.isEmpty())
        {
            if(coolingBlocks.isEmpty())
            {
                scanningBlocks.addAll(lavaCells.keySet());
                scanningBlocks.addAll(this.fillerCells);
            }
            else
            {
                final BlockPos target = coolingBlocks.removeFirst();
                
                if(canLavaCool(target))
                {
                    LavaCell cell = this.getFluidCell(target);
                    if(cell == null || cell.canCool(this))
                    {
                        coolLava(target);
                        
                        if(cell != null)
                        {
                            cell.changeLevel(this, -cell.getFluidAmount());
                            cell.clearBlockUpdate();
                            cell.validate(this, true);
                        }
                    }
                }
            }
        }
        else
        {
            int attempts = 0;
            while(attempts++ < COOLING_SCANS_PER_TICK && !scanningBlocks.isEmpty())
            {
                BlockPos target = scanningBlocks.removeFirst();
                
                if(canLavaCool(target))
                {
                    coolingBlocks.add(target);
                }
            }
        }
    
        this.coolingTime += (System.nanoTime() - startTime);
    }

    /**
     * Returns true if lava can cool based on world state alone. Does not consider 
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
        IBlockState state = this.worldBuffer.getBlockState(pos);
        Block currentBlock = state.getBlock();
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
            this.worldBuffer.setBlockState(pos, newBlock.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
//            this.itMe = false;
        }
    }

    private long stepTime;
    private long connectionRequestTime;
    private int connectionRequestCount;
    private long connectionProcessTime;
    private int connectionProcessCount;
    private long connectionRemovalTime;
    private int connectionRemovalCount;
    
    public void doStep()
    {

        long startTime = System.nanoTime();
        connectionRequestCount += newConnections.size();
        this.processNewConnectionRequests();
        this.connectionRequestTime += (System.nanoTime() - startTime);

        if(this.connections.size() > 0)
        {

            startTime = System.nanoTime();

            connectionProcessCount += this.connections.size();

            for(LavaCellConnection c : this.connections.getSortedValues())
            {
                if(c.firstCell.isBarrier() || c.secondCell.isBarrier()
                        || (c.firstCell.getFluidAmount() == 0 && c.secondCell.getFluidAmount() == 0))
                {
                    this.deadConnections.add(new CellConnectionPos(c.firstCell.pos, c.secondCell.pos));
                }
                else
                {
                    c.doStep(this);
                }
            }

            this.connectionProcessTime += (System.nanoTime() - startTime);


            startTime = System.nanoTime();
            connectionRemovalCount += deadConnections.size();
            this.processDeadConnections();
            this.connectionRemovalTime += (System.nanoTime() - startTime);


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
    public LavaCell getFluidCell(BlockPos pos)
    {
        return this.lavaCells.get(pos);
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
            if(!lavaCells.containsKey(cell.pos))
            {
                cell.retain("updateFluidStatus self");
                this.lavaCells.put(cell.pos, cell);
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
            if(lavaCells.containsKey(cell.pos))
            {
                lavaCells.remove(cell.pos);
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
     * Should only force resynch with world when you know you removed a barrier and simulation
     * needs to know the cell is now open.  Otherwise if this addition is occurs
     * after an earlier one but before block update resynch will cause earlier addition to be lost.
     */
    public void addLava(BlockPos pos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
        Adversity.log.info("addLava amount=" + amount + " @" + pos.toString());

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

    public void queueParticle(BlockPos pos, int amount)
    {
        Adversity.log.info("queueParticle amount=" + amount +" @"+ pos.toString());

        this.particles.addLavaForParticle(this, pos, amount);
    }

    public void requestNewConnection(BlockPos pos1, BlockPos pos2)
    {
        this.newConnections.add(new CellConnectionPos(pos1, pos2));
    }



    private void processNewConnectionRequests()
    {
        if(!this.newConnections.isEmpty())
        {
            for(CellConnectionPos pos : this.newConnections)
            {
                this.connections.createConnectionIfNotPresent(this, pos);
            }
            this.newConnections.clear();
        }
    }

    private void processDeadConnections()
    {
        if(!deadConnections.isEmpty())
        {
            for(CellConnectionPos pos : this.deadConnections)
            {
                this.connections.remove(pos);
            }
            this.deadConnections.clear();
        }
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
                    List<Pair<BlockPos, IBlockState>> updates = IFlowBlock.adjustFillIfNeeded(this.worldBuffer, p);
                    
                    // update set of filler blocks that need cooling
                    for(Pair<BlockPos, IBlockState> pair : updates)
                    {
                        if(pair.getRight().getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                        {
                            this.fillerCells.add(pair.getLeft());
                        }
                        else
                        {
                            this.fillerCells.remove(pair.getLeft());
                        }
                    }
                    this.worldBuffer.addUpdates(updates);
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

        IBlockState state = this.worldBuffer.getBlockState(targetPos);
        if(!(state.getBlock() instanceof NiceBlock)) return false;

        NiceBlock block = (NiceBlock)state.getBlock();

        if(!IFlowBlock.isFlowHeight(block)) return false;

        boolean isFullCube = IFlowBlock.shouldBeFullCube(state, this.worldBuffer, targetPos);


        if(isFullCube)
        {
            if(block == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
            {
                //                Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
                //                        + this.world.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
                //                        + NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getRegistryName() + " @ " + targetPos.toString());
                this.worldBuffer.setBlockState(targetPos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
            }
        }
        else if (block == NiceBlockRegistrar.COOL_STATIC_BASALT_HEIGHT_BLOCK 
                || block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK )
        {

            //            Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
            //                    + worldObj.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
            //                    + NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getRegistryName() + " @ " + targetPos.toString());


            this.worldBuffer.setBlockState(targetPos, NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getDefaultState()
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
        lavaCells.clear();
        fillerCells.clear();
        connections.clear();
        newConnections.clear();
        deadConnections.clear();
        adjustmentList.clear();
        dirtyCells.clear();
        totalFluidRegistered = 0;
        this.tickIndex = 0;
        
        this.isLoading = true;
        
        this.worldBuffer.readFromNBT(nbt);

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
                LavaCell cell = this.getCell(new BlockPos(saveData[i++], saveData[i++], saveData[i++]), false);
    
                cell.changeLevel(this, saveData[i++] - cell.getFluidAmount());
                cell.setLastFlowTick(saveData[i++]);
                cell.setFloor(saveData[i++]);
                if(!cell.getNeverCools())
                {
                    this.tickIndex = Math.max(this.tickIndex, cell.getLastFlowTick());
                }
                this.totalFluidRegistered += cell.getFluidAmount();
    
            }
            Adversity.log.info("Loaded " + lavaCells.size() + " lava cells.");
        }
        
        // LOAD FILLER CELLS
        saveData = nbt.getIntArray(FILLER_CELL_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % FILLER_CELL_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Filler blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                this.fillerCells.add(new BlockPos(saveData[i++], saveData[i++], saveData[i++]));
            }
            Adversity.log.info("Loaded " + fillerCells.size() + " filler cells.");
        }

        this.isLoading = false;

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        // SAVE LAVA CELLS
        Adversity.log.info("Saving " + lavaCells.size() + " lava cells.");
        int[] saveData = new int[lavaCells.size() * LAVA_CELL_NBT_WIDTH];
        int i = 0;

        for(LavaCell cell: lavaCells.values())
        {
            saveData[i++] = cell.pos.getX();
            saveData[i++] = cell.pos.getY();
            saveData[i++] = cell.pos.getZ();
            saveData[i++] = cell.getFluidAmount();
            saveData[i++] = cell.getLastFlowTick();
            saveData[i++] = cell.getFloor();
        }       

        nbt.setIntArray(LAVA_CELL_NBT_TAG, saveData);
        
        
        // SAVE FILLER CELLS
        Adversity.log.info("Saving " + lavaCells.size() + " lava cells.");
        saveData = new int[fillerCells.size() * FILLER_CELL_NBT_WIDTH];
        i = 0;

        for(BlockPos pos: fillerCells)
        {
            saveData[i++] = pos.getX();
            saveData[i++] = pos.getY();
            saveData[i++] = pos.getZ();
        }       

        nbt.setIntArray(FILLER_CELL_NBT_TAG, saveData);
        
        this.worldBuffer.writeToNBT(nbt);

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