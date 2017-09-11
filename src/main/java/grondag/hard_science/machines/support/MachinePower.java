package grondag.hard_science.machines.support;

public class MachinePower
{
    public static final long JOULES_PER_KWH = 3600000;
    
    public static enum FuelCellSpec
    {
        STANDARD_INTEGRATED(JOULES_PER_KWH, 1000, 2000, 80);
        
        public static final FuelCellSpec[] VALUES = FuelCellSpec.values();
        
        public final long maxEnergyJoules;
        public final long maxPowerInputWatts;
        public final long maxPowerOutputWatts;
        public final long maxPowerInOrOutWatts;
        public final int conversionEfficiencyPercent;
        
        private FuelCellSpec(long maxEnergyJoules, long maxPowerInputWatts, long maxPowerOutputWatts, int conversionEfficiencyPercent)
        {
            this.maxEnergyJoules = maxEnergyJoules;
            this.maxPowerInputWatts = maxPowerInputWatts;
            this.maxPowerOutputWatts = maxPowerOutputWatts;
            this.maxPowerInOrOutWatts = Math.max(maxPowerInputWatts, maxPowerOutputWatts);
            this.conversionEfficiencyPercent = conversionEfficiencyPercent;
        }
    }
}
