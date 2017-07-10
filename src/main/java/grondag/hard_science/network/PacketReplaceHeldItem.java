package grondag.hard_science.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketReplaceHeldItem implements IMessage
{
    
    private ItemStack stack;

    public PacketReplaceHeldItem() 
    {
    }
    
    public PacketReplaceHeldItem(ItemStack stack) 
    {
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        try
        {
            this.stack = pBuff.readItemStack();
        }
        catch (IOException e)
        {
            this.stack = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        pBuff.writeItemStack(this.stack);
    }

    public static class Handler implements IMessageHandler<PacketReplaceHeldItem, IMessage> 
    {
        @Override
        public IMessage onMessage(PacketReplaceHeldItem message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketReplaceHeldItem message, MessageContext ctx) 
        {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            playerEntity.setHeldItem(EnumHand.MAIN_HAND, message.stack);
        }
    }
}
