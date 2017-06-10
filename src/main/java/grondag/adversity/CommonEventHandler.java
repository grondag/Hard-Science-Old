package grondag.adversity;

import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;

import grondag.adversity.feature.volcano.lava.simulator.LavaSimulator;
import grondag.adversity.library.model.quadfactory.QuadCache;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.superblock.support.NiceBlockHighlighter;
import grondag.adversity.superblock.terrain.LavaBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
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
            Output.warn("Unable to parse localized denial messages. Using default.");
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
        NiceBlockHighlighter.handleDrawBlockHighlightEvent(event);
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
            Output.info("QuadCache stats = " + QuadCache.INSTANCE.cache.stats().toString());
        }
    }

    
//
//	@SubscribeEvent
//	public void onReplaceBiomeBlocks(ReplaceBiomeBlocks.ReplaceBiomeBlocks event) {
//		if (event.getWorld().provider.getDimension() == 0) {
//			Drylands.replaceBiomeBlocks(event);
//			event.setResult(Result.DENY);
//		}
//	}
//
}
