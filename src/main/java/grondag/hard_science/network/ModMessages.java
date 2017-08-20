package grondag.hard_science.network;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.wip.ItemResourceWithQuantity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModMessages
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(HardScience.MODID);
    private static int packetID = 0;

    public static void registerNetworkMessages() 
    {

        // Server side
        INSTANCE.registerMessage(PacketReplaceHeldItem.class, PacketReplaceHeldItem.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdatePlacementKey.class, PacketUpdatePlacementKey.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketDestroyVirtualBlock.class, PacketDestroyVirtualBlock.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenContainerStorageInteraction.class, PacketOpenContainerStorageInteraction.class, packetID++, Side.SERVER);
        
        
        // Client side        
        INSTANCE.registerMessage(PacketOpenContainerItemStorageRefresh.Handler.class, PacketOpenContainerItemStorageRefresh.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(ItemResourceWithQuantity.OpenContainerPacketHandler.class, ItemResourceWithQuantity.class, packetID++, Side.CLIENT);
    }
}
