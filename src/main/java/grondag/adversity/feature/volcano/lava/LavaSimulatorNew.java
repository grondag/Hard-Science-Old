package grondag.adversity.feature.volcano.lava;


import java.util.List;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import grondag.adversity.feature.volcano.lava.cell.LavaCells;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class LavaSimulatorNew extends AbstractLavaSimulator
{
    private final LavaConnections connections = new LavaConnections();
    private final LavaCells cells = new LavaCells();
    
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
        
    }

    @Override
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        // TODO Auto-generated method stub
        
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
        List<Chunk> updates = this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;

        //validate cells in chunks that were just written to world
        for(Chunk chunk : updates)
        {
            
        }
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
        // Note that actual cell validation now occurs in doBlockUpdateApplication
        // Methods should be rearranged once compatibility with prior sim no longer needed
        
        this.cells.clearDeletedCells();
    }

    @Override
    protected void doConnectionValidation()
    {
        // TODO Auto-generated method stub
        
    }
 
}
