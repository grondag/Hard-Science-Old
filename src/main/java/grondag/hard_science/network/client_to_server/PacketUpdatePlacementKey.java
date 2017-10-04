package grondag.hard_science.network.client_to_server;


import grondag.hard_science.network.AbstractPlayerToServerPacket;
import grondag.hard_science.player.ModPlayerCaps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

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
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.isKeyPressed = pBuff.readBoolean();
    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeBoolean(this.isKeyPressed);
    }

    @Override
    protected void handle(PacketUpdatePlacementKey message, EntityPlayerMP player)
    {
        ModPlayerCaps.setPlacementModifierOn(player, message.isKeyPressed);
    }
}
