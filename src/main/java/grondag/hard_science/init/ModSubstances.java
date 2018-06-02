package grondag.hard_science.init;

import grondag.exotic_matter.block.BlockHarvestTool;
import grondag.exotic_matter.block.BlockSubstance;
import grondag.exotic_matter.init.SubstanceConfig;
import grondag.exotic_matter.model.color.BlockColorMapProvider;
import grondag.exotic_matter.model.color.Chroma;
import grondag.exotic_matter.model.color.Hue;
import grondag.exotic_matter.model.color.Luminance;
import grondag.hard_science.Configurator;
import grondag.hard_science.machines.base.MachineBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class ModSubstances
{

    public static BlockSubstance  MACHINE = BlockSubstance.create("machine", new SubstanceConfig(1, BlockHarvestTool.ANY, 0, 100, 1.0),  MachineBlock.MACHINE_MATERIAL, SoundType.METAL, 
    BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT).ordinal);
    //can't use lava as material here - confuses the lava fluid renderer
    public static BlockSubstance FLEXSTONE = BlockSubstance.create("flexstone", Configurator.SUBSTANCES.flexstone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.LIGHT).ordinal);
    public static BlockSubstance DURASTONE = BlockSubstance.create("durastone", Configurator.SUBSTANCES.durastone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.LIGHT).ordinal);
    public static BlockSubstance HYPERSTONE = BlockSubstance.createHypermatter("hyperstone", Configurator.SUBSTANCES.hyperstone, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.INDIGO, Chroma.WHITE, Luminance.LIGHT).ordinal);
    public static BlockSubstance FLEXIGLASS = BlockSubstance.create("flexiglass", Configurator.SUBSTANCES.flexiglass, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.APATITE, Chroma.WHITE, Luminance.LIGHT).ordinal);
    public static BlockSubstance DURAGLASS = BlockSubstance.create("duraglass", Configurator.SUBSTANCES.durastone, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.BERYL, Chroma.WHITE, Luminance.LIGHT).ordinal);
    public static BlockSubstance HYPERGLASS = BlockSubstance.createHypermatter("hyperglass", Configurator.SUBSTANCES.hyperstone, Material.GLASS, SoundType.GLASS, BlockColorMapProvider.INSTANCE.getColorMap(Hue.ICE, Chroma.WHITE, Luminance.LIGHT).ordinal);
    public static BlockSubstance FLEXWOOD = BlockSubstance.create("flexwood", Configurator.SUBSTANCES.flexwood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.CHEDDAR, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal);
    public static BlockSubstance DURAWOOD = BlockSubstance.create("durawood", Configurator.SUBSTANCES.durawood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.EMBER, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal);
    public static BlockSubstance HYPERWOOD = BlockSubstance.createHypermatter("hyperwood", Configurator.SUBSTANCES.hyperwood, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.CHERRY, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal);
    public static BlockSubstance HDPE = BlockSubstance.create("hdpe", Configurator.SUBSTANCES.hdpe, Material.WOOD, SoundType.WOOD, BlockColorMapProvider.INSTANCE.getColorMap(Hue.BERYL, Chroma.PURE_NETURAL, Luminance.LIGHT).ordinal);

}
