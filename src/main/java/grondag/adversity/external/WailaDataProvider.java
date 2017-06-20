package grondag.adversity.external;


import java.util.List;

import grondag.adversity.Log;
import grondag.adversity.superblock.block.SuperBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "waila")
public class WailaDataProvider implements IWailaDataProvider
{
    public static final WailaDataProvider INSTANCE = new WailaDataProvider();

    private WailaDataProvider() {}

    @Optional.Method(modid = "waila")
    public static void register(){
        if (registered)
            return;
        registered = true;
        FMLInterModComms.sendMessage("Waila", "register", "grondag.adversity.WailaDataProvider.load");
    }

    private static boolean registered;
    private static boolean loaded;

    @Optional.Method(modid = "waila")
    public static void load(IWailaRegistrar registrar) {
        if (!registered){
            Log.error("Unable to load Waila data provider.  Registration method not called prior to load.");
            return;
        }
        if (!loaded) {
//            registrar.registerHeadProvider(INSTANCE, NiceBlock.class);
            registrar.registerBodyProvider(INSTANCE, SuperBlock.class);
//            registrar.registerTailProvider(INSTANCE, NiceBlock.class);
            loaded = true;
        }
    }

    @Override
    @Optional.Method(modid = "waila")
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();
        if (block instanceof IWailaProvider) {
            return ((IWailaProvider) block).getWailaBody(itemStack, currenttip, accessor, config);
        }
        return currenttip;
    }

    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    @Optional.Method(modid = "waila")
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        return tag;
    }
}