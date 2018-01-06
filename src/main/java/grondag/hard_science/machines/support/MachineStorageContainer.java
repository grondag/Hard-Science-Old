package grondag.hard_science.machines.support;

import java.util.HashMap;

import javax.annotation.Nullable;

import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.storage.ItemStorageListener;
import grondag.hard_science.simulator.transport.management.LogisticsService;
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
    private HashMap<EntityPlayerMP, ItemStorageListener> storageListeners
        = new HashMap<EntityPlayerMP, ItemStorageListener>();
    
    public MachineStorageContainer(IInventory playerInventory, MachineTileEntity te, ContainerLayout layout)
    {
        super(playerInventory, te, layout);
    }
    
    @Nullable
    public ItemStorageListener getItemListener(EntityPlayerMP player)
    {
        return this.storageListeners.get(player);
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
                IStorage<StorageType.StorageTypeStack> storage = ((MachineTileEntity)this.te).storageMachine();
                if(storage == null) return ItemStack.EMPTY;
                
                int consumed = (int) storage.add(ItemResourceWithQuantity.fromStack(slotStack), false, null);
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
        if(listener instanceof EntityPlayerMP && this.te != null && ((MachineTileEntity)this.te).storageMachine() != null)
        {
            ItemStorageListener newItemListener =
                    new ItemStorageListener(
                            (ItemStorage) ((MachineTileEntity)this.te).storageMachine(), 
                            (EntityPlayerMP)listener);
            
            
            assert this.storageListeners.put((EntityPlayerMP)listener, newItemListener) == null
                    : "Found existing storage listener for player on container";
            
            LogisticsService.ITEM_SERVICE.initializeListener(newItemListener);
            
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        ItemStorageListener oldListener = this.storageListeners.remove(playerIn);
        if(oldListener != null) oldListener.die();
        super.onContainerClosed(playerIn);
    }
    
    
}
