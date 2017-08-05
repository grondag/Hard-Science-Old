package grondag.hard_science.network;

import grondag.hard_science.init.ModBlocks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Necessary because virtual blocks report that they are air, server side
 * and so server will not apply normal destruction logic.
 */
public class PacketDestroyVirtualBlock implements IMessage
{
    private BlockPos blockPos;

    public PacketDestroyVirtualBlock() 
    {
    }
    
    public PacketDestroyVirtualBlock(BlockPos pos) 
    {
        this.blockPos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        this.blockPos = pBuff.readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        pBuff.writeBlockPos(this.blockPos);
    }

    public static class Handler implements IMessageHandler<PacketDestroyVirtualBlock, IMessage> 
    {
        @Override
        public IMessage onMessage(PacketDestroyVirtualBlock message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketDestroyVirtualBlock message, MessageContext ctx) 
        {
            World world = ctx.getServerHandler().player.getEntityWorld();
            if(world.getBlockState(message.blockPos).getBlock() == ModBlocks.virtual_block)
            {
                world.setBlockToAir(message.blockPos);
            }
        }
    }

}
