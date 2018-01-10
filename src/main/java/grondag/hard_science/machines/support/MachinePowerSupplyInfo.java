package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IMessagePlus;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of static energy component descriptive information for display on client.
 */
public class MachinePowerSupplyInfo implements IMessagePlus
{
    private float maxPowerOutputWatts;
    private EnergyComponentInfo battery;
    private EnergyComponentInfo fuelCell;
    // currently not used
//    private EnergyComponentInfo powerReceiver;
    
    public MachinePowerSupplyInfo() { }
    
    public MachinePowerSupplyInfo(MachinePowerSupply powerSupply)
    {
        this.maxPowerOutputWatts = powerSupply.maxPowerOutputWatts();
        if(powerSupply.battery() != null) this.battery = new EnergyComponentInfo(powerSupply.battery());
        if(powerSupply.fuelCell() != null) this.fuelCell = new EnergyComponentInfo(powerSupply.fuelCell());
//        if(powerSupply.powerReceiver() != null) this.powerReceiver = new EnergyComponentInfo(powerSupply.powerReceiver());
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeFloat(this.maxPowerOutputWatts);
        if(this.battery == null)
        {
            pBuff.writeBoolean(false);
        }
        else
        {
            pBuff.writeBoolean(true);
            this.battery.toBytes(pBuff);
        }
        if(this.fuelCell == null)
        {
            pBuff.writeBoolean(false);
        }
        else
        {
            pBuff.writeBoolean(true);
            this.fuelCell.toBytes(pBuff);
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.maxPowerOutputWatts = pBuff.readFloat();
        if(pBuff.readBoolean())
        {
            this.battery = new EnergyComponentInfo();
            this.battery.fromBytes(pBuff);
        }
        if(pBuff.readBoolean())
        {
            this.fuelCell = new EnergyComponentInfo();
            this.fuelCell.fromBytes(pBuff);
        }
    }

    public boolean hasBattery()
    {
        return this.battery != null;
    }

    public float maxPowerOutputWatts()
    {
        return this.maxPowerOutputWatts;
    }

    public EnergyComponentInfo fuelCell()
    {
        return this.fuelCell;
    }

    public EnergyComponentInfo battery()
    {
        return this.battery;
    }

    public boolean hasFuelCell()
    {
        return this.fuelCell != null;
    }
}
