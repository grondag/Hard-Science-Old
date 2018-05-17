package grondag.hard_science.matter;

import grondag.hard_science.machines.energy.MachinePower;

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
    
    @FunctionalInterface
    private static interface Formatter
    {
        String apply(String t);
    }
    
    private static final Formatter FORMAT_NANOLITERS[] = 
    {
                // 0 digits- should never get
                new Formatter() { @Override
                public String apply(String t) { return "nL"; }},
                
                // 1 digit
                new Formatter() { @Override
                public String apply(String t) { return t + "nL"; }},
                
                // 2  digits
                new Formatter() { @Override
                public String apply(String t) { return t + "nL"; }},
                
                // 3  digits
                new Formatter() { @Override
                public String apply(String t) { return t + "nL"; }},
                
                // FIXME: code in MachinePower should be somewhere else
                
                // 4  digits
                new Formatter() { @Override
                public String apply(String t) { return t + "nL"; }},
                
                // 5  digits
                new Formatter() { @Override
                public String apply(String t) { return "0.0" + t.substring(0, 1) + "mL"; }},
                
                // 6  digits
                new Formatter() { @Override
                public String apply(String t) { return "0." + t.substring(0, 2) + "mL"; }},
                
                // 7  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "mL"); }},
                
                // 8  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "mL"); }},
                
                // 9  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "mL"; }},
                
                // 10  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "L"); }},
                
                // 11  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "L"); }},
                
                // 12  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "L"; }},
                
                // 13  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "kL"); }},
                
                // 14  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "kL"); }},
                
                // 15  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "kL"; }},

                // 16  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "ML"); }},
                
                // 17  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "ML"); }},
                
                // 18  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "ML"; }},

                // 19  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "GL"); }},
                
                // 20  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "GL"); }},
                
                // 21  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "GL"; }},
                
                // 22  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "TL"); }},
                
                // 23  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "TL"); }},
                
                // 24  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "TL"; }},
                
                // 25  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "PL"); }},
                
                // 26  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "PL"); }},
                
                // 27  digits
                new Formatter() { @Override
                public String apply(String t) { return t.substring(0, 3) + "PL"; }},

                // 28  digits
                new Formatter() { @Override
                public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "EL"); }},
        };
    
    private VolumeUnits(String symbol, long nL)
    {
        this.symbol = symbol;
        this.nL = nL;
    }
    
    public static String formatVolume(long nanoLiters, boolean includePositiveSign)
    {
        if(nanoLiters == 0) return "0L";

        String raw = Long.toString(nanoLiters);
        int len = raw.length();
        
        if(nanoLiters > 0)
        {
            String result = FORMAT_NANOLITERS[len].apply(raw);
            return includePositiveSign ? "+" + result : result;
        }
        else
        {
            return "-" + FORMAT_NANOLITERS[len - 1].apply(raw.substring(1));
        }
    }
    
    public Volume withQuantity(long quantity)
    {
        return new Volume(quantity);
    }
    
    /** rounds down when going from smaller to larger */
    public static long convertFromTo(long value, VolumeUnits from, VolumeUnits to)
    {
        return value * from.nL / to.nL;
    }

    public static double nL2Liters(long value)
    {
        return convertFromTo(value, VolumeUnits.NANOLITER, VolumeUnits.LITER);
    }
    
    public static double nL2Blocks(long value)
    {
        return value / (double)VolumeUnits.KILOLITER.nL;
    }
    
    public static long blocks2nL(int kL)
    {
        return convertFromTo(kL, VolumeUnits.KILOLITER, VolumeUnits.NANOLITER);
    }
    
    public static long liters2nL(long value)
    {
        return convertFromTo(value, VolumeUnits.LITER, VolumeUnits.NANOLITER);
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
