package grondag.adversity;

import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;

import grondag.adversity.Configurator.Render.PreviewMode;
import grondag.adversity.feature.volcano.lava.LavaBlock;
import grondag.adversity.feature.volcano.lava.simulator.LavaSimulator;
import grondag.adversity.init.ModKeys;
import grondag.adversity.library.render.QuadCache;
import grondag.adversity.library.varia.Useful;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.superblock.placement.PlacementItem;
import grondag.adversity.superblock.varia.BlockHighlighter;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings({ "deprecation", "unused" })
public class CommonEventHandler 
{
    public static final CommonEventHandler INSTANCE = new CommonEventHandler();
    private static final String[] DENIALS;
    
    static
    {
        String[] denials = {"DENIED"};
        try
        {
            Gson g = new Gson();
            String json = I18n.translateToLocal("misc.denials");
            denials = g.fromJson(json, String[].class);
        }
        catch(Exception e)
        {
            Log.warn("Unable to parse localized denial messages. Using default.");
        }
        DENIALS = denials;
    }
    
    /**
     * Troll user if they attempt to put volcanic lava in a bucket.
     */
    @SubscribeEvent(priority = EventPriority.HIGH) 
    @SideOnly(Side.SERVER)
    public void onFillBucket(FillBucketEvent event)
    {
        if(event.getEntityPlayer() != null && event.getWorld().isRemote)
        {
            RayTraceResult target = event.getTarget();
            if(target != null && target.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                if(target.getBlockPos() != null)
                {
                    IBlockState state = event.getWorld().getBlockState(target.getBlockPos());
                    if(state.getBlock() instanceof LavaBlock)
                    {
                        event.getEntityPlayer().sendMessage(new TextComponentString(DENIALS[ThreadLocalRandom.current().nextInt(DENIALS.length)]));
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) 
    {
        if (event.getModID().equals(Adversity.MODID))
        {
            ConfigManager.sync(Adversity.MODID, Type.INSTANCE);
            Configurator.recalcDerived();
        }
    }
    
    /**
     * Check for blocks that need a custom block highlight and draw if found.
     * Adapted from the vanilla highlight code.
     */
    @SubscribeEvent
    public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) 
    {
        BlockHighlighter.handleDrawBlockHighlightEvent(event);
    }
    
    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void onBlockBreak(BlockEvent.BreakEvent event) 
    {
        // Lava blocks have their own handling
        if(!event.getWorld().isRemote && !(event.getState().getBlock() instanceof LavaBlock))
        {
            Simulator.INSTANCE.getFluidTracker().notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        // Lava blocks have their own handling
        if(!event.getWorld().isRemote && !(event.getState().getBlock() instanceof LavaBlock))
        {
            Simulator.INSTANCE.getFluidTracker().notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        if(event.getWorld().isRemote) return;
        
        LavaSimulator sim = Simulator.INSTANCE.getFluidTracker();
        for(BlockSnapshot snap : event.getReplacedBlockSnapshots())
        {
            if(!(snap.getCurrentBlock() instanceof LavaBlock))
            {
                sim.notifyBlockChange(event.getWorld(), snap.getPos());
            }
        }
    }
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) 
    {
        Simulator.INSTANCE.onServerTick(event);
    }
    
    private int clientStatCounter = Configurator.RENDER.quadCacheStatReportingInterval * 20;
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) 
    {
        if (event.phase == TickEvent.Phase.END
                && Configurator.RENDER.enableQuadCacheStatistics
                && --clientStatCounter == 0) 
        {
            clientStatCounter = Configurator.RENDER.quadCacheStatReportingInterval * 20;
            Log.info("QuadCache stats = " + QuadCache.INSTANCE.cache.stats().toString());
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (ModKeys.PLACEMENT_FACE.isPressed())
        {
            //TODO
            Log.info("Placement Face Key");
        } 
        else if (ModKeys.PLACEMENT_MODE.isPressed())
        {
            //TODO
            Log.info("Placement Mode Key");
        }
        else if(ModKeys.PLACEMENT_PREVIEW.isPressed())
        {
            PreviewMode newMode = Useful.nextEnumValue(Configurator.RENDER.previewSetting);
            Configurator.RENDER.previewSetting = newMode;
            ConfigManager.sync(Adversity.MODID, Type.INSTANCE);
            String message = I18n.translateToLocalFormatted("placement.message.preview_set",  I18n.translateToLocal("placement.preview." + newMode.toString().toLowerCase()));
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
        }
        else if(ModKeys.PLACEMENT_ROTATION.isPressed())
        {
            //TODO
            Log.info("Placement Rotation Key");
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = MinecraftTools.getPlayer(mc);
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem !=null && !heldItem.isEmpty() && (heldItem.getItem() instanceof PlacementItem)) 
        {
            PlacementItem placer = (PlacementItem) heldItem.getItem();
            placer.renderOverlay(event, player, heldItem);
        }
    }
//	@SubscribeEvent
//	public void onReplaceBiomeBlocks(ReplaceBiomeBlocks.ReplaceBiomeBlocks event) {
//		if (event.getWorld().provider.getDimension() == 0) {
//			Drylands.replaceBiomeBlocks(event);
//			event.setResult(Result.DENY);
//		}
//	}
//
}
