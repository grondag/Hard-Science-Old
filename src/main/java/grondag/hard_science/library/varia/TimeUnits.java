package grondag.hard_science.library.varia;

public class TimeUnits
{
    public static final int MINUTES_PER_HOUR = 60;
    public static final int SECONDS_PER_HOUR = MINUTES_PER_HOUR * 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR;
    public static final int SECONDS_PER_DAY = MINUTES_PER_DAY * 60;
    
    public static final int TICKS_PER_REAL_SECOND = 20;
    public static final int TICKS_PER_REAL_MINUTE = TICKS_PER_REAL_SECOND * 60;
    public static final int TICKS_PER_REAL_HOUR = TICKS_PER_REAL_MINUTE * 60;
    public static final int TICKS_PER_REAL_DAY = TICKS_PER_REAL_HOUR * 24;
    
    public static final int TICKS_PER_SIMULATED_DAY = 24000;
    public static final int TICKS_PER_SIMULATED_HOUR = TICKS_PER_SIMULATED_DAY / 24;
    public static final float TICKS_PER_SIMULATED_MINUTE = TICKS_PER_SIMULATED_HOUR / 60f;
    public static final float TICKS_PER_SIMULATED_SECOND = TICKS_PER_SIMULATED_MINUTE / 60f;
    
    public static final int SIMULATED_SECONDS_PER_REAL_SECOND = SECONDS_PER_DAY * TICKS_PER_REAL_SECOND / TICKS_PER_SIMULATED_DAY;
    public static final float SIMULATED_SECONDS_PER_TICK = (float) SECONDS_PER_DAY / TICKS_PER_SIMULATED_DAY;
    
    /** Equivalent to (ticks * TimeUnits.SIMULATED_SECONDS_PER_TICK) but avoids floating point math */
    public static long ticksToSimulatedSeconds(long ticks)
    {
        return ticks * SECONDS_PER_DAY / TICKS_PER_SIMULATED_DAY;
    }
    
    /** Equivalent to (ticks * TimeUnits.SIMULATED_SECONDS_PER_TICK) but avoids floating point math */
    public static int ticksToSimulatedSeconds(int ticks)
    {
        return ticks * SECONDS_PER_DAY / TICKS_PER_SIMULATED_DAY;
    }

    
    
    
    
}
