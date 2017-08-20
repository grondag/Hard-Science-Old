package grondag.hard_science.machines;


import grondag.hard_science.simulator.wip.IStorage;
import grondag.hard_science.simulator.wip.ItemResourceWithQuantity;
import grondag.hard_science.simulator.wip.OpenContainerStorageListener;
import grondag.hard_science.simulator.wip.StorageType;
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

    public static IStorage<StorageType.StorageTypeStack> getOpenContainerStackStorage(EntityPlayerMP player)
    {
        Container container = player.openContainer;

        if (container == null ||  !(container instanceof MachineContainerBase)) return null;
        
        MachineContainerTEBase openTE = ((MachineContainerBase) container).tileEntity();
        
        return openTE == null ? null : openTE.storage();
    }

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
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();
        
            if(playerIn instanceof EntityPlayerMP)
            {
                IStorage<StorageType.StorageTypeStack> storage = this.te.storage();
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
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te.canInteractWith(playerIn);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        if(listener instanceof EntityPlayerMP && this.te != null && this.te.storage() != null)
        {
            this.te.storage().addListener(new OpenContainerStorageListener.ItemListener((EntityPlayerMP)listener));
        }
    }
    
    public MachineContainerTEBase tileEntity()
    {
        return this.te;
    }
}