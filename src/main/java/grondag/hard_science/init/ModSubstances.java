package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.movetogether.BlockColorMapProvider;
import grondag.hard_science.movetogether.BlockSubstance;
import grondag.hard_science.movetogether.Chroma;
import grondag.hard_science.movetogether.Hue;
import grondag.hard_science.movetogether.Luminance;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class ModSubstances
{

    public static BlockSubstance  MACHINE = BlockSubstance.create("machine", Configurator.SUBSTANCES.durastone,  MachineBlock.MACHINE_MATERIAL, SoundType.METAL, 
    BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT).ordinal);
    public static BlockSubstance BASALT = BlockSubstance.create("basalt", Configurator.SUBSTANCES.basalt, Material.ROCK, SoundType.STONE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK).ordinal);
    //can't use lava as material here - confuses the lava fluid renderer
    public static BlockSubstance VOLCANIC_LAVA = BlockSubstance.create("volcanic_lava", Configurator.SUBSTANCES.volcanicLava, Material.ROCK, SoundType.STONE, 0);
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
