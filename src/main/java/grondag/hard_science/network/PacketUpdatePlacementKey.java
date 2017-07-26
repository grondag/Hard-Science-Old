package grondag.hard_science.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import grondag.hard_science.player.ModPlayerCaps;

/**
 * Keeps server in synch with user keypress for placement modifier, if it is other than sneak.
 */
public class PacketUpdatePlacementKey implements IMessage
{
    
    private boolean isKeyPressed;

    public PacketUpdatePlacementKey() 
    {
    }
    
    public PacketUpdatePlacementKey(boolean isKeyPressed) 
    {
        this.isKeyPressed = isKeyPressed;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        this.isKeyPressed = pBuff.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        pBuff.writeBoolean(this.isKeyPressed);
    }

    public static class Handler implements IMessageHandler<PacketUpdatePlacementKey, IMessage> 
    {
        @Override
        public IMessage onMessage(PacketUpdatePlacementKey message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdatePlacementKey message, MessageContext ctx) 
        {
            ModPlayerCaps.setPlacementModifierOn(ctx.getServerHandler().player, message.isKeyPressed);
        }
    }
}
