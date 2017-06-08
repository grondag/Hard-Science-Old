package grondag.adversity.network;

import grondag.adversity.Adversity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AdversityMessages
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Adversity.MODID);
    private static int packetID = 0;

    public static void registerNetworkMessages() 
    {

        // Server side
        INSTANCE.registerMessage(PacketReplaceHeldItem.Handler.class, PacketReplaceHeldItem.class, packetID++, Side.SERVER);
        

        // Client side        

    }
}
