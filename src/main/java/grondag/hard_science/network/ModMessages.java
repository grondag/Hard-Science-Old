package grondag.hard_science.network;

import grondag.hard_science.HardScience;
import grondag.hard_science.network.client_to_server.PacketDestroyVirtualBlock;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction;
import grondag.hard_science.network.client_to_server.PacketMachineStatusAddListener;
import grondag.hard_science.network.client_to_server.PacketOpenContainerStorageInteraction;
import grondag.hard_science.network.client_to_server.ConfigurePlacementItem;
import grondag.hard_science.network.client_to_server.PacketUpdateModifierKeys;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderUpdate;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderRefresh;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.network.server_to_client.PacketOpenContainerItemStorageRefresh;
import grondag.hard_science.simulator.base.ItemResourceWithQuantity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModMessages
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(HardScience.MODID);
    private static int packetID = 0;

    public static void registerNetworkMessages() 
    {

        // Packed handled on Server side, sent from Client
        INSTANCE.registerMessage(ConfigurePlacementItem.class, ConfigurePlacementItem.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateModifierKeys.class, PacketUpdateModifierKeys.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketDestroyVirtualBlock.class, PacketDestroyVirtualBlock.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenContainerStorageInteraction.class, PacketOpenContainerStorageInteraction.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketMachineStatusAddListener.class, PacketMachineStatusAddListener.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(PacketMachineInteraction.class, PacketMachineInteraction.class, packetID++, Side.SERVER);
        
        // Packets handled on Client side, sent from Server        
        INSTANCE.registerMessage(PacketOpenContainerItemStorageRefresh.class, PacketOpenContainerItemStorageRefresh.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(ItemResourceWithQuantity.OpenContainerPacketHandler.class, ItemResourceWithQuantity.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketMachineStatusUpdateListener.class, PacketMachineStatusUpdateListener.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketExcavationRenderUpdate.class, PacketExcavationRenderUpdate.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketExcavationRenderRefresh.class, PacketExcavationRenderRefresh.class, packetID++, Side.CLIENT);
    }
    
    /**
     * Slightly more streamlined version of other routines already available.
     * Uses integer vs. double precision floating point arithmetic and consumes player list directly.
     * Also assumes range is pre-squared, probably a constant value.
     * Expected to be heavily used, thus the attempt to be the efficient.
     */
    public static void sendToPlayersNearPos(IMessage message, int dimension, BlockPos pos, int distanceSquared)
    {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
        {
            if (player.dimension == dimension)
            {
                int dx = pos.getX() - (int)player.posX;
                int dy = pos.getY() - (int)player.posY;
                int dz = pos.getZ() - (int)player.posZ;

                if (dx * dx + dy * dy + dz * dz < distanceSquared)
                {
                    INSTANCE.sendTo(message, player);
                }
            }
        }
    }
}
