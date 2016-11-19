package grondag.adversity.niceblock.support;

import grondag.adversity.config.Config;
import grondag.adversity.config.Substance;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.HueSet.Chroma;
import grondag.adversity.niceblock.color.HueSet.Luminance;
import grondag.adversity.niceblock.color.NiceHues.Hue;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BaseMaterial {
	FLEXSTONE("flexstone", Material.ROCK, SoundType.STONE, 
	        BlockColorMapProvider.INSTANCE.getColorMap(Hue.YELLOW, Chroma.WHITE, Luminance.LIGHT).ordinal),
	DURASTONE("durastone", Material.ROCK, SoundType.STONE, 
	        BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal),
	HYPERSTONE("hyperstone", Material.IRON, SoundType.METAL,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.ULTRAMARINE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	SUPERWOOD("superwood", Material.WOOD, SoundType.WOOD,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.BURGUNDY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	BASALT("basalt", Material.ROCK, SoundType.STONE,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	//can't use lava as material here - confuses the lava fluid renderer
    VOLCANIC_LAVA("volcanicLava", Material.ROCK, SoundType.STONE, 0);
	public final String materialName;
	public final Material material;
	public final SoundType stepSound;

	public final int hardness;
	public final int resistance;
	public final String harvestTool;
	public final int harvestLevel;
	public final int defaultColorMapID;
	


	BaseMaterial(String name, Material material, SoundType sound, int defaultColorMapID) {
		this.materialName = name;
		this.material = material;
		stepSound = sound;
		this.defaultColorMapID = defaultColorMapID;

		Substance props = Config.substances().get(name);
		hardness = props.hardness;
		resistance = props.resistance;
		harvestTool = props.harvestTool;
		harvestLevel = props.harvestLevel;
	}
}