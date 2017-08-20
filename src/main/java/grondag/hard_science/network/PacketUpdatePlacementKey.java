package grondag.hard_science.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import grondag.hard_science.player.ModPlayerCaps;

/**
 * Keeps server in synch with user keypress for placement modifier, if it is other than sneak.
 */
public class PacketUpdatePlacementKey extends AbstractPlayerToServerPacket<PacketUpdatePlacementKey>
{
    
    private boolean isKeyPressed;

    public PacketUpdatePlacementKey() 
    {
    }
    
    public PacketUpdatePlacementKey(boolean isKeyPressed) 
    {
        this.isKeyPressed = isKeyPressed;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        this.isKeyPressed = pBuff.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
        pBuff.writeBoolean(this.isKeyPressed);
    }

    @Override
    protected void handle(PacketUpdatePlacementKey message, EntityPlayerMP player)
    {
        ModPlayerCaps.setPlacementModifierOn(player, message.isKeyPressed);
    }
}
