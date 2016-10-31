package grondag.adversity.network;

import mcjty.lib.network.PacketHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AdversityMessages
{
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) 
    {
        INSTANCE = net;

        // Server side
//        net.registerMessage(PacketCrafter.Handler.class, PacketCrafter.class, PacketHandler.nextID(), Side.SERVER);
        

        // Client side
//        net.registerMessage(PacketPlayersReady.Handler.class, PacketPlayersReady.class, PacketHandler.nextID(), Side.CLIENT);
        

//        PacketHandler.register(PacketHandler.nextPacketID(), StorageInfoPacketServer.class, StorageInfoPacketClient.class);


    }
}
