package grondag.hard_science.machines.support;

import java.util.function.Function;

import grondag.hard_science.machines.support.IMachinePowerProvider;

public class MachinePower
{
    public static final long JOULES_PER_KWH = 3600000;
    
    public static enum FuelCellSpec
    {
        STANDARD_INTEGRATED(JOULES_PER_KWH, 1000, 2000, 800);
        
        public static final FuelCellSpec[] VALUES = FuelCellSpec.values();
        
        /**
         * Corresponds to {@link IMachinePowerProvider#maxEnergyJoules}
         */
        public final long maxEnergyJoules;
        
        /**
         * Corresponds to {@link IMachinePowerProvider#maxPowerInputWatts().}
         */
        public final long maxPowerInputWatts;
        
        /**
         * Corresponds to {@link IMachinePowerProvider#maxEnergyInputPerTick()()}
         */
        public final long maxEnergyInputPerTick;
        
        /**
         * Corresponds to {@link IMachinePowerProvider#maxPowerOutputWatts()}
         */
        public final long maxPowerOutputWatts;
        
        /**
         * Corresponds to {@link IMachinePowerProvider#maxEnergyOutputPerTick()()}
         */
        public final long maxEnergyOutputPerTick;
        
        /**
         * Corresponds to {@link IMachinePowerProvider#maxPowerInOrOutWatts()}
         */
        public final long maxPowerInOrOutWatts;

        /**
         * Natural log of {@link #maxPowerInOrOutWatts}
         */
        public final double logMaxInOrOut;
        
        /**
         * For fuel cells and generators, efficiency of power conversion for fuel consumed. As value 0 to 1000.
         */
        public final int conversionEfficiencyPerKilo;
        
        
        private FuelCellSpec(long maxEnergyJoules, long maxPowerInputWatts, long maxPowerOutputWatts, int conversionEfficiencyPerKilo)
        {
            this.maxEnergyJoules = maxEnergyJoules;
            this.maxPowerInputWatts = maxPowerInputWatts;
            this.maxEnergyInputPerTick = maxPowerInputWatts / 20;
            this.maxPowerOutputWatts = maxPowerOutputWatts;
            this.maxEnergyOutputPerTick = maxPowerOutputWatts / 20;
            this.maxPowerInOrOutWatts = Math.max(maxPowerInputWatts, maxPowerOutputWatts);
            this.conversionEfficiencyPerKilo = conversionEfficiencyPerKilo;
            this.logMaxInOrOut = Math.log(this.maxPowerInOrOutWatts);
        }
    }
    
    
    private static final Function<?, ?> FORMAT_JOULES[] = 
    {
            // 0 digits- should never get
            new Function<String, String>() { public String apply(String t) { return t + "J"; }},
            
            // 1 digit
            new Function<String, String>() { public String apply(String t) { return t + "J"; }},
            
            // 2  digits
            new Function<String, String>() { public String apply(String t) { return t + "J"; }},
            
            // 3  digits
            new Function<String, String>() { public String apply(String t) { return t + "J"; }},
            
            // 4  digits - kJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "kJ"); }},
            
            // 5  digits - kJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "kJ"); }},
            
            // 6  digits - kJ
            new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "kJ"; }},
            
            // 7  digits - MJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "MJ"); }},
            
            // 8  digits - MJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "MJ"); }},
            
            // 9  digits - MJ
            new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "MJ"; }},
            
            // 10  digits - GJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "GJ"); }},
            
            // 11  digits - GJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "GJ"); }},
            
            // 12  digits - GJ
            new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "GJ"; }},
            
            // 13  digits - TJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "TJ"); }},
            
            // 14  digits - TJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "TJ"); }},
            
            // 15  digits - TJ
            new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "TJ"; }},

            // 16  digits - PJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "PJ"); }},
            
            // 17  digits - PJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "PJ"); }},
            
            // 18  digits - PJ
            new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "PJ"; }},

            // 19  digits - EJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "EJ"); }},
            
            // 20  digits - EJ
            new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "EJ"); }},
            
            // 21  digits - EJ
            new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "EJ"; }},
            
            new Function<String, String>() { public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { public String apply(String t) { return "WOW"; }}
    };
    
    private static final Function<?, ?> FORMAT_WATTS[] = 
        {
                // 0 digits- should never get
                new Function<String, String>() { public String apply(String t) { return t + "W"; }},
                
                // 1 digit
                new Function<String, String>() { public String apply(String t) { return t + "W"; }},
                
                // 2  digits
                new Function<String, String>() { public String apply(String t) { return t + "W"; }},
                
                // 3  digits
                new Function<String, String>() { public String apply(String t) { return t + "W"; }},
                
                // 4  digits - kJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "kW"); }},
                
                // 5  digits - kJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "kW"); }},
                
                // 6  digits - kJ
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "kW"; }},
                
                // 7  digits - MJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "MW"); }},
                
                // 8  digits - MJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "MW"); }},
                
                // 9  digits - MJ
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "MW"; }},
                
                // 10  digits - GJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "GW"); }},
                
                // 11  digits - GJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "GW"); }},
                
                // 12  digits - GJ
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "GW"; }},
                
                // 13  digits - TJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "TW"); }},
                
                // 14  digits - TJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "TW"); }},
                
                // 15  digits - TJ
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "TW"; }},

                // 16  digits - PJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "PW"); }},
                
                // 17  digits - PJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "PW"); }},
                
                // 18  digits - PJ
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "PW"; }},

                // 19  digits - EJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal1_2Suffix(t, "EW"); }},
                
                // 20  digits - EJ
                new Function<String, String>() { public String apply(String t) { return insertDecimal2_1Suffix(t, "EW"); }},
                
                // 21  digits - EJ
                new Function<String, String>() { public String apply(String t) { return t.substring(0, 3) + "EW"; }},
                
                new Function<String, String>() { public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { public String apply(String t) { return "WOW"; }}
        };
    
    private static String insertDecimal1_2Suffix(String inString, String withSuffix)
    {
        return inString.substring(0, 1) + "." + inString.substring(1, 3) + withSuffix;
    }
    
    private static String insertDecimal2_1Suffix(String inString, String withSuffix)
    {
        return inString.substring(0, 2) + "." + inString.substring(2, 3) + withSuffix;
    }
    
    @SuppressWarnings("unchecked")
    public static String formatEnergy(long joules, boolean includePositiveSign)
    {
        if(joules == 0) return "0J";

        String raw = Long.toString(joules);
        int len = raw.length();
        
        if(joules > 0)
        {
            String result = ((Function<String, String>)FORMAT_JOULES[len]).apply(raw);
            return includePositiveSign ? "+" + result : result;
        }
        else
        {
            return "-" + ((Function<String, String>)FORMAT_JOULES[len - 1]).apply(raw.substring(1));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static String formatPower(long watts, boolean includePositiveSign)
    {
        if(watts == 0) return "0J";

        String raw = Long.toString(watts);
        int len = raw.length();
        
        if(watts > 0)
        {
            String result = ((Function<String, String>)FORMAT_WATTS[len]).apply(raw);
            return includePositiveSign ? "+" + result : result;
        }
        else
        {
            return "-" + ((Function<String, String>)FORMAT_WATTS[len - 1]).apply(raw.substring(1));
        }
    }
}
