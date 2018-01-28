package grondag.hard_science.simulator.transport.endpoint;

import static grondag.hard_science.simulator.resource.StorageType.FLUID;
import static grondag.hard_science.simulator.resource.StorageType.ITEM;
import static grondag.hard_science.simulator.resource.StorageType.POWER;
import static grondag.hard_science.simulator.transport.carrier.CarrierLevel.BOTTOM;
import static grondag.hard_science.simulator.transport.carrier.CarrierLevel.MIDDLE;
import static grondag.hard_science.simulator.transport.carrier.CarrierLevel.TOP;
import static grondag.hard_science.simulator.transport.carrier.Channel.CONFIGURABLE_FOLLOWS_DEVICE;
import static grondag.hard_science.simulator.transport.carrier.Channel.CONFIGURABLE_NOT_SET;
import static grondag.hard_science.simulator.transport.carrier.Channel.TOP_CHANNEL;
import static grondag.hard_science.simulator.transport.endpoint.PortConnector.COMPACT;
import static grondag.hard_science.simulator.transport.endpoint.PortConnector.STANDARD;
import static grondag.hard_science.simulator.transport.endpoint.PortFunction.BRIDGE;
import static grondag.hard_science.simulator.transport.endpoint.PortFunction.CARRIER;
import static grondag.hard_science.simulator.transport.endpoint.PortFunction.DIRECT;

import grondag.hard_science.matter.Matters;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;

public class PortDescriptions
{
    public static final PortDescription<StorageTypePower> POWER_LOW_CARRIER =
            PortDescription.find(POWER, BOTTOM, CARRIER, STANDARD, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypePower> POWER_LOW_CARRIER_COMPACT =
            PortDescription.find(POWER, BOTTOM, CARRIER, COMPACT, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypePower> POWER_LOW_DIRECT =
            PortDescription.find(POWER, BOTTOM, DIRECT, STANDARD, CONFIGURABLE_NOT_SET);
    
    public static final PortDescription<StorageTypePower> POWER_MID_CARRIER =
            PortDescription.find(POWER, MIDDLE, CARRIER, STANDARD, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypePower> POWER_MID_DIRECT =
            PortDescription.find(POWER, MIDDLE, DIRECT, STANDARD, CONFIGURABLE_NOT_SET);

    public static final PortDescription<StorageTypePower> POWER_MID_BRIDGE =
            PortDescription.find(POWER, MIDDLE, BRIDGE, STANDARD, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypePower> POWER_TOP_CARRIER =
            PortDescription.find(POWER, TOP, CARRIER, STANDARD, TOP_CHANNEL);
    
    public static final PortDescription<StorageTypePower> POWER_TOP_DIRECT =
            PortDescription.find(POWER, TOP, DIRECT, STANDARD, TOP_CHANNEL);

    public static final PortDescription<StorageTypePower> POWER_TOP_BRIDGE =
            PortDescription.find(POWER, TOP, BRIDGE, STANDARD, TOP_CHANNEL);
    
    
    public static final PortDescription<StorageTypeStack> ITEM_LOW_CARRIER =
            PortDescription.find(ITEM, BOTTOM, CARRIER, STANDARD, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypeStack> ITEM_LOW_DIRECT =
            PortDescription.find(ITEM, BOTTOM, DIRECT, STANDARD, CONFIGURABLE_NOT_SET);
    
    public static final PortDescription<StorageTypeStack> ITEM_MID_CARRIER =
            PortDescription.find(ITEM, MIDDLE, CARRIER, STANDARD, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypeStack> ITEM_MID_DIRECT =
            PortDescription.find(ITEM, MIDDLE, DIRECT, STANDARD, CONFIGURABLE_NOT_SET);

    public static final PortDescription<StorageTypeStack> ITEM_MID_BRIDGE =
            PortDescription.find(ITEM, MIDDLE, BRIDGE, STANDARD, CONFIGURABLE_FOLLOWS_DEVICE);
    
    public static final PortDescription<StorageTypeStack> ITEM_TOP_CARRIER =
            PortDescription.find(ITEM, TOP, CARRIER, STANDARD, TOP_CHANNEL);
    
    public static final PortDescription<StorageTypeStack> ITEM_TOP_DIRECT =
            PortDescription.find(ITEM, TOP, DIRECT, STANDARD, TOP_CHANNEL);

    public static final PortDescription<StorageTypeStack> ITEM_TOP_BRIDGE =
            PortDescription.find(ITEM, TOP, BRIDGE, STANDARD, TOP_CHANNEL);
    


    public static final PortDescription<StorageTypeFluid> WATER_LOW_CARRIER =
            PortDescription.find(FLUID, BOTTOM, CARRIER, STANDARD, Matters.WATER.channel());
    
    public static final PortDescription<StorageTypeFluid> WATER_LOW_DIRECT =
            PortDescription.find(FLUID, BOTTOM, DIRECT, STANDARD, Matters.WATER.channel());
    
    public static final PortDescription<StorageTypeFluid> WATER_MID_CARRIER =
            PortDescription.find(FLUID, MIDDLE, CARRIER, STANDARD, Matters.WATER.channel());
    
    public static final PortDescription<StorageTypeFluid> WATER_MID_DIRECT =
            PortDescription.find(FLUID, MIDDLE, DIRECT, STANDARD, Matters.WATER.channel());

    public static final PortDescription<StorageTypeFluid> WATER_MID_BRIDGE =
            PortDescription.find(FLUID, MIDDLE, BRIDGE, STANDARD, Matters.WATER.channel());
    
    public static final PortDescription<StorageTypeFluid> WATER_TOP_CARRIER =
            PortDescription.find(FLUID, TOP, CARRIER, STANDARD, Matters.WATER.channel());
    
    public static final PortDescription<StorageTypeFluid> WATER_TOP_DIRECT =
            PortDescription.find(FLUID, TOP, DIRECT, STANDARD, Matters.WATER.channel());

    public static final PortDescription<StorageTypeFluid> WATER_TOP_BRIDGE =
            PortDescription.find(FLUID, TOP, BRIDGE, STANDARD, Matters.WATER.channel());

    
    public static final PortDescription<StorageTypeFluid> H2O_LOW_CARRIER =
            PortDescription.find(FLUID, BOTTOM, CARRIER, STANDARD, Matters.H2O.channel());
    
    public static final PortDescription<StorageTypeFluid> H2O_LOW_DIRECT =
            PortDescription.find(FLUID, BOTTOM, DIRECT, STANDARD, Matters.H2O.channel());
    
    public static final PortDescription<StorageTypeFluid> H2O_MID_CARRIER =
            PortDescription.find(FLUID, MIDDLE, CARRIER, STANDARD, Matters.H2O.channel());
    
    public static final PortDescription<StorageTypeFluid> H2O_MID_DIRECT =
            PortDescription.find(FLUID, MIDDLE, DIRECT, STANDARD, Matters.H2O.channel());

    public static final PortDescription<StorageTypeFluid> H2O_MID_BRIDGE =
            PortDescription.find(FLUID, MIDDLE, BRIDGE, STANDARD, Matters.H2O.channel());
    
    public static final PortDescription<StorageTypeFluid> H2O_TOP_CARRIER =
            PortDescription.find(FLUID, TOP, CARRIER, STANDARD, Matters.H2O.channel());
    
    public static final PortDescription<StorageTypeFluid> H2O_TOP_DIRECT =
            PortDescription.find(FLUID, TOP, DIRECT, STANDARD, Matters.H2O.channel());

    public static final PortDescription<StorageTypeFluid> H2O_TOP_BRIDGE =
            PortDescription.find(FLUID, TOP, BRIDGE, STANDARD, Matters.H2O.channel());
    
    
    public static final PortDescription<StorageTypeFluid> AMMONIA_LOW_CARRIER =
            PortDescription.find(FLUID, BOTTOM, CARRIER, STANDARD, Matters.AMMONIA.channel());
    
    public static final PortDescription<StorageTypeFluid> AMMONIA_LOW_DIRECT =
            PortDescription.find(FLUID, BOTTOM, DIRECT, STANDARD, Matters.AMMONIA.channel());
    
    public static final PortDescription<StorageTypeFluid> AMMONIA_MID_CARRIER =
            PortDescription.find(FLUID, MIDDLE, CARRIER, STANDARD, Matters.AMMONIA.channel());
    
    public static final PortDescription<StorageTypeFluid> AMMONIA_MID_DIRECT =
            PortDescription.find(FLUID, MIDDLE, DIRECT, STANDARD, Matters.AMMONIA.channel());

    public static final PortDescription<StorageTypeFluid> AMMONIA_MID_BRIDGE =
            PortDescription.find(FLUID, MIDDLE, BRIDGE, STANDARD, Matters.AMMONIA.channel());
    
    public static final PortDescription<StorageTypeFluid> AMMONIA_TOP_CARRIER =
            PortDescription.find(FLUID, TOP, CARRIER, STANDARD, Matters.AMMONIA.channel());
    
    public static final PortDescription<StorageTypeFluid> AMMONIA_TOP_DIRECT =
            PortDescription.find(FLUID, TOP, DIRECT, STANDARD, Matters.AMMONIA.channel());

    public static final PortDescription<StorageTypeFluid> AMMONIA_TOP_BRIDGE =
            PortDescription.find(FLUID, TOP, BRIDGE, STANDARD, Matters.AMMONIA.channel());
}
