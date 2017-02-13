package grondag.adversity.feature.volcano.lava;

import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractLavaSimulator extends SimulationNode
{

    public static final byte LEVELS_PER_BLOCK = FlowHeightState.BLOCK_LEVELS_INT;
    public static final byte LEVELS_PER_QUARTER_BLOCK = FlowHeightState.BLOCK_LEVELS_INT / 4;
    public static final byte LEVELS_PER_HALF_BLOCK = FlowHeightState.BLOCK_LEVELS_INT / 2;
    public static final byte LEVELS_PER_BLOCK_AND_A_QUARTER = LEVELS_PER_BLOCK + LEVELS_PER_QUARTER_BLOCK;
    public static final byte LEVELS_PER_BLOCK_AND_A_HALF = LEVELS_PER_BLOCK + LEVELS_PER_HALF_BLOCK;
    public static final byte LEVELS_PER_TWO_BLOCKS = LEVELS_PER_BLOCK * 2;
    public static final int FLUID_UNITS_PER_LEVEL = 1000;
    public static final int FLUID_UNITS_PER_BLOCK = FLUID_UNITS_PER_LEVEL * LEVELS_PER_BLOCK;
    public static final int FLUID_UNTIS_PER_HALF_BLOCK = FLUID_UNITS_PER_BLOCK / 2;

    public AbstractLavaSimulator(int nodeID)
    {
        super(nodeID);
    }

    public abstract float loadFactor();

    public abstract int getTickIndex();

    public abstract void writeToNBT(NBTTagCompound nbt);

    public abstract void readFromNBT(NBTTagCompound nbt);

    public abstract void queueParticle(long packedBlockPos, int amount);

    public abstract void addLava(BlockPos pos, int amount, boolean shouldResynchToWorldBeforeAdding);

    public abstract void notifyLavaNeighborChange(World worldIn, BlockPos pos, IBlockState state);

    public abstract void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state);

    public abstract void trackCoolingBlock(BlockPos pos);

    public abstract void trackLavaFiller(BlockPos pos);

    public abstract void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state);

    public abstract void registerCoolingBlock(World worldIn, BlockPos pos);

    public abstract void doTick(int newLastTickIndex);

}