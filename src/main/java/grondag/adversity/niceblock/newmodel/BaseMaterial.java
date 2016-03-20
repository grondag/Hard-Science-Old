package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Config;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public enum BaseMaterial {
	FLEXSTONE("flexstone", Material.rock, SoundType.STONE),
	DURASTONE("durastone", Material.rock, SoundType.STONE),
	HYPERSTONE("hyperstone", Material.iron, SoundType.WOOD),
	SUPERWOOD("superwood", Material.wood, SoundType.METAL);

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