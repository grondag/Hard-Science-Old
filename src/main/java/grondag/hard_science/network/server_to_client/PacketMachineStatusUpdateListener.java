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
    public long[] materialBufferData;
    public MachineStatusState statusState;
    public int[] powerProviderData;
    
    public PacketMachineStatusUpdateListener() {}
   
    
    public PacketMachineStatusUpdateListener(MachineTileEntity te)
    {
        this.pos = te.getPos();
        this.controlState = te.getControlState();
        this.statusState = te.getStatusState();
        
        if(this.controlState.hasMaterialBuffer()) 
            this.materialBufferData = te.getBufferManager().serializeToArray();
        
        if(this.controlState.hasPowerProvider()) 
            this.powerProviderData = te.getPowerProvider().serializeToArray();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.pos = pBuff.readBlockPos();
        this.controlState = new MachineControlState();
        this.controlState.fromBytes(pBuff);
        this.statusState = new MachineStatusState();
        this.statusState.fromBytes(pBuff);
        
        if(this.controlState.hasMaterialBuffer()) 
        {
            int count = pBuff.readByte();
            this.materialBufferData = new long[count];
            for(int i = 0; i < count; i++)
            {
                this.materialBufferData[i] = pBuff.readVarLong();
            }
        }
        if(this.controlState.hasPowerProvider()) 
            this.powerProviderData = pBuff.readVarIntArray();
        
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(pos);
        this.controlState.toBytes(pBuff);
        this.statusState.toBytes(pBuff);

        if(this.controlState.hasMaterialBuffer()) 
        {
            int count = this.materialBufferData.length;
            pBuff.writeByte(count);
            for(int i = 0; i < count; i++)
            {
                pBuff.writeVarLong(this.materialBufferData[i]);
            }
        }

        if(this.controlState.hasPowerProvider()) 
            pBuff.writeVarIntArray(this.powerProviderData);
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
