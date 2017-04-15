package grondag.adversity.niceblock.support;

import grondag.adversity.Configurator;
import grondag.adversity.Configurator.Substances.Substance;
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
	FLEXSTONE("flexstone", Configurator.SUBSTANCES.flexstone, Material.ROCK, SoundType.STONE, 
	        BlockColorMapProvider.INSTANCE.getColorMap(Hue.YELLOW, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	DURASTONE("durastone", Configurator.SUBSTANCES.durastone, Material.ROCK, SoundType.STONE, 
	        BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	HYPERSTONE("hyperstone", Configurator.SUBSTANCES.hyperstone, Material.IRON, SoundType.METAL,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.ULTRAMARINE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	SUPERWOOD("superwood", Configurator.SUBSTANCES.superwood, Material.WOOD, SoundType.WOOD,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.BURGUNDY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
	BASALT("basalt", Configurator.SUBSTANCES.basalt, Material.ROCK, SoundType.STONE,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
	//can't use lava as material here - confuses the lava fluid renderer
    VOLCANIC_LAVA("volcanicLava", Configurator.SUBSTANCES.volcanicLava, Material.ROCK, SoundType.STONE, 0);
    
	public final String materialName;
	public final Material material;
	public final SoundType stepSound;

	public final int hardness;
	public final int resistance;
	public final String harvestTool;
	public final int harvestLevel;
	public final int defaultColorMapID;

	BaseMaterial(String name, Substance substance, Material material, SoundType sound, int defaultColorMapID) 
	{
		this.materialName = name;
		this.material = material;
		stepSound = sound;
		this.defaultColorMapID = defaultColorMapID;

		Substance props = substance;
		hardness = props.hardness;
		resistance = props.resistance;
		harvestTool = props.harvestTool;
		harvestLevel = props.harvestLevel;
	}
}