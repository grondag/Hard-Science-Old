package grondag.hard_science.network.client_to_server;


import grondag.exotic_matter.network.AbstractPlayerToServerPacket;
import grondag.hard_science.player.ModPlayerCaps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

/**
 * Keeps server in synch with user keypress for placement modifier, if it is other than sneak.
 */
public class PacketUpdateModifierKeys extends AbstractPlayerToServerPacket<PacketUpdateModifierKeys>
{
    private int keyFlags;

    public PacketUpdateModifierKeys() 
    {
    }
    
    public PacketUpdateModifierKeys(int keyFlags) 
    {
        this.keyFlags = keyFlags;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.keyFlags = pBuff.readByte();
    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeByte(this.keyFlags);
    }

    @Override
    protected void handle(PacketUpdateModifierKeys message, EntityPlayerMP player)
    {
        ModPlayerCaps.setPlacementModifierFlags(player, message.keyFlags);
    }
}
