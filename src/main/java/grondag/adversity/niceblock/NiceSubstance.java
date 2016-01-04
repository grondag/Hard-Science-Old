package grondag.adversity.niceblock;

import grondag.adversity.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Substance as a property of a NiceBlock determines textures and some physical
 * properties via baseMaterial. While a NiceBlock can have up to 16 substances
 * with different physical properties, and while most of the block methods
 * should use metadata, preference is to have each NiceBlock use Substances with
 * the same or similar base materials.
 */
public enum NiceSubstance {
	BASALT("basalt", BaseMaterial.DRESSED_STONE),
	DIORITE("diorite", BaseMaterial.DRESSED_STONE),
	HOT_BASALT("hot_basalt", BaseMaterial.DRESSED_STONE, "hot_basalt");

	/**
	 * Folder and prefix for primary textures.
	 * Specific usage left up to model cookbook.
	 */
	public final String firstTexture;

	/**
	 * Folder and prefix for secondary textures.
	 * Specific usage left up to model cookbook.
	 * MAY BE NULL.
	 */
	public final String secondTexture;
	
	/**
	 * Controls MineCraft material properties.
	 */
	public final BaseMaterial baseMaterial;

	NiceSubstance(String firstTexture, BaseMaterial baseMaterial) {
		this(firstTexture, baseMaterial, null);
	}
	
	NiceSubstance(String firstTexture, BaseMaterial baseMaterial, String secondTexture){
		this.baseMaterial = baseMaterial;
		this.firstTexture = firstTexture;
		this.secondTexture = secondTexture;
	}

	/**
	 * Similar to Minecraft Material. Didn't want to tie to that implementation.
	 * Determines Minecraft material and other physical properties.
	 */
	public enum BaseMaterial {
		DRESSED_STONE("dressed_stone", Material.rock, Block.soundTypeStone),
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
}
