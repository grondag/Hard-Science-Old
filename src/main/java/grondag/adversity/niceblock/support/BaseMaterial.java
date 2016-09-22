package grondag.adversity.niceblock.support;

import grondag.adversity.Config;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BaseMaterial {
	FLEXSTONE("flexstone", Material.ROCK, SoundType.STONE),
	DURASTONE("durastone", Material.ROCK, SoundType.STONE),
	HYPERSTONE("hyperstone", Material.IRON, SoundType.WOOD),
	SUPERWOOD("superwood", Material.WOOD, SoundType.METAL),
	BASALT("basalt", Material.ROCK, SoundType.STONE),
	//can't use lava as material here - confuses the lava fluid renderer
    VOLCANIC_LAVA("volcanicLava", Material.ROCK, SoundType.STONE);
	public final String materialName;
	public final Material material;
	public final SoundType stepSound;

	public final int hardness;
	public final int resistance;
	public final String harvestTool;
	public final int harvestLevel;

	BaseMaterial(String name, Material material, SoundType sound) {
		this.materialName = name;
		this.material = material;
		stepSound = sound;

		Config.Substance props = Config.substances.get(name);
		hardness = props.hardness;
		resistance = props.resistance;
		harvestTool = props.harvestTool;
		harvestLevel = props.harvestLevel;
	}
}