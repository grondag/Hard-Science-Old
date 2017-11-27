package grondag.hard_science.simulator.storage;

import grondag.hard_science.machines.support.OpenContainerStorageProxy;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenContainerPacketHandler implements IMessageHandler<ItemResourceWithQuantity, IMessage> 
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