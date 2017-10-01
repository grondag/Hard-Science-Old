package grondag.hard_science.machines.support;

public enum VolumeUnits
{
    GIGALITER("GL",  1000000000000000000L),
    MEGALITER("ML",  1000000000000000L),
    KILOLITER("kL",  1000000000000L),
    LITER("L",       1000000000),
    MILLILITER("mL", 1000000),
    MICROLITER("μL", 1000),
    NANOLITER("nL",  1);
    
    public final String symbol;
    public final long nL;
    
    public static final int CUBIC_MICROMETERS_PER_NANOLITER = 1000000;
    
    private VolumeUnits(String symbol, long nL)
    {
        this.symbol = symbol;
        this.nL = nL;
    }
    
    public Volume withQuantity(long quantity)
    {
        return new Volume(quantity);
    }
    
    public static long convertFromTo(long value, VolumeUnits from, VolumeUnits to)
    {
        return value * from.nL / to.nL;
    }

    public class Volume
    {
        
        public final long quantity;
        
        private Volume(long quantity)
        {
            this.quantity = quantity;
        }
        
        public VolumeUnits unit()
        {
            return VolumeUnits.this;
        }
        
        public Volume convertTo(VolumeUnits toUnit)
        {
            return toUnit.withQuantity(convertFromTo(this.quantity, this.unit(), toUnit));
        }
        
        public long nanoLiters()
        {
            return unit().nL * this.quantity;
        }
        
    }
}
