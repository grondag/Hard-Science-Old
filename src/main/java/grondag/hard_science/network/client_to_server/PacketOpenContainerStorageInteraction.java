package grondag.hard_science.network.client_to_server;

import javax.annotation.Nonnull;

import grondag.exotic_matter.network.AbstractPlayerToServerPacket;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MachineStorageContainer;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.storage.ItemStorageListener;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Sent when player interacts with the GUI of an IStorage (vs container slots).
 * IStorage has no concept of slots.
 */
public class PacketOpenContainerStorageInteraction extends AbstractPlayerToServerPacket<PacketOpenContainerStorageInteraction> 
{
    public static enum Action
    {
        /** move targeted stack to player's inventory */
        QUICK_MOVE_STACK,
        
        /** move half of targeted item, up to half a stack, to player's inventory */
        QUICK_MOVE_HALF,
        
        /** move one of targeted item to player's inventory */
        QUICK_MOVE_ONE,
        
        /** if player has an empty hand or holds the target item, add one to held */
        TAKE_ONE,
        
        /** if player has an empty hand, take half of targeted item, up to half a stack*/
        TAKE_HALF,
        
        /** if player has an empty hand, take full stack of targeted item */
        TAKE_STACK,
        
        /** if player holds a stack, deposit one of it into storage. target is ignored/can be null */
        PUT_ONE_HELD,
        
        /** if player holds a stack, deposit all of it into storage. target is ignored/can be null */
        PUT_ALL_HELD
    }
    
    private Action action;
    private int resourceHandle;
    
    public PacketOpenContainerStorageInteraction(@Nonnull Action action, @Nonnull ItemResourceDelegate target)
    {
        this.action = action;
        this.resourceHandle = target.handle();
    }
    
    public PacketOpenContainerStorageInteraction() {}
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.action = pBuff.readEnumValue(Action.class);
        this.resourceHandle = pBuff.readInt();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this.action);
        pBuff.writeInt(this.resourceHandle);
    }

    public Action getAction()
    {
        return action;
    }


    @Override
    protected void handle(PacketOpenContainerStorageInteraction message, EntityPlayerMP player)
    {
        MachineTileEntity te = MachineContainer.getOpenContainerTileEntity(player);
        if(te == null || !(te instanceof MachineTileEntity)) return;
        
        if(!(player.openContainer instanceof MachineStorageContainer)) return;
        
        MachineStorageContainer container = (MachineStorageContainer)player.openContainer;
                
        ItemStorageListener listener = container.getItemListener(player);
        
        if(listener == null || listener.isDead()) return;
        
        ItemResource targetResource = (ItemResource) listener.getResourceForHandle(message.resourceHandle);
        
        switch(message.action)
        {
            case PUT_ALL_HELD:
                this.doPut(false, player, listener);
                return;
            
            case PUT_ONE_HELD:
                this.doPut(true, player, listener);
                return;
            
            case QUICK_MOVE_HALF:
            {
                if(targetResource == null) return;
                int toMove = Math.max(1, (int) Math.min(targetResource.sampleItemStack().getMaxStackSize() / 2, listener.getQuantityStored(targetResource) / 2));
                this.doQuickMove(toMove, player, targetResource, listener);
                return;
            }
                
            case QUICK_MOVE_ONE:
                if(targetResource == null) return;
                this.doQuickMove(1, player, targetResource, listener);
                return;

            case QUICK_MOVE_STACK:
            {
                if(targetResource == null) return;
                int toMove = (int) Math.min(targetResource.sampleItemStack().getMaxStackSize(), listener.getQuantityStored(targetResource));
                this.doQuickMove(toMove, player, targetResource, listener);
                return;
            }

            case TAKE_ONE:
                this.doTake(1, player, targetResource, listener);
                return;
           
            case TAKE_HALF:
            {
                if(targetResource == null) return;
                int toTake = Math.max(1, (int) Math.min(targetResource.sampleItemStack().getMaxStackSize() / 2, listener.getQuantityStored(targetResource) / 2));
                this.doTake(toTake, player, targetResource, listener);
                return;
            }

            case TAKE_STACK:
            {
                if(targetResource == null) return;
                int toTake = (int) Math.min(targetResource.sampleItemStack().getMaxStackSize(), listener.getQuantityStored(targetResource));
                this.doTake(toTake, player, targetResource, listener);
                return;
            }
            
            default:
                return;
           
        }
    }
    
    private void doPut(boolean single, EntityPlayerMP player, ItemStorageListener listener)
    {
        ItemStack heldStack = player.inventory.getItemStack();
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            ItemResourceWithQuantity heldResource = ItemResourceWithQuantity.fromStack(heldStack);
            int added = 0;
            try
            {
                added = LogisticsService.ITEM_SERVICE.executor.submit( () ->
                {
                    return (int) listener.add(heldResource.resource(), single ? 1 : heldStack.getCount(), false, null);
                }, true).get();
            }
            catch (Exception e)
            {
                HardScience.INSTANCE.error("Error in open container item handling", e);
            }
            if(added > 0) heldStack.shrink(added);
            player.updateHeldItem();
        }
        return;        
    }

    private void doQuickMove(int howMany, EntityPlayerMP player, ItemResource targetResource, ItemStorageListener listener)
    {
        if(howMany == 0) return;
        int toMove = 0;
        try
        {
            toMove = LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                return (int) listener.takeUpTo(targetResource, howMany, false, null);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Error in open container item handling", e);
        }
        
        if(toMove == 0) return;
        ItemStack newStack = targetResource.sampleItemStack();
        newStack.setCount(toMove);
        player.inventory.addItemStackToInventory(newStack);
        if(!newStack.isEmpty())
        {
            InventoryHelper.spawnItemStack(player.world, player.posX, player.posY, player.posZ, newStack);;
        }
    }
    
    /**
     * Note: assumes player held item is empty and does not check for this.
     */
    private void doTake(int howMany, EntityPlayerMP player, ItemResource targetResource, ItemStorageListener listener)
    {
        if(howMany == 0) return;

        ItemStack heldStack = player.inventory.getItemStack();
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            boolean heldStackMatchesTarget = targetResource.isStackEqual(heldStack);
            if(!heldStackMatchesTarget) return;
            if(heldStack.getCount() >= heldStack.getMaxStackSize()) return;
            howMany = Math.min(howMany, heldStack.getMaxStackSize() - heldStack.getCount());
        }
        else
        {
            howMany = Math.min(howMany, targetResource.sampleItemStack().getMaxStackSize());
        }
        
        final int finalHowMany = howMany;
        
        int toAdd = 0;
        try
        {
            toAdd = LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                return (int) listener.takeUpTo(targetResource, finalHowMany, false, null);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Error in open container item handling", e);
        }
            
        if(toAdd == 0) return;
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            heldStack.grow(toAdd);
        }
        else
        {
            ItemStack newStack = targetResource.sampleItemStack();
            newStack.setCount(toAdd);
            player.inventory.setItemStack(newStack);
        }
        player.updateHeldItem();
    }
}
