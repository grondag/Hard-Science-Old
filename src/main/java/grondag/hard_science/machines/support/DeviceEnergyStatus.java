package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IMessagePlus;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of dynamic energy component descriptive information for display on client.
 */
public class DeviceEnergyStatus implements IMessagePlus
{
    private boolean isFailureCause;
    private float powerOutputWatts;
    private EnergyComponentStatus battery;
    private EnergyComponentStatus fuelCell;
    // currently not used
//    private EnergyComponentStatus powerReceiver;

    public DeviceEnergyStatus() { }
    
    public DeviceEnergyStatus(DeviceEnergyManager powerSupply)
    {
        this.isFailureCause = powerSupply.isFailureCause();
        this.powerOutputWatts = powerSupply.powerOutputWatts();
        if(powerSupply.battery() != null) this.battery = new EnergyComponentStatus(powerSupply.battery());
        if(powerSupply.fuelCell() != null) this.fuelCell = new EnergyComponentStatus(powerSupply.fuelCell());
//        if(powerSupply.powerReceiver() != null) this.powerReceiver = new EnergyComponentStatus(powerSupply.powerReceiver());
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBoolean(this.isFailureCause);
        pBuff.writeFloat(this.powerOutputWatts);
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
        this.isFailureCause = pBuff.readBoolean();
        this.powerOutputWatts = pBuff.readFloat();
        if(pBuff.readBoolean())
        {
            this.battery = new EnergyComponentStatus();
            this.battery.fromBytes(pBuff);
        }
        if(pBuff.readBoolean())
        {
            this.fuelCell = new EnergyComponentStatus();
            this.fuelCell.fromBytes(pBuff);
        }        
    }

    public float powerOutputWatts()
    {
        return this.powerOutputWatts;
    }

    public boolean isFailureCause()
    {
        return this.isFailureCause;
    }

    public EnergyComponentStatus fuelCell()
    {
        return this.fuelCell;
    }

    public EnergyComponentStatus battery()
    {
        return this.battery;
    }

    public boolean hasFuelCell()
    {
        return this.fuelCell != null;
    }

    public boolean hasBattery()
    {
        return this.battery != null;
    }
}
