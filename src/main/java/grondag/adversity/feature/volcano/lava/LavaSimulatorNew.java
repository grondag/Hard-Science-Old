package grondag.adversity.feature.volcano.lava;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaSimulatorNew extends AbstractLavaSimulator
{
    private final ConcurrentHashMap<Long, LavaCell2> allCells = new ConcurrentHashMap<Long, LavaCell2>(16000, 0.6F, 8);
    private final static String LAVA_CELL_NBT_TAG = "lavacells";
    private static final int LAVA_CELL_NBT_WIDTH = 5;
    
    private final ConnectionMap connections = new ConnectionMap();
    
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCellCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getConnectionCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void saveLavaNBT(NBTTagCompound nbt)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void readLavaNBT(NBTTagCompound nbt)
    {
        // TODO Auto-generated method stub
        
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
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doStep()
    {
        this.stepIndex++;
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doLastStep()
    {
        this.stepIndex++;
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doBlockUpdateProvision()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doLavaCooling()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected boolean isBlockLavaBarrier(long packedBlockPos)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean isHighEnoughForParticle(long packedBlockPos)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void updateCells()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doCellValidation()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doConnectionValidation()
    {
        // TODO Auto-generated method stub
        
    }
 
}
