package grondag.adversity.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketUpdateSuperModelBlock implements IMessage
{
    
    private int meta;
    private ModelState modelState;

    public PacketUpdateSuperModelBlock() 
    {
    }
    
    public PacketUpdateSuperModelBlock(int meta, ModelState modelState) 
    {
        this.meta = meta;
        this.modelState = modelState;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        this.meta = buf.readInt();
        long b0 = buf.readLong();
        long b1 = buf.readLong();
        long b2 = buf.readLong();
        long b3 = buf.readLong();
        
        this.modelState = new ModelState(b0, b1, b2, b3);
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {
        buf.writeInt(this.meta);
        buf.writeLong(this.modelState.getBits0());
        buf.writeLong(this.modelState.getBits1());
        buf.writeLong(this.modelState.getBits2());
        buf.writeLong(this.modelState.getBits3());
    }

    public static class Handler implements IMessageHandler<PacketUpdateSuperModelBlock, IMessage> 
    {
        @Override
        public IMessage onMessage(PacketUpdateSuperModelBlock message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateSuperModelBlock message, MessageContext ctx) 
        {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null || !(heldItem.getItem() instanceof SuperItemBlock) ) 
            {
                return;
            }
            SuperItemBlock.setModelState(heldItem, message.modelState);
            heldItem.setItemDamage(message.meta);
        }
    }
}
