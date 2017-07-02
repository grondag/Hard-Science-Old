package grondag.adversity.superblock.varia;

import grondag.adversity.Configurator;
import grondag.adversity.Configurator.Substances.Substance;
import grondag.adversity.superblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.color.Chroma;
import grondag.adversity.superblock.color.Hue;
import grondag.adversity.superblock.color.Luminance;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.text.translation.I18n;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BlockSubstance {
	FLEXSTONE(Configurator.SUBSTANCES.flexstone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	DURASTONE(Configurator.SUBSTANCES.durastone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	HYPERSTONE(Configurator.SUBSTANCES.hyperstone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.INDIGO, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	FLEXIGLASS(Configurator.SUBSTANCES.flexiglass, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.APATITE, Chroma.WHITE, Luminance.LIGHT).ordinal),
    
    DURAGLASS(Configurator.SUBSTANCES.durastone, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.BERYL, Chroma.WHITE, Luminance.LIGHT).ordinal),
    
    HYPERGLASS(Configurator.SUBSTANCES.hyperstone, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.ICE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	FLEXWOOD(Configurator.SUBSTANCES.flexwood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.CHEDDAR, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
    DURAWOOD(Configurator.SUBSTANCES.durawood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.EMBER, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),

    HYPERWOOD(Configurator.SUBSTANCES.hyperwood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.CHERRY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),

    
	BASALT(Configurator.SUBSTANCES.basalt, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
	//can't use lava as material here - confuses the lava fluid renderer
    VOLCANIC_LAVA(Configurator.SUBSTANCES.volcanicLava, Material.ROCK, SoundType.STONE, 0);
    
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
	
	BlockSubstance(Substance substance, Material material, SoundType sound, int defaultColorMapID) 
	{
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

    @SuppressWarnings("deprecation")
    public String localizedName()
    {
        return I18n.translateToLocal("material." + this.name().toLowerCase());
    }
}