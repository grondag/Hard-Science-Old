package grondag.hard_science.matter;

import java.util.function.Function;

import grondag.hard_science.machines.energy.MachinePower;

public enum MassUnits
{
    TONNE("t",  1000000000000000L),
    KILOGRAM("kg",  1000000000000L),
    GRAM("g",       1000000000),
    MILLIGRAM("mg", 1000000),
    MICROGRAM("μg", 1000),
    NANOGRAM("ng",  1);
    
    public final String symbol;
    public final long ng;
    
    public static final int CUBIC_MICROMETERS_PER_NANOLITER = 1000000;
    
    private static final Function<?, ?> FORMAT_NANOGRAMS[] = 
    {
                // 0 digits- should never get
                new Function<String, String>() { public String apply(String t) { return "ng"; }},
                
                // 1 digit
                new Function<String, String>() { public String apply(String t) { return t + "ng"; }},
                
                // 2  digits
                new Function<String, String>() { public String apply(String t) { return t + "ng"; }},
                
                // 3  digits
                new Function<String, String>() { public String apply(String t) { return t + "ng"; }},
                
                
                // 4  digits
                new Function<String, String>() { public String apply(String t) { return t + "ng"; }},
                
                // 5  digits
                new Function<String, String>() { public String apply(String t) { return "0.0" + t.substring(0, 1) + "mg"; }},
                
                // 6  digits
                new Function<String, String>() { public String apply(String t) { return "0." + t.substring(0, 2) + "mg"; }},
                
                // 7  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "mg"); }},
                
                // 8  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "mg"); }},
                
                // 9  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "mg"; }},
                
                // 10  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "g"); }},
                
                // 11  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "g"); }},
                
                // 12  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "g"; }},
                
                // 13  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "kg"); }},
                
                // 14  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "kg"); }},
                
                // 15  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "kg"; }},

                // 16  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "t"); }},
                
                // 17  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "t"); }},
                
                // 18  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "t"; }},

                // 19  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "kt"); }},
                
                // 20  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "kt"); }},
                
                // 21  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "kt"; }},
                
                // 22  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "mt"); }},
                
                // 23  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "mt"); }},
                
                // 24  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "mt"; }},
                
                // 25  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "gt"); }},
                
                // 26  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal2_1Suffix(t, "gt"); }},
                
                // 27  digits
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "gt"; }},

                // 28  digits
                new Function<String, String>() { public String apply(String t) { return MachinePower.insertDecimal1_2Suffix(t, "tt"); }},
        };
    
    private MassUnits(String symbol, long ng)
    {
        this.symbol = symbol;
        this.ng = ng;
    }
    
    @SuppressWarnings("unchecked")
    public static String formatMass(long nanoGrams, boolean includePositiveSign)
    {
        if(nanoGrams == 0) return "0L";

        String raw = Long.toString(nanoGrams);
        int len = raw.length();
        
        if(nanoGrams > 0)
        {
            String result = ((Function<String, String>)FORMAT_NANOGRAMS[len]).apply(raw);
            return includePositiveSign ? "+" + result : result;
        }
        else
        {
            return "-" + ((Function<String, String>)FORMAT_NANOGRAMS[len - 1]).apply(raw.substring(1));
        }
    }
    
    public Mass withQuantity(long quantity)
    {
        return new Mass(quantity);
    }
    
    public static long convertFromTo(long value, MassUnits from, MassUnits to)
    {
        return value * from.ng / to.ng;
    }

    public static long ng2kg(long value)
    {
        return convertFromTo(value, MassUnits.NANOGRAM, MassUnits.KILOGRAM);
    }
    
    public static long kg2ng(long value)
    {
        return convertFromTo(value, MassUnits.KILOGRAM, MassUnits.NANOGRAM);
    }
    
    public static long ng2grams(long value)
    {
        return convertFromTo(value, MassUnits.NANOGRAM, MassUnits.GRAM);
    }
    
    public static long grams2ng(long value)
    {
        return convertFromTo(value, MassUnits.GRAM, MassUnits.NANOGRAM);
    }
    
    public class Mass
    {
        
        public final long quantity;
        
        private Mass(long quantity)
        {
            this.quantity = quantity;
        }
        
        public MassUnits unit()
        {
            return MassUnits.this;
        }
        
        public Mass convertTo(MassUnits toUnit)
        {
            return toUnit.withQuantity(convertFromTo(this.quantity, this.unit(), toUnit));
        }
        
        public long nanoGrams()
        {
            return unit().ng * this.quantity;
        }
        
    }
}
