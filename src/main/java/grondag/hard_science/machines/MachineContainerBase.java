package grondag.hard_science.machines;


import grondag.hard_science.CommonEventHandler;
import grondag.hard_science.simulator.wip.OpenContainerStorageListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class MachineContainerBase extends Container
{
    private MachineContainerTEBase te;
    
    public final ContainerLayout layout;


    public MachineContainerBase(IInventory playerInventory, MachineContainerTEBase te, ContainerLayout layout) 
    {
        this.te = te;
        this.layout = layout;

        // This container references items out of our own inventory (the 9 slots we hold ourselves)
        // as well as the slots from the player inventory so that the user can transfer items between
        // both inventories. The two calls below make sure that slots are defined for both inventories.
        addMachineSlots();
        addPlayerSlots(playerInventory);
    }

    protected void addPlayerSlots(IInventory playerInventory) 
    {
        // Slots for the hotbar - start at slot 0
        for (int col = 0; col < 9; ++col) 
        {
            int x = layout.playerInventoryLeft + col * layout.slotSpacing;
            int y = layout.playerInventoryTop + layout.slotSpacing * 2 + 16 + layout.externalMargin;
            this.addSlotToContainer(new Slot(playerInventory, col, x, y));
        }

        // Slots for the main inventory - start at slot 9;
        for (int row = 0; row < 3; ++row) 
        {
            for (int col = 0; col < 9; ++col) 
            {
                int x = layout.playerInventoryLeft + col * layout.slotSpacing;
                int y = row * layout.slotSpacing + layout.playerInventoryTop;
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

    }

    protected void addMachineSlots() 
    {
        
//        IItemHandler itemHandler = this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//        int x = 10;
//        int y = 6;
//
//        // Add our own slots
//        int slotIndex = 0;
//        for (int i = 0; i < itemHandler.getSlots(); i++) {
//            addSlotToContainer(new SlotItemHandler(itemHandler, slotIndex, x, y));
//            slotIndex++;
//            x += 18;
//        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < MachineContainerTEBase.SIZE) {
                if (!this.mergeItemStack(itemstack1, MachineContainerTEBase.SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, MachineContainerTEBase.SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te.canInteractWith(playerIn);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        if(listener instanceof EntityPlayerMP)
        {
            CommonEventHandler.TEST_STORE.addListener(new OpenContainerStorageListener.ItemListener((EntityPlayerMP)listener));
        }
    }
    
    
}