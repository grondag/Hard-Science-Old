package grondag.adversity.niceblock.modelstate;

public class ModelStateSet
{
    private final int[] typeIndexes = new int[ModelStateComponentType.values().length];
    private final int[] shiftBits = new int[ModelStateComponentType.values().length];
    final int typeCount;
    public static final int NOT_PRESENT = -1;
    
    public ModelStateSet(ModelStateComponentType... components)
    {
        typeCount = components.length;
        
        for(int i = 0; i < ModelStateComponentType.values().length; i++)
        {
            typeIndexes[i] = NOT_PRESENT;
        }
        
        int counter = 0;
        int shift = 0;
        for(ModelStateComponentType c : components)
        {
            typeIndexes[c.ordinal()] = counter++;

            shiftBits[c.ordinal()] = shift;
            shift += c.getBitLength();
        }
    }
    
    public int getTypeCount()
    {
        return typeCount;
    }
    
    public int getIndexForType(ModelStateComponentType type)
    {
        return typeIndexes[type.ordinal()];
    }
    
    public int getBitShiftForType(ModelStateComponentType type)
    {
        return shiftBits[typeIndexes[type.ordinal()]];
    }
    
    public long computeKey(AbstractModelStateComponentFactory<?>.ModelStateComponent... components)
    {
        long key = 0L;
        for(AbstractModelStateComponentFactory<?>.ModelStateComponent c : components)
        {
            if(getIndexForType(c.getComponentType()) != NOT_PRESENT)
            {
                key |= (c.toBits() << getBitShiftForType(c.getComponentType()));
            }
        }
        return key;
    }
}