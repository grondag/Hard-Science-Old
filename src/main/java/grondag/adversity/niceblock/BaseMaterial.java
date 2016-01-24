package grondag.adversity.niceblock;

import grondag.adversity.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BaseMaterial {
	EXTRUDED_STONE("extruded_stone", Material.rock, Block.soundTypeStone),
	COMPOSITE("composite", Material.rock, Block.soundTypeStone),
	DURAPLAST("duraplast", Material.iron, Block.soundTypeMetal);

	public final String name;
	public final Material material;
	public final SoundType stepSound;

	public final int hardness;
	public final int resistance;
	public final String harvestTool;
	public final int harvestLevel;

	BaseMaterial(String name, Material material, SoundType sound) {
		this.name = name;
		this.material = material;
		stepSound = sound;

		Config.Substance props = Config.substances.get(name);
		hardness = props.hardness;
		resistance = props.resistance;
		harvestTool = props.harvestTool;
		harvestLevel = props.harvestLevel;
	}
}