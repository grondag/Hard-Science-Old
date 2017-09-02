package grondag.hard_science.network.client_to_server;

import javax.annotation.Nonnull;

import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.network.AbstractPlayerToServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketMachineInteraction extends AbstractPlayerToServerPacket<PacketMachineInteraction>
{
    public static enum Action
    {
        TOGGLE_POWER
    }
    
    private BlockPos blockPos;
    private Action action;

    public PacketMachineInteraction() 
    {
    }
    
    public PacketMachineInteraction(@Nonnull Action action, @Nonnull BlockPos pos) 
    {
        this.action = action;
        this.blockPos = pos;
    }
   
    @Override
    protected void handle(PacketMachineInteraction message, EntityPlayerMP player)
    {
        if(player == null) return;
        
        World world = player.getEntityWorld();
        
        if(!world.isBlockLoaded(message.blockPos)) return;
                
        TileEntity te = player.world.getTileEntity(message.blockPos);
        if(te != null && te instanceof MachineTileEntity)
        {
            MachineTileEntity mte  = (MachineTileEntity)te;
            
            switch(message.action)
            {
            
            case TOGGLE_POWER:
                mte.togglePower(player);
                return;
                
            default:
                return;
            
            }
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.action = pBuff.readEnumValue(Action.class);
        this.blockPos = pBuff.readBlockPos();
        
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this.action);
        pBuff.writeBlockPos(this.blockPos);
    }
    
    public Action getAction()
    {
        return action;
    }
    
    public BlockPos getPos()
    {
        return this.blockPos;
    }
}