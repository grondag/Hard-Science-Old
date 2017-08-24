package grondag.hard_science.machines;


import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineContainerTileEntity;
import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.inventory.IInventory;


public class BasicBuilderContainer extends MachineContainer
{
 
    public BasicBuilderContainer(IInventory playerInventory, MachineContainerTileEntity te, ContainerLayout layout) 
    {
        super(playerInventory, te, layout);
    }
    

    @Override
    protected void addMachineSlots()
    {
        // nothing for this one
    }
}