package grondag.hard_science.network.server_to_client;

import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.network.AbstractServerToPlayerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMachineStatusUpdateListener extends AbstractServerToPlayerPacket<PacketMachineStatusUpdateListener>
{
    public BlockPos pos;
    public MaterialBufferManager materialBuffers;
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.pos = pBuff.readBlockPos();
        if(pBuff.readBoolean())
        {
            this.materialBuffers = new MaterialBufferManager();
            this.materialBuffers.fromBytes(pBuff);
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(pos);
        if(this.materialBuffers == null) pBuff.writeBoolean(false);
        else
        {
            pBuff.writeBoolean(true);
            this.materialBuffers.toBytes(pBuff);
        }
    }

    @Override
    protected void handle(PacketMachineStatusUpdateListener message, MessageContext context)
    {
        TileEntity te = Minecraft.getMinecraft().player.world.getTileEntity(message.pos);
        if(te != null && te instanceof MachineTileEntity)
        {
            ((MachineTileEntity)te).handleMachineStatusUpdate(message);
        }
    }

}
