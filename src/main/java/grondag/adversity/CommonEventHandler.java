package grondag.adversity;

import grondag.adversity.feature.volcano.VolcanicLavaBlock;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.FillBucketEvent;
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
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString(DENIALS[Useful.SALT_SHAKER.nextInt(DENIALS.length)]));
            }
            event.setCanceled(true);
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
