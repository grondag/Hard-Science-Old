package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IMessagePlus;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of static energy component descriptive information for display on client.
 */
public class EnergyComponentInfo implements IMessagePlus
{
    /**
     * See {@link IPowerComponent#componentType()}
     */
    public EnergyComponentType componentType() { return this.componentType; }
    private EnergyComponentType componentType;
    
    /**
     * See {@link IPowerComponent#maxStoredEnergyJoules()}
     */
    public long maxStoredEnergyJoules() { return this.maxStoredEnergyJoules; }
    private long maxStoredEnergyJoules;
    
    /**
     * See {@link IPowerComponent#maxPowerInputWatts()}
     */
    public float maxPowerInputWatts() { return this.maxPowerInputWatts; }
    private float maxPowerInputWatts;
    
    /**
     * See {@link IPowerComponent#maxPowerOutputWatts()}
     */
    public float maxPowerOutputWatts() { return this.maxPowerOutputWatts; }
    private float maxPowerOutputWatts;
    
    public EnergyComponentInfo() {}
    
    public EnergyComponentInfo(IPowerComponent from)
    {
        this.componentType = from.componentType();
        this.maxStoredEnergyJoules = from.maxStoredEnergyJoules();
        this.maxPowerInputWatts = from.maxPowerInputWatts();
        this.maxPowerOutputWatts = from.maxPowerOutputWatts();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.componentType = pBuff.readEnumValue(EnergyComponentType.class);
        this.maxStoredEnergyJoules = pBuff.readVarLong();
        this.maxPowerInputWatts = pBuff.readFloat();
        this.maxPowerOutputWatts = pBuff.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this.componentType);
        pBuff.writeVarLong(this.maxStoredEnergyJoules);
        pBuff.writeFloat(this.maxPowerInputWatts);
        pBuff.writeFloat(this.maxPowerOutputWatts);        
    }
}
