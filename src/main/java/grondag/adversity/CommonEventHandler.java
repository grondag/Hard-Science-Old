package grondag.adversity;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.config.Config;
import grondag.adversity.feature.volcano.VolcanicLavaBlock;
import grondag.adversity.feature.volcano.lava.columnmodel.LavaSimulatorNew;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.simulator.Simulator;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
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
    //TODO: localize
    private static final String[] DENIALS = 
    {
        "Absolutely not.",
        "Nope.",
        "Not happening",
        "Doctor No sends his regards.",
        "You must be joking.",
        "I made a list of things that aren't going to happen, and what you just tried is near the top.",
        "Nice try.",
        "That's not going to work."
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
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString(DENIALS[ThreadLocalRandom.current().nextInt(DENIALS.length)]));
            }
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) 
    {
        Config.reload();
    }
    
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) 
    {
        //TODO: remove cast when column model is fully implemented
        
        // Lava blocks have their own handling
        if(!(event.getState().getBlock() instanceof VolcanicLavaBlock))
        {
            LavaSimulatorNew sim = (LavaSimulatorNew) Simulator.instance.getFluidTracker();        
            sim.notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        //TODO: remove cast when column model is fully implemented
        
        // Lava blocks have their own handling
        if(!(event.getState().getBlock() instanceof VolcanicLavaBlock))
        {
            LavaSimulatorNew sim = (LavaSimulatorNew) Simulator.instance.getFluidTracker();        
            sim.notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        //TODO: remove cast when column model is fully implemented
        LavaSimulatorNew sim = (LavaSimulatorNew) Simulator.instance.getFluidTracker();
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
