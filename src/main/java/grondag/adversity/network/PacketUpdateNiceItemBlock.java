package grondag.adversity.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import grondag.adversity.niceblock.base.NiceItemBlock;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketUpdateNiceItemBlock implements IMessage
{
    
    private int colorMapID;

    public PacketUpdateNiceItemBlock() 
    {
    }
    
    public PacketUpdateNiceItemBlock(int colorMapID) 
    {
        this.colorMapID = colorMapID;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        colorMapID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {
        buf.writeInt(colorMapID);
    }

    public static class Handler implements IMessageHandler<PacketUpdateNiceItemBlock, IMessage> 
    {
        @Override
        public IMessage onMessage(PacketUpdateNiceItemBlock message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateNiceItemBlock message, MessageContext ctx) 
        {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null || !(heldItem.getItem() instanceof NiceItemBlock) ) 
            {
                return;
            }
            NiceItemBlock niceItem = (NiceItemBlock)heldItem.getItem();
            niceItem.setColorMapID(heldItem, message.colorMapID);
        }
    }
}
