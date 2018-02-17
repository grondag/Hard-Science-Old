package grondag.hard_science.machines.impl.logistics;

import grondag.hard_science.init.ModItems;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.ItemContainer;
import net.minecraft.nbt.NBTTagCompound;

public class SmartChestMachine extends AbstractSimpleMachine
{
    protected final ItemContainer itemStorage;
    
    public SmartChestMachine()
    {
        super();
        this.itemStorage = new ItemContainer(this, ContainerUsage.STORAGE);
        this.itemStorage.setContentPredicate( 
                r -> r != null 
                && !(((ItemResource)r).getItem() == ModItems.smart_chest 
                        && ((ItemResource)r).hasTagCompound()));
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
    public ItemContainer itemStorage()
    {
        return this.itemStorage;
    }
}
