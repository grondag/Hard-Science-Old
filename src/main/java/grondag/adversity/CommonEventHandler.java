package grondag.adversity;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.feature.volcano.VolcanicLavaBlock;
import grondag.adversity.feature.volcano.lava.simulator.LavaSimulator;
import grondag.adversity.simulator.Simulator;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//import com.grondag.adversity.feature.drylands.Drylands;
//
//import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks;
//import net.minecraftforge.fml.common.eventhandler.Event.Result;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
public class CommonEventHandler 
{
    public static final CommonEventHandler INSTANCE = new CommonEventHandler();
    //TODO: localize
    private static final String[] DENIALS = 
    {
        "Absolutely not.",
        "Nope.",
        "Not happening",
        "Doctor No sends his regards.",
        "You must be joking.",
        "I made a list of things that aren't going to happen, and what you just tried is on it.",
        "Nice try.",
        "That's not going to work.",
        "DENIED!"
    };
    
    @SubscribeEvent(priority = EventPriority.HIGH) 
    public void onFillBucket(FillBucketEvent event)
    {
        RayTraceResult target = event.getTarget();

        if(target.typeOfHit == RayTraceResult.Type.BLOCK 
                && event.getWorld().getBlockState(target.getBlockPos()).getBlock() instanceof VolcanicLavaBlock)
        {
            if(event.getEntityPlayer() != null && event.getWorld().isRemote)
            {
                event.getEntityPlayer().sendMessage(new TextComponentString(DENIALS[ThreadLocalRandom.current().nextInt(DENIALS.length)]));
            }
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) 
    {
        if (event.getModID().equals(Adversity.MODID))
        {
            ConfigManager.sync(Adversity.MODID, Type.INSTANCE);
            Configurator.recalcDervied();
        }
    }
    
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) 
    {
        // Lava blocks have their own handling
        if(!(event.getState().getBlock() instanceof VolcanicLavaBlock))
        {
            Simulator.INSTANCE.getFluidTracker().notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        // Lava blocks have their own handling
        if(!(event.getState().getBlock() instanceof VolcanicLavaBlock))
        {
            Simulator.INSTANCE.getFluidTracker().notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        LavaSimulator sim = Simulator.INSTANCE.getFluidTracker();
        for(BlockSnapshot snap : event.getReplacedBlockSnapshots())
        {
            if(!(snap.getCurrentBlock() instanceof VolcanicLavaBlock))
            {
                sim.notifyBlockChange(event.getWorld(), snap.getPos());
            }
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
