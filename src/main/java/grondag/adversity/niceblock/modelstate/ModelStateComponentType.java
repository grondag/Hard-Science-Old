package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;

public enum ModelStateComponentType
{
    AXIS(ModelAxisFactory.INSTANCE, 2),
    CORNER_JOIN(ModelCornerJoinStateFactory.INSTANCE,
            Integer.SIZE - Integer.numberOfLeadingZeros(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT));
    
    //private final ModelStateComponentFactory<?>.ModelStateComponent instance;
    private final AbstractModelStateComponentFactory<?> factory;
    protected final int bitLength;
    protected final long bitMask;
    
    private ModelStateComponentType(AbstractModelStateComponentFactory<?> factory, int bitLength)
    {
        //this.instance = instance;
        this.factory = factory;
        this.bitLength = bitLength;
        long mask = 0L;
        for(int i = 0; i < bitLength; i++)
        {
            mask |= (1L << i);
        }
        this.bitMask = mask;
    }
    
   // public ModelStateComponentFactory<?>.ModelStateComponent getInstance() { return this.instance; }
    public AbstractModelStateComponentFactory<?> getFactory() { return this.factory; }
    public int getBitLength() { return bitLength; }
    public long getBitMask() { return bitMask; }
}