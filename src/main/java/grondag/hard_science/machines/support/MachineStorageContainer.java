package grondag.hard_science.machines.support;

import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
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
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack slotStack = slot.getStack();
        if(slotStack == null || slotStack.isEmpty()) return ItemStack.EMPTY;
    
        if(!(playerIn instanceof EntityPlayerMP))
        {
            // on client, always assume entire stack was transfered
            // server will send corrective update if that was not the case
            slot.putStack(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        
        ItemStorageListener storage = getItemListener((EntityPlayerMP) playerIn);
        if(storage == null || storage.isDead()) return ItemStack.EMPTY;
            
        ItemResourceWithQuantity heldResource = ItemResourceWithQuantity.fromStack(slotStack);
        int consumed = 0;
        try
        {
            consumed = LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                return (int) storage.add(heldResource.resource(), slotStack.getCount(), false, null);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Error in open container item handling", e);
        }
        
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
       
        
        // Code that calls this is crazy and not clear how the return value 
        // is used except that it is used to terminate a for loop and
        // returning an empty stack is one way to ensure it terminates
        return ItemStack.EMPTY;
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener)
    {
        super.addListener(listener);
        if(listener instanceof EntityPlayerMP && this.te != null)
        {
            AbstractMachine machine = ((MachineTileEntity)this.te).machine();
            
            if(machine != null && machine.hasItemStorage())
            {
                ItemStorageListener newItemListener =
                        new ItemStorageListener(
                                ((MachineTileEntity)this.te).machine().itemStorage(), 
                                (EntityPlayerMP)listener);
                
                
                assert this.storageListeners.put((EntityPlayerMP)listener, newItemListener) == null
                        : "Found existing storage listener for player on container";
                
                LogisticsService.ITEM_SERVICE.initializeListener(newItemListener);
            }
        }
    }

    @Override
    public void onContainerClosed(@Nonnull EntityPlayer playerIn)
    {
        ItemStorageListener oldListener = this.storageListeners.remove(playerIn);
        if(oldListener != null) oldListener.die();
        super.onContainerClosed(playerIn);
    }
    
    
}
