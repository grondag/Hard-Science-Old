package grondag.adversity.superblock.support;

import grondag.adversity.Configurator;
import grondag.adversity.Configurator.Substances.Substance;
import grondag.adversity.superblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.color.HueSet.Chroma;
import grondag.adversity.superblock.color.HueSet.Luminance;
import grondag.adversity.superblock.color.NiceHues.Hue;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BlockSubstance {
	FLEXSTONE("flexstone", Configurator.SUBSTANCES.flexstone, Material.ROCK, SoundType.STONE, 
	        BlockColorMapProvider.INSTANCE.getColorMap(Hue.YELLOW, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	DURASTONE("durastone", Configurator.SUBSTANCES.durastone, Material.ROCK, SoundType.STONE, 
	        BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	HYPERSTONE("hyperstone", Configurator.SUBSTANCES.hyperstone, Material.ROCK, SoundType.STONE,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.ULTRAMARINE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	FLEXIGLASS("flexiglass", Configurator.SUBSTANCES.flexiglass, Material.GLASS, SoundType.GLASS, 
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.YELLOW, Chroma.WHITE, Luminance.LIGHT).ordinal),
    
    DURAGLASS("duraglass", Configurator.SUBSTANCES.durastone, Material.GLASS, SoundType.GLASS, 
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal),
    
    HYPERGLASS("hyperglass", Configurator.SUBSTANCES.hyperstone, Material.GLASS, SoundType.GLASS,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.ULTRAMARINE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	FLEXWOOD("flexwood", Configurator.SUBSTANCES.hyperwood, Material.WOOD, SoundType.WOOD,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.BURGUNDY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
    DURAWOOD("durawood", Configurator.SUBSTANCES.hyperwood, Material.WOOD, SoundType.WOOD,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.BURGUNDY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),

    HYPERWOOD("hyperwood", Configurator.SUBSTANCES.hyperwood, Material.WOOD, SoundType.WOOD,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.BURGUNDY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),

    
	BASALT("basalt", Configurator.SUBSTANCES.basalt, Material.ROCK, SoundType.STONE,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
	//can't use lava as material here - confuses the lava fluid renderer
    VOLCANIC_LAVA("volcanicLava", Configurator.SUBSTANCES.volcanicLava, Material.ROCK, SoundType.STONE, 0);
    
	public final String materialName;
	public final Material material;
	public final SoundType soundType;

	public final int hardness;
	public final int resistance;
	public final String harvestTool;
	public final int harvestLevel;
	public final int defaultColorMapID;
	public final boolean isHyperMaterial;
	public final boolean isTranslucent;
	public final double walkSpeedFactor;
	
	BlockSubstance(String name, Substance substance, Material material, SoundType sound, int defaultColorMapID) 
	{
		this.materialName = name;
		this.material = material;
		this.isHyperMaterial = substance == Configurator.SUBSTANCES.hyperstone;
		soundType = sound;
		this.defaultColorMapID = defaultColorMapID;
		this.isTranslucent = this.material == Material.GLASS;

		this.hardness = substance.hardness;
		this.resistance = substance.resistance;
		this.harvestTool = substance.harvestTool;
		this.harvestLevel = substance.harvestLevel;
		this.walkSpeedFactor = substance.walkSpeedFactor;
		
	}
}