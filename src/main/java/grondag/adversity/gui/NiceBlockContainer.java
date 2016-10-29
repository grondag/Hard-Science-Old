package grondag.adversity.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class NiceBlockContainer extends Container
{
    protected final InventoryPlayer inventoryPlayer;
    
    protected final int usageSlot;
    protected final ItemStack usedItemStack;

    public NiceBlockContainer(InventoryPlayer inventoryplayer, EnumHand hand) 
    {        
        this.inventoryPlayer = inventoryplayer;
        
        this.usageSlot = hand == EnumHand.MAIN_HAND ? inventoryplayer.currentItem : inventoryplayer.getSizeInventory() - 1;
        this.usedItemStack = inventoryplayer.getStackInSlot(usageSlot);
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return false;
    }

}
