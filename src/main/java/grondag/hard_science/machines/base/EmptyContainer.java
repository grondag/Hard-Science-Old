package grondag.hard_science.machines.base;

import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

/**
 * For when it is useful to track the "open container" without 
 * actually having or displaying any slots.
 */
public class EmptyContainer extends MachineContainer
{
    public EmptyContainer(IInventory playerInventory, MachineContainerTileEntity te, ContainerLayout layout) 
    {
        super(null, te, layout);

        // This container references items out of our own inventory (the 9 slots we hold ourselves)
        // as well as the slots from the player inventory so that the user can transfer items between
        // both inventories. The two calls below make sure that slots are defined for both inventories.
        this.addMachineSlots();
        this.addPlayerSlots(playerInventory, layout);
    }

    protected void addPlayerSlots(IInventory playerInventory,ContainerLayout layout)
    {
        // NOOP
    
    }

    protected  void addMachineSlots() {};
    

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return te.canInteractWith(playerIn);
    }

    public MachineContainerTileEntity tileEntity()
    {
        return this.te;
    }
}