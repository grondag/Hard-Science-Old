package grondag.adversity.feature.volcano.lava;


import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.ParticleManager.ParticleInfo;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
public class LavaSimulator extends AbstractLavaSimulator
{

    private final ConcurrentHashMap<Long, LavaCell> allCells = new ConcurrentHashMap<Long, LavaCell>(16000, 0.6F, 8);
    private final static String LAVA_CELL_NBT_TAG = "lavacells";
    private static final int LAVA_CELL_NBT_WIDTH = 5;
    
    private final ConnectionMap connections = new ConnectionMap();
  
    public final ParticleManager particles = new ParticleManager();
    
    public LavaSimulator(World world)
    {
        super(world);
    }

    @Override
    protected void doLavaCooling()
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


    @Override
    protected void doFirstStep()
    {
        final int size = this.connections.size();
        LavaCellConnection[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doFirstStep(this);
        }
    }
    
    @Override 
    protected void doParticles()
    {
        //TODO: make particle limit configurable
        int capacity =  10 - EntityLavaParticle.getLiveParticleCount(this.worldBuffer.realWorld.getMinecraftServer());
        
        if(capacity <= 0) return;
        
        Collection<ParticleInfo> particles = this.particles.pollEligible(this, capacity);
        
        if(particles != null && !particles.isEmpty())
        {
            for(ParticleInfo p : particles)
            {
            
                // abort on strangeness
                if(this.isBlockLavaBarrier(p.packedBlockPos)) continue;
                
                if(this.isHighEnoughForParticle(p.packedBlockPos))
                {
                    // Spawn in world, discarding particles that have aged out and aren't big enough to form a visible lava block
                    if(p.getFluidUnits() >= AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL)
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
                else 
                {
                    this.addLava(p.packedBlockPos, p.getFluidUnits(), false);
                }
            }
        }
    }
    
    
    public void queueParticle(long packedBlockPos, int amount)
    {
//        Adversity.log.info("queueParticle amount=" + amount +" @"+ pos.toString());
        this.particles.addLavaForParticle(this, packedBlockPos, amount);
    }
    
    @Override
    protected void doStep()
    {
        final int size = this.connections.size();
        LavaCellConnection[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doStep(this);
        }
    }
    
    @Override
    protected void doLastStep()
    {
        final int size = this.connections.size();
        LavaCellConnection[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doStep(this);
//            values[i].updateSortKey();
        }
    }

    @Override
    protected void updateCells()
    {
        this.allCells.values().parallelStream().forEach(c -> c.updateFluidStatus(this));
    }
    
    @Override
    protected void doCellValidation()
    {
        this.allCells.values().parallelStream().forEach(c -> c.validate(this));
        this.cleanCellCache();
    }
    
    @Override
    protected void doConnectionValidation()
    {
        connections.validateConnections(this);
    }

    @Override
    protected void doBlockUpdateProvision()
    {
        //TODO: make parallel a config option
        LAVA_THREAD_POOL.submit(() ->
            this.allCells.values().parallelStream().forEach(c -> c.provideBlockUpdateIfNeeded(this))).join();
    }
    
    @Override
    protected void doBlockUpdateApplication()
    {
        this.itMe = true;
        this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;
    }
    
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
            
            while(candidate.getDistanceToFlowFloor() > AbstractLavaSimulator.LEVELS_PER_BLOCK && candidate.getFluidAmount() == 0)
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
    
    @Override
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        
        if(itMe) return;

        this.worldBuffer.isMCWorldAccessAppropriate = true;
        
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
        
        this.worldBuffer.isMCWorldAccessAppropriate = false;
    }

    @Override
    public void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;
        
        this.worldBuffer.isMCWorldAccessAppropriate = true;

        LavaCell target = this.getCell(pos, false);
        if(target.getFluidAmount() > 0)
        {
            target.validate(this);
        }
        this.setSaveDirty(true);
        
        this.worldBuffer.isMCWorldAccessAppropriate = false;

    }

    @Override
    public void notifyLavaNeighborChange(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        this.worldBuffer.isMCWorldAccessAppropriate = true;

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
        
        this.worldBuffer.isMCWorldAccessAppropriate = false;

    }

    @Override
    public void addLava(long packedBlockPos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
        this.worldBuffer.isMCWorldAccessAppropriate = true;

//        Adversity.log.info("addLava amount=" + amount + " @" + pos.toString());
        LavaCell target = this.getCellForLavaAddition(packedBlockPos, shouldResynchToWorldBeforeAdding);
        if(target == null)
        {
            Adversity.log.info("Attept to place lava in a barrier block was ignored! Amount=" + amount + " @" + PackedBlockPos.unpack(packedBlockPos).toString());
        }
        else
        {
            target.changeLevel(this, amount);
        }
        
        this.worldBuffer.isMCWorldAccessAppropriate = false;

    }

    protected void addConnection(long packedConnectionPos)
    {
        this.connections.createConnectionIfNotPresent(this, packedConnectionPos);
    }

    protected void removeConnectionIfInvalid(long packedConnectionPos)
    {
        this.connections.removeIfInvalid(this, packedConnectionPos);
    }
    
    @Override
    public void saveLavaNBT(NBTTagCompound nbt)
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
        
        this.particles.writeToNBT(nbt);
    }
    
    @Override
    public void readLavaNBT(NBTTagCompound nbt)
    {
        allCells.clear();
        connections.clear();

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
        
        this.particles.readFromNBT(nbt);
    }


    @Override
    public float loadFactor()
    {
        return Math.max((float)this.connections.size() / 20000F, (float)this.allCells.size() / 10000F);
    }


    @Override
    public int getCellCount()
    {
        return this.allCells.size();
    }


    @Override
    public int getConnectionCount()
    {
        return this.connections.size();
    }

//    @Override
    protected boolean isBlockLavaBarrier(long packedBlockPos)
    {
        LavaCell cell = this.getCell(packedBlockPos, false);
        return cell.isBarrier();
    }

//    @Override
    protected boolean isHighEnoughForParticle(long packedBlockPos)
    {
        LavaCell cell = this.getCell(packedBlockPos, false);
        return (cell.getDistanceToFlowFloor() == LavaCell.FLOW_FLOOR_DISTANCE_REALLY_FAR);
    }


}