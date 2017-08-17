package grondag.hard_science.simulator.wip;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ItemResourceWithQuantity extends AbstractResourceWithQuantity<StorageType.StorageTypeStack>
{

    public ItemResourceWithQuantity(@Nonnull ItemResource resource, long quantity)
    {
        super(resource, quantity);
    }
    
    public ItemResourceWithQuantity()
    {
        super();
    }
    
    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }
    
    public static class OpenContainerPacketHandler implements IMessageHandler<ItemResourceWithQuantity, IMessage> 
    {
        @Override
        public IMessage onMessage(ItemResourceWithQuantity message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(ItemResourceWithQuantity message, MessageContext ctx) 
        {
            OpenContainerStorageProxy.ITEM_PROXY.handleStorageUpdate(null, message);
        }
    }
}
