package grondag.adversity.feature.volcano.lava;


import java.util.List;

import grondag.adversity.feature.volcano.lava.cell.LavaCells;
import grondag.adversity.feature.volcano.lava.cell.builder.ColumnChunkBuffer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class LavaSimulatorNew extends AbstractLavaSimulator
{
    private final LavaConnections connections = new LavaConnections();
    public final CellChunkLoader cellChunkLoader = new CellChunkLoader();
    private final LavaCells cells = new LavaCells(this);
    
    /** incremented each step, multiple times per tick */
    private int stepIndex;
    
    public LavaSimulatorNew(World world)
    {
        super(world);
    }
    
    public int getStepIndex()
    {
        return this.stepIndex;
    }

    @Override
    public float loadFactor()
    {
        return Math.max((float)this.connections.size() / 10000F, (float)this.cells.size() / 5000F);
    }

    @Override
    public int getCellCount()
    {
        return this.cells.size();
    }

    @Override
    public int getConnectionCount()
    {
        return this.connections.size();
    }

    @Override
    public void saveLavaNBT(NBTTagCompound nbt)
    {
        cells.writeNBT(nbt);
    }

    @Override
    public void readLavaNBT(NBTTagCompound nbt)
    {
        cells.readNBT(this, nbt);
    }

    @Override
    public void addLava(long packedBlockPos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void notifyLavaNeighborChange(World worldIn, BlockPos pos, IBlockState state)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        // TODO Auto-generated method stub
        // should be able to find a loaded chunk and post a pending event to handle during validation
        // if the chunk is not loaded, is strange, but no reason to load it just to tell it to delete lava
        
    }

    @Override
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        // TODO Auto-generated method stub
        // if chunk is not loaded, simply mark the chunk for load
        // loading the chunk should cause any placed lava to be recognized
        // if the chunk is already loaded, queue lava addition to occur before cell validation
    }

    @Override
    protected void doFirstStep()
    {
        this.stepIndex++;
        final int size = this.connections.size();
        LavaConnection2[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doFirstStep(this);
        }
    }

    @Override
    protected void doStep()
    {
        this.stepIndex++;
        final int size = this.connections.size();
        LavaConnection2[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doStep(this);
        }
        
    }

    @Override
    protected void doLastStep()
    {
        this.stepIndex++;
        final int size = this.connections.size();
        LavaConnection2[] values = this.connections.values();
        for(int i = 0; i < size; i++)
        {
            values[i].doStep(this);
        }
    }

    @Override
    protected void doBlockUpdateProvision()
    {
        LAVA_THREAD_POOL.submit(() ->
            this.cells.parallelStream().forEach(c -> c.provideBlockUpdateIfNeeded(this))).join();       
    }

    @Override
    protected void doBlockUpdateApplication()
    {
        this.itMe = true;
        this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;
    }
    
    @Override
    protected void doLavaCooling()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void updateCells()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doCellValidation()
    {
        // this part needs to be done during tick
        // -------------------------------
        
        // Apply pending world events for pre-validation in loaded chunks so that
        // those chunks will match the world if they are marked for validation
        // TODO

        // Load and or validate marked chunks.
        // Chunks that were unloaded and had lava blocks created by world events will generally handle
        // those events simply by loading the chunk and reading in the world state. 
        this.cellChunkLoader.queueMarkedChunks(worldBuffer);
        
        // rest can be done post tick
        // -------------------------------

        // Apply world events for post-validation when the event does not imply
        // that world state will be different from simulation state. (Add lava from particles)
        // TODO
        
        // Add or update cells from world as needed
        // could be concurrent, but not yet implemented as such
        ColumnChunkBuffer buffer = this.cellChunkLoader.poll();
        while(buffer != null)
        {
            this.cells.loadOrValidateChunk(buffer);
            buffer = this.cellChunkLoader.poll();
        }
        
        
        // update connections as needed
        this.cells.parallelStream().forEach(c -> c.updateConnectionsIfNeeded(cells, connections));
        
        // clear out cells no longer needed
        // NON-CONCURRENT
        this.cells.clearDeletedCells();
    }

    @Override
    protected void doConnectionValidation()
    {
        // TODO Auto-generated method stub
        
    }
    
 
 
}
