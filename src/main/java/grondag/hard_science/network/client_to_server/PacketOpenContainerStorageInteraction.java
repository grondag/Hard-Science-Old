package grondag.hard_science.network.client_to_server;

import javax.annotation.Nonnull;

import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineContainerTileEntity;
import grondag.hard_science.machines.base.MachineStorageTileEntity;
import grondag.hard_science.network.AbstractPlayerToServerPacket;
import grondag.hard_science.simulator.resource.EnumStorageType;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
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
    private StorageType<?> storageType;
    private IResource<?> target;
    
    public PacketOpenContainerStorageInteraction(@Nonnull Action action, @Nonnull IResource<?> target)
    {
        this.action = action;
        this.target = target;
        this.storageType = target.storageType();
    }
    
    public PacketOpenContainerStorageInteraction() {}
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.action = pBuff.readEnumValue(Action.class);
        this.storageType = StorageType.fromEnum(EnumStorageType.values()[pBuff.readByte()]);
        this.target = this.storageType.fromPacket(pBuff);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this.action);
        pBuff.writeByte(this.storageType.ordinal);
        this.storageType.toPacket(pBuff, this.target);
    }

    public Action getAction()
    {
        return action;
    }

    public StorageType<?> getStorageType()
    {
        return this.storageType;
    }
    
    public IResource<?> getTarget()
    {
        return target;
    }

    @Override
    protected void handle(PacketOpenContainerStorageInteraction message, EntityPlayerMP player)
    {
        MachineContainerTileEntity te = MachineContainer.getOpenContainerTileEntity(player);
        if(te == null || !(te instanceof MachineStorageTileEntity)) return;
        
        IStorage<StorageTypeStack> storage =  ((MachineStorageTileEntity)te).getStorage();
        if(storage == null) return;

        if(storage.storageType() != StorageType.ITEM) return;
        
        ItemResource targetResource = (ItemResource) message.target;
        if(targetResource == null) targetResource = (ItemResource) StorageType.ITEM.emptyResource;
        
        switch(message.action)
        {
            case PUT_ALL_HELD:
                this.doPut(false, player, storage);
                return;
            
            case PUT_ONE_HELD:
                this.doPut(true, player, storage);
                return;
            
            case QUICK_MOVE_HALF:
            {
                int toMove = Math.max(1, (int) Math.min(targetResource.sampleItemStack().getMaxStackSize() / 2, storage.getQuantityStored(targetResource) / 2));
                this.doQuickMove(toMove, player, targetResource, storage);
                return;
            }
                
            case QUICK_MOVE_ONE:
                this.doQuickMove(1, player, targetResource, storage);
                return;

            case QUICK_MOVE_STACK:
            {
                int toMove = (int) Math.min(targetResource.sampleItemStack().getMaxStackSize(), storage.getQuantityStored(targetResource));
                this.doQuickMove(toMove, player, targetResource, storage);
                return;
            }

            case TAKE_ONE:
                this.doTake(1, player, targetResource, storage);
                return;
           
            case TAKE_HALF:
            {
                int toTake = Math.max(1, (int) Math.min(targetResource.sampleItemStack().getMaxStackSize() / 2, storage.getQuantityStored(targetResource) / 2));
                this.doTake(toTake, player, targetResource, storage);
                return;
            }

            case TAKE_STACK:
            {
                int toTake = (int) Math.min(targetResource.sampleItemStack().getMaxStackSize(), storage.getQuantityStored(targetResource));
                this.doTake(toTake, player, targetResource, storage);
                return;
            }
            
            default:
                return;
           
        }
    }
    
    private void doPut(boolean single, EntityPlayerMP player, IStorage<StorageTypeStack> storage)
    {
        ItemStack heldStack = player.inventory.getItemStack();
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            ItemResourceWithQuantity heldResource = ItemResourceWithQuantity.fromStack(heldStack);
            int added = (int) storage.add(heldResource.resource(), single ? 1 : heldStack.getCount(), false);
            if(added > 0) heldStack.shrink(added);
            player.updateHeldItem();
        }
        return;
    }
    
    private void doQuickMove(int howMany, EntityPlayerMP player, ItemResource targetResource, IStorage<StorageTypeStack> storage)
    {
        if(howMany == 0) return;
        int toMove = (int) storage.takeUpTo(targetResource, howMany, false);
        if(toMove == 0) return;
        ItemStack newStack = targetResource.sampleItemStack().copy();
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
    private void doTake(int howMany, EntityPlayerMP player, ItemResource targetResource, IStorage<StorageTypeStack> storage)
    {
        if(howMany == 0) return;

        ItemStack heldStack = player.inventory.getItemStack();
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            ItemResourceWithQuantity heldResource = ItemResourceWithQuantity.fromStack(heldStack);
            boolean heldStackMatchesTarget = targetResource.isResourceEqual(heldResource.resource());
            if(!heldStackMatchesTarget) return;
            if(heldStack.getCount() >= heldStack.getMaxStackSize()) return;
            int toAdd = (int) storage.takeUpTo(targetResource, howMany, false);
            if(toAdd == 0) return;
            heldStack.grow(toAdd);
            player.updateHeldItem();
        }
        else
        {
            int toAdd = (int) storage.takeUpTo(targetResource, howMany, false);
            if(toAdd == 0) return;
            ItemStack newStack = targetResource.sampleItemStack();
            newStack.setCount(toAdd);
            player.inventory.setItemStack(newStack);
            player.updateHeldItem();
        }
    }
}
