package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.base.NiceBlock;
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
        NEVER,  // state is not stored in world
        CACHED, // state can be derived from world, but cache in TE for performance if possible
        ALWAYS  // state is always derived from world
    }
    
    public ModelStateComponent(int ordinal, WorldRefreshType refreshType, long valueCount)
    {
        this.valueCount = valueCount;
        this.bitLength = Useful.bitLength(valueCount);
        this.bitMask = Useful.longBitMask(this.bitLength);
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
    public WorldRefreshType getRefreshType() { return this.refreshType; }

    /** override if can derive state from meta or neighbor blocks */
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos) { return 0; }

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
