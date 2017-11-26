package grondag.hard_science.machines;



import javax.annotation.Nullable;

import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineContainerTileEntity;
import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class BlockFabricatorContainer extends MachineContainer
{
 
    public BlockFabricatorContainer(IInventory playerInventory, MachineContainerTileEntity te, ContainerLayout layout) 
    {
        super(playerInventory, te, layout);
    }

    
    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) 
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();
        
            if(playerIn instanceof EntityPlayerMP)
            {
                
                IItemHandler itemHandler = ((MachineContainerTileEntity)this.te).getItemHandler();
                if(itemHandler == null) return ItemStack.EMPTY;
                
                ItemStack remainderStack = itemHandler.insertItem(0, slotStack, false);
                
                int consumed = slotStack.getCount() - remainderStack.getCount();
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
        
//        if(index == 0) return null;
//        
//        ItemStack stackCopy = null;
//        Slot slot = this.inventorySlots.get(index);
//
//        if (slot != null && slot.getHasStack()) 
//        {
//            ItemStack stack = slot.getStack();
//            stackCopy = stack.copy();
//
//            if (!this.mergeItemStack(stack, 0, 1, false)) return null;
//
//            if (stack.isEmpty()) 
//            {
//                slot.putStack(ItemStack.EMPTY);
//            } 
//            else 
//            {
//                slot.onSlotChanged();
//            }
//        }
//
//        return stackCopy;
    }
}