package grondag.hard_science.machines.support;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.server_to_client.PacketOpenContainerItemStorageRefresh;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IListenableStorage;
import grondag.hard_science.simulator.storage.IStorageListener;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

/**
 * Sends updates to the given player from the given storage, for as long
 * as the currently open container remains open with that player.
 */
public abstract class OpenContainerStorageListener<T extends StorageType<T>> implements IStorageListener<T>
{
    protected final Container container;
    protected final EntityPlayerMP player;

    public OpenContainerStorageListener(@Nonnull EntityPlayerMP player)
    {
        this.player = player;
        this.container = player.openContainer;
    }
 
    @Override
    public void handleStorageDisconnect(IListenableStorage<T> storage)
    {
        ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(Collections.emptyList(), storage.getCapacity()), player);
    }

    @Override
    public boolean isClosed()
    {
        return this.player.hasDisconnected() || this.player.openContainer != this.container;
    }
    
    public static class ItemListener extends OpenContainerStorageListener<StorageType.StorageTypeStack>
    {

        public ItemListener(EntityPlayerMP player)
        {
            super(player);
        }

        @Override
        public void handleStorageRefresh(IListenableStorage<StorageTypeStack> sender, List<AbstractResourceDelegate<StorageTypeStack>> update, long capacity)
        {
            ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(update, capacity), player);
        }

        @Override
        public void handleStorageUpdate(IListenableStorage<StorageTypeStack> sender, AbstractResourceDelegate<StorageTypeStack> update)
        {
            ModMessages.INSTANCE.sendTo(update, player);
        }
    }
}
