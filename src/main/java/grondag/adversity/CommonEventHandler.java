package grondag.adversity;

import com.grondag.adversity.feature.drylands.Drylands;

import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonEventHandler {

	@SubscribeEvent
	public void onReplaceBiomeBlocks(ReplaceBiomeBlocks.ReplaceBiomeBlocks event) {
		if (event.getWorld().provider.getDimension() == 0) {
			Drylands.replaceBiomeBlocks(event);
			event.setResult(Result.DENY);
		}
	}

}
