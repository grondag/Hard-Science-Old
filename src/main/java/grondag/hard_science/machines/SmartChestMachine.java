package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.nbt.NBTTagCompound;

public class SmartChestMachine extends AbstractSimpleMachine
{
    protected final ItemStorage itemStorage;
    
    public SmartChestMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
        this.itemStorage = new ItemStorage(this);
    }

    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return false;
    }
   
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.itemStorage.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.itemStorage.serializeNBT(tag);
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        this.itemStorage.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.itemStorage.onDisconnect();
        super.onDisconnect();
    }
    
    @Override
    public ItemStorage itemStorage()
    {
        return this.itemStorage;
    }
}
