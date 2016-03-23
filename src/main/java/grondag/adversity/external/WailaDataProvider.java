package grondag.adversity.external;
//package grondag.adversity;
//
//import grondag.adversity.niceblock.newmodel.NiceBlock;
//
//import java.util.List;
//
//import net.minecraft.block.Block;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.BlockPos;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.common.event.FMLInterModComms;
//import mcp.mobius.waila.api.IWailaConfigHandler;
//import mcp.mobius.waila.api.IWailaDataAccessor;
//import mcp.mobius.waila.api.IWailaDataProvider;
//import mcp.mobius.waila.api.IWailaRegistrar;
//
//public class WailaDataProvider implements IWailaDataProvider
//{
//    public static final WailaDataProvider INSTANCE = new WailaDataProvider();
//
//    private WailaDataProvider() {}
//
//    public static void register(){
//        if (registered)
//            return;
//        registered = true;
//        FMLInterModComms.sendMessage("Waila", "register", "grondag.adversity.WailaDataProvider.load");
//    }
//
//    private static boolean registered;
//    private static boolean loaded;
//
//    public static void load(IWailaRegistrar registrar) {
//        if (!registered){
//            Adversity.log.error("Unable to load Waila data provider.  Registration method not called prior to load.");
//            return;
//        }
//        if (!loaded) {
////            registrar.registerHeadProvider(INSTANCE, NiceBlock.class);
//            registrar.registerBodyProvider(INSTANCE, NiceBlock.class);
////            registrar.registerTailProvider(INSTANCE, NiceBlock.class);
//            loaded = true;
//        }
//    }
//
//    @Override
//    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return null;
//    }
//
//    @Override
//    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return currenttip;
//    }
//
//    @Override
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        Block block = accessor.getBlock();
//        if (block instanceof IWailaProvider) {
//            return ((IWailaProvider) block).getWailaBody(itemStack, currenttip, accessor, config);
//        }
//        return currenttip;
//    }
//
//    @Override
//    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return currenttip;
//    }
//
//    @Override
//    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
//    {
//        return tag;
//    }
//
//}