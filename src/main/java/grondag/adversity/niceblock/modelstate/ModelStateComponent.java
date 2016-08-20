package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class ModelStateComponent<T extends IModelStateValue<T, V>, V> 
{
    private final int bitLength;
    private final long bitMask;
    private final int ordinal;
    
    public ModelStateComponent(int ordinal)
    {
        long mask = 0L;
        bitLength = Long.SIZE - Long.numberOfLeadingZeros(getValueCount());
        for(int i = 0; i < bitLength; i++)
        {
            mask |= (1L << i);
        }
        this.bitMask = mask;
        this.ordinal = ordinal;
    }
    abstract public long getValueCount();
    abstract public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos);
    abstract public T createValueFromBits(long bits);
//    abstract public long getBits(V value);
    abstract public Class<T> getStateType();
    abstract public Class<V> getValueType();

    public int getOrdinal() { return ordinal; }
    public int getBitLength() { return bitLength; }
    public long getBitMask() { return bitMask; }
}
