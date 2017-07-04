package grondag.hard_science;

import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;

import grondag.hard_science.feature.volcano.lava.LavaBlock;
import grondag.hard_science.feature.volcano.lava.simulator.LavaSimulator;
import grondag.hard_science.simulator.Simulator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@SuppressWarnings({ "deprecation"})
@Mod.EventBusSubscriber
public class CommonEventHandler 
{
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
    public static void onFillBucket(FillBucketEvent event)
    {
        if(event.getEntityPlayer() != null && !event.getWorld().isRemote)
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
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) 
    {
        if (event.getModID().equals(HardScience.MODID))
        {
            ConfigManager.sync(HardScience.MODID, Type.INSTANCE);
            Configurator.recalcDerived();
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) 
    {
        // Lava blocks have their own handling
        if(!event.getWorld().isRemote && !(event.getState().getBlock() instanceof LavaBlock))
        {
            LavaSimulator sim = Simulator.INSTANCE.getLavaSimulator();
            if(sim != null) sim.notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        // Lava blocks have their own handling
        if(!event.getWorld().isRemote && !(event.getState().getBlock() instanceof LavaBlock))
        {
            LavaSimulator sim = Simulator.INSTANCE.getLavaSimulator();
            if(sim != null) sim.notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public static void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        if(event.getWorld().isRemote) return;
        
        LavaSimulator sim = Simulator.INSTANCE.getLavaSimulator();
        if(sim != null)
        {
            for(BlockSnapshot snap : event.getReplacedBlockSnapshots())
            {
                if(!(snap.getCurrentBlock() instanceof LavaBlock))
                {
                    sim.notifyBlockChange(event.getWorld(), snap.getPos());
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) 
    {
        Simulator.INSTANCE.onServerTick(event);
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
