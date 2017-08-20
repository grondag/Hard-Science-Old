package grondag.hard_science.network;

import grondag.hard_science.init.ModBlocks;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Necessary because virtual blocks report that they are air, server side
 * and so server will not apply normal destruction logic.
 */
public class PacketDestroyVirtualBlock extends AbstractPlayerToServerPacket<PacketDestroyVirtualBlock>
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

    @Override
    protected void handle(PacketDestroyVirtualBlock message, EntityPlayerMP player)
    {
        World world = player.getEntityWorld();
        if(world.getBlockState(message.blockPos).getBlock() == ModBlocks.virtual_block)
        {
            world.setBlockToAir(message.blockPos);
        }
    }

}
