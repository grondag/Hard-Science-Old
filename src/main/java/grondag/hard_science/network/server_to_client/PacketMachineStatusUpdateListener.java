package grondag.hard_science.network.server_to_client;

import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.DeviceEnergyInfo;
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
    public DeviceEnergyInfo powerSupplyInfo;
    public String machineName;
    
    public PacketMachineStatusUpdateListener() {}
    
    public PacketMachineStatusUpdateListener(MachineTileEntity te)
    {
        this.pos = te.getPos();
        this.controlState = te.machine().getControlState();
        this.statusState = te.machine().getStatusState();
        
        if(this.controlState.hasMaterialBuffer()) 
            this.materialBufferData = te.machine().getBufferManager().serializeToArray();
        
        if(this.controlState.hasPowerSupply())
        {
            this.powerSupplyInfo = new DeviceEnergyInfo(te.machine().energyManager());
        }
        
        this.machineName = te.machine().machineName();
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
        if(this.controlState.hasPowerSupply()) 
        {
            this.powerSupplyInfo = new DeviceEnergyInfo();
            this.powerSupplyInfo.fromBytes(pBuff);
        }
        this.machineName = pBuff.readString(8);
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

        if(this.controlState.hasPowerSupply())
        {
            this.powerSupplyInfo.toBytes(pBuff);
        }
        
        pBuff.writeString(this.machineName);
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
