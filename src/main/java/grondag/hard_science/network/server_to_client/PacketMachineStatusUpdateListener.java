package grondag.hard_science.network.server_to_client;

import grondag.hard_science.machines.base.MachineControlState;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.network.AbstractServerToPlayerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMachineStatusUpdateListener extends AbstractServerToPlayerPacket<PacketMachineStatusUpdateListener>
{
    public BlockPos pos;
    public long controlStateBits;
    public int[] materialBufferData;
    
    public PacketMachineStatusUpdateListener() {}
   
    
    public PacketMachineStatusUpdateListener(BlockPos pos, MachineControlState controlState, int[] materialBufferData)
    {
        this.pos = pos;
        this.controlStateBits = controlState.serializeToBits();
        this.materialBufferData = materialBufferData;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.pos = pBuff.readBlockPos();
        this.controlStateBits = pBuff.readVarLong();
        if(pBuff.readBoolean())
        {
            this.materialBufferData = pBuff.readVarIntArray();
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(pos);
        pBuff.writeVarLong(this.controlStateBits);
        if(this.materialBufferData == null || this.materialBufferData.length == 0) 
        {
            pBuff.writeBoolean(false);
        }
        else
        {
            pBuff.writeBoolean(true);
            pBuff.writeVarIntArray(this.materialBufferData);
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
