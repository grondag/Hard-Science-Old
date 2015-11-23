package grondag.adversity.library;

import grondag.adversity.Adversity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TextureLoader {

	private final String basename;
	private final int	spriteCount;

	public TextureLoader(String basename, int spriteCount){
		this.basename = basename;
		this.spriteCount = spriteCount;
	}

	@SubscribeEvent
	public void stitcherEventPre(TextureStitchEvent.Pre event) {
		for (int n = 0 ; n < spriteCount ; n++){
			event.map.registerSprite(new ResourceLocation(buildTextureName(basename, n)));
		}

	}
	
	public static String buildTextureName(String basename, int offset){
		return "adversity:blocks/" + basename + "/" + basename + "_" + (offset >> 3) + "_" + (offset & 7);
	}
}