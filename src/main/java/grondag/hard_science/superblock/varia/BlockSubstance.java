package grondag.hard_science.superblock.varia;

import grondag.exotic_matter.varia.ILocalized;
import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.Configurator;
import grondag.hard_science.Configurator.Substances.Substance;
import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.Chroma;
import grondag.hard_science.superblock.color.Hue;
import grondag.hard_science.superblock.color.Luminance;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BlockSubstance implements IMessagePlusImmutable<BlockSubstance>, IReadWriteNBTImmutable<BlockSubstance>, ILocalized
{
	FLEXSTONE(Configurator.SUBSTANCES.flexstone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	DURASTONE(Configurator.SUBSTANCES.durastone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	HYPERSTONE(Configurator.SUBSTANCES.hyperstone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.INDIGO, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	FLEXIGLASS(Configurator.SUBSTANCES.flexiglass, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.APATITE, Chroma.WHITE, Luminance.LIGHT).ordinal),
    
    DURAGLASS(Configurator.SUBSTANCES.durastone, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.BERYL, Chroma.WHITE, Luminance.LIGHT).ordinal),
    
    HYPERGLASS(Configurator.SUBSTANCES.hyperstone, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.ICE, Chroma.WHITE, Luminance.LIGHT).ordinal),
	
	FLEXWOOD(Configurator.SUBSTANCES.flexwood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.CHEDDAR, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
    DURAWOOD(Configurator.SUBSTANCES.durawood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.EMBER, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),

    HYPERWOOD(Configurator.SUBSTANCES.hyperwood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.CHERRY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),

    HDPE(Configurator.SUBSTANCES.hdpe, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.BERYL, Chroma.PURE_NETURAL, Luminance.LIGHT).ordinal),
    
	BASALT(Configurator.SUBSTANCES.basalt, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal),
	
	//can't use lava as material here - confuses the lava fluid renderer
    VOLCANIC_LAVA(Configurator.SUBSTANCES.volcanicLava, Material.ROCK, SoundType.STONE, 0),
    
    MACHINE(Configurator.SUBSTANCES.durastone,  MachineBlock.MACHINE_MATERIAL, SoundType.METAL, 
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT).ordinal),
    
    // Reserved to pad enum serializer so don't break world saves if later add more substances.
    RESERVED02(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED03(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED04(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED05(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED06(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED07(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED08(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED09(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED10(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED11(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED12(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED13(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED14(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED15(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED16(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED17(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED18(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED19(Configurator.SUBSTANCES.flexstone,  null, null, 0),
    RESERVED20(Configurator.SUBSTANCES.flexstone,  null, null, 0);
    
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
    
    @Override
    public BlockSubstance deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.SUPER_MODEL_SUBSTANCE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.SUPER_MODEL_SUBSTANCE, this);
    }

    @Override
    public BlockSubstance fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(BlockSubstance.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this);
    }
}