package grondag.hard_science.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class AbstractPlayerToServerPacket<T extends IMessage> implements IMessageHandler<T, IMessage>, IMessage
{

    @Override
    public IMessage onMessage(final T message, MessageContext context) {
        final EntityPlayerMP player = context.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> handle(message, player));

        return null;
    }

    protected abstract void handle(T message, EntityPlayerMP player);

}