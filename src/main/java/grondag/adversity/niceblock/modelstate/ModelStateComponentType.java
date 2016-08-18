package grondag.adversity.niceblock.modelstate;

public enum ModelStateComponentType
{
    AXIS(ModelAxisAdapter.INSTANCE),
    CORNER_JOIN(ModelCornerJoinStateAdapter.INSTANCE);
    
    //private final ModelStateComponentFactory<?>.ModelStateComponent instance;
    private final ModelStateComponentFactory<?> factory;
    private final AbstractModelStateComponentAdapter<?> adapter;
    protected final int bitLength;
    protected final long bitMask;
    
    private <T extends IModelStateComponent<?>> ModelStateComponentType(AbstractModelStateComponentAdapter<T> adapter)
    {
        //this.instance = instance;
        
        this.factory = new ModelStateComponentFactory<T>(adapter);
        this.adapter = adapter;
        long mask = 0L;
        bitLength = Long.SIZE - Long.numberOfLeadingZeros(adapter.getValueCount());
        for(int i = 0; i < bitLength; i++)
        {
            mask |= (1L << i);
        }
        this.bitMask = mask;
    }
    
   // public ModelStateComponentFactory<?>.ModelStateComponent getInstance() { return this.instance; }
    public ModelStateComponentFactory<?> getFactory() { return this.factory; }
    public AbstractModelStateComponentAdapter<?> getAdapter() { return this.adapter; }
    public int getBitLength() { return bitLength; }
    public long getBitMask() { return bitMask; }
}