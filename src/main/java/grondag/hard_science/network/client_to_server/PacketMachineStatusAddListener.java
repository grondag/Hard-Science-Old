package grondag.hard_science.network.client_to_server;

import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.network.AbstractPlayerToServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketMachineStatusAddListener extends AbstractPlayerToServerPacket<PacketMachineStatusAddListener> 
{
    public BlockPos pos;

    public PacketMachineStatusAddListener() {}
    
    public PacketMachineStatusAddListener(BlockPos pos)
    {
        this.pos = pos;
    }
    
    @Override
    protected void handle(PacketMachineStatusAddListener message, EntityPlayerMP player)
    {
        if(player.world.isBlockLoaded(message.pos))
        {
            TileEntity te = player.world.getTileEntity(message.pos);
            if(te != null && te instanceof MachineTileEntity)
            {
                ((MachineTileEntity)te).addPlayerListener(player);
            }
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.pos = pBuff.readBlockPos();
    }


    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(pos);
    }

}
