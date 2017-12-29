package grondag.hard_science.simulator.transport.carrier;

import javax.annotation.Nonnull;

import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.simulator.resource.EnumStorageType;
import grondag.hard_science.simulator.resource.StorageType;

public class Carrier
{
    private static final Carrier[][] lookup = new Carrier[EnumStorageType.values().length][CarrierLevel.values().length];
    
    public static Carrier findByTypeAndLevel(@Nonnull StorageType<?> storageType, @Nonnull CarrierLevel level)
    {
        return lookup[storageType.enumType.ordinal()][level.ordinal()];
    }

    public static final Carrier POWER_BASE = new Carrier(StorageType.POWER, CarrierLevel.BOTTOM, MachinePower.POWER_BUS_JOULES_PER_TICK);
    public static final Carrier POWER_INTER = new Carrier(StorageType.POWER, CarrierLevel.MIDDLE, MachinePower.POWER_BUS_JOULES_PER_TICK * 1000);
    public static final Carrier POWER_TOP = new Carrier(StorageType.POWER, CarrierLevel.TOP, MachinePower.POWER_BUS_JOULES_PER_TICK * 1000000);
    
    public static final Carrier ITEM_BASE = new Carrier(StorageType.ITEM, CarrierLevel.BOTTOM, 64);
    public static final Carrier ITEM_INTER = new Carrier(StorageType.ITEM, CarrierLevel.MIDDLE, 64 * 8);
    public static final Carrier ITEM_TOP = new Carrier(StorageType.ITEM, CarrierLevel.TOP, 64 * 10000);

    public final StorageType<?> storageType;
    public final CarrierLevel level;
    public final long capacityPerTick;
    
    private Carrier(StorageType<?> storageType, CarrierLevel level, long capacityPerTick)
    {
        this.storageType = storageType;
        this.level = level;
        this.capacityPerTick = capacityPerTick;
        lookup[storageType.enumType.ordinal()][level.ordinal()] = this;
    }
    
}
