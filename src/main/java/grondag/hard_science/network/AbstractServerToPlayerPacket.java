package grondag.hard_science.network;

import grondag.hard_science.simulator.wip.IMessagePlus;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class AbstractServerToPlayerPacket<T extends IMessagePlus> implements IMessageHandler<T, IMessage>, IMessagePlus
{
    @Override
    public IMessage onMessage(final T message, MessageContext context) 
    {
        FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> handle(message, context));
        return null;
    }

    protected abstract void handle(T message, MessageContext context);
}
