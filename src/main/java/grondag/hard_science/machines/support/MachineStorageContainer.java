package grondag.hard_science.machines.support;

import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineStorageTileEntity;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.IStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container for SmartChest or other machines that present a slot-like view
 * of an underlying ItemStorage.
 */
public class MachineStorageContainer extends MachineContainer
{

    public MachineStorageContainer(IInventory playerInventory, MachineStorageTileEntity te, ContainerLayout layout)
    {
        super(playerInventory, te, layout);
    }
    
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();
        
            if(playerIn instanceof EntityPlayerMP)
            {
                IStorage<StorageType.StorageTypeStack> storage = ((MachineStorageTileEntity)this.te).getStorage();
                if(storage == null) return ItemStack.EMPTY;
                
                int consumed = (int) storage.add(ItemResourceWithQuantity.fromStack(slotStack), false);
                if(consumed > 0)
                {
                    slotStack.shrink(consumed);
                    if (slotStack.isEmpty())
                    {
                        slot.putStack(ItemStack.EMPTY);
                    } else
                    {
                        slot.onSlotChanged();
                    }
                }
                
                // always update client, in case was unable to transfer all of stack for any reason
                ((EntityPlayerMP)playerIn).sendSlotContents(this, slot.slotNumber, slot.getStack());
            }
            else
            {
                // on client, always assume entire stack was transfered
                // server will send corrective update if that was not the case
                slot.putStack(ItemStack.EMPTY);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        if(listener instanceof EntityPlayerMP && this.te != null && ((MachineStorageTileEntity)this.te).getStorage() != null)
        {
            ((MachineStorageTileEntity)this.te).getStorage().addListener(new OpenContainerStorageListener.ItemListener((EntityPlayerMP)listener));
        }
    }
}
