package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class ModelStateComponent<T extends ModelStateValue<T, V>, V> 
{
    private final int bitLength;
    private final long bitMask;
    private final int ordinal;
    private final long valueCount;
    private final WorldRefreshType refreshType;
    
    public static enum WorldRefreshType
    {
        NEVER,
        SOMETIMES,
        ALWAYS
    }
    
    public ModelStateComponent(int ordinal, WorldRefreshType refreshType, long valueCount)
    {
        this.valueCount = valueCount;
        bitLength = Long.SIZE - Long.numberOfLeadingZeros(valueCount - 1);

        // note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow & signed values
        long mask = 0L;
        for(int i = 0; i < bitLength; i++)
        {
            mask |= (1L << i);
        }
        this.bitMask = mask;
        this.ordinal = ordinal;
        this.refreshType = refreshType;
    }
    
    public ModelStateComponent(int ordinal, long valueCount)
    {
        this(ordinal, WorldRefreshType.NEVER, valueCount);
    }

    abstract public T createValueFromBits(long bits);
    abstract public Class<T> getStateType();
    abstract public Class<V> getValueType();
    
    public final long getValueCount() { return this.valueCount; }
    
    /** override if can derive state from meta or neighbor blocks */
    public boolean canRefreshFromWorld(WorldRefreshType refreshType) { return this.refreshType.ordinal() >= refreshType.ordinal(); }

    /** override if can derive state from meta or neighbor blocks */
    public long getBitsFromWorld(NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos) { return 0; }

    // another option vs. statically defining in each subclass
//    @SuppressWarnings("unchecked")
//    public Class<T> getStateType()
//    {
//            ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
//            return (Class<T>) superclass.getActualTypeArguments()[0];
//    }
//    
//    @SuppressWarnings("unchecked")
//    
//    public Class<V> getValueType()
//    {
//            ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
//            return (Class<V>) superclass.getActualTypeArguments()[1];
//    }
    
    public int getOrdinal() { return ordinal; }
    public int getBitLength() { return bitLength; }
    public long getBitMask() { return bitMask; }
}
