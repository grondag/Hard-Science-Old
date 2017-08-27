package grondag.hard_science.network.client_to_server;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.network.AbstractPlayerToServerPacket;
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
    protected void handle(PacketDestroyVirtualBlock message, EntityPlayerMP player)
    {
        World world = player.getEntityWorld();
        if(world.getBlockState(message.blockPos).getBlock() == ModBlocks.virtual_block)
        {
            world.setBlockToAir(message.blockPos);
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.blockPos = pBuff.readBlockPos();
        
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(this.blockPos);
    }

}