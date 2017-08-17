package grondag.hard_science.simulator.wip;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.PacketOpenContainerItemStorageRefresh;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
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
    public void handleStorageDisconnect(IStorage<T> storage)
    {
        ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(Collections.emptyList()), player);
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
        public void handleStorageRefresh(IStorage<StorageTypeStack> sender, List<AbstractResourceWithQuantity<StorageTypeStack>> update)
        {
            ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(update), player);
        }

        @Override
        public void handleStorageUpdate(IStorage<StorageTypeStack> sender, AbstractResourceWithQuantity<StorageTypeStack> update)
        {
            ModMessages.INSTANCE.sendTo(update, player);
        }
    }
}
