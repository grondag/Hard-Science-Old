package grondag.hard_science.network.server_to_client;

import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MachineStatusState;
import grondag.hard_science.network.AbstractServerToPlayerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMachineStatusUpdateListener extends AbstractServerToPlayerPacket<PacketMachineStatusUpdateListener>
{
    public BlockPos pos;
    public MachineControlState controlState;
    public int[] materialBufferData;
    public MachineStatusState statusState;
    
    public PacketMachineStatusUpdateListener() {}
   
    
    public PacketMachineStatusUpdateListener(BlockPos pos, MachineControlState controlState, int[] materialBufferData, MachineStatusState statusState)
    {
        this.pos = pos;
        this.controlState = controlState;
        this.materialBufferData = materialBufferData;
        this.statusState = statusState;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.pos = pBuff.readBlockPos();
        this.controlState = new MachineControlState();
        this.controlState.fromBytes(pBuff);
        if(pBuff.readBoolean())
        {
            this.materialBufferData = pBuff.readVarIntArray();
        }
        this.statusState = new MachineStatusState();
        this.statusState.fromBytes(pBuff);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(pos);
        this.controlState.toBytes(pBuff);
        if(this.materialBufferData == null || this.materialBufferData.length == 0) 
        {
            pBuff.writeBoolean(false);
        }
        else
        {
            pBuff.writeBoolean(true);
            pBuff.writeVarIntArray(this.materialBufferData);
        }
        this.statusState.toBytes(pBuff);
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
