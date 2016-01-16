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
	HOT_BASALT_0("tintbase", 0x373839, BaseMaterial.DRESSED_STONE, "hot_basalt_0"),
	HOT_BASALT_1("tintbase", 0x373839, BaseMaterial.DRESSED_STONE, "hot_basalt_1"),
	HOT_BASALT_2("tintbase", 0x373839, BaseMaterial.DRESSED_STONE, "hot_basalt_2"),
	HOT_BASALT_3("tintbase", 0x373839, BaseMaterial.DRESSED_STONE, "hot_basalt_3"),
	GLERP("tintbase", 0x6689DD, BaseMaterial.DRESSED_STONE, "hot_basalt_3");
	

	/**
	 * Folder and prefix for primary textures.
	 * Specific usage left up to model cookbook.
	 */
	public final String baseTexture;
	
	/**
	 * Color multiplier for base texture.
	 */
	public final int baseColor;
	
	/**
	 * Folder and prefix for secondary textures.
	 * Specific usage left up to model cookbook.
	 * MAY BE NULL.
	 */
	public final String overlayTexture;
	
	/**
	 * Color multiplier for overlay texture.
	 */
	public final int overlayColor;
	
	/**
	 * Controls MineCraft material properties.
	 */
	public final BaseMaterial baseMaterial;

	NiceSubstance(String baseTexture, BaseMaterial baseMaterial) {
		this(baseTexture, 16777215, baseMaterial, null, 16777215);
	}
	
	NiceSubstance(String baseTexture, int baseColor, BaseMaterial baseMaterial) {
		this(baseTexture, baseColor, baseMaterial, null, 16777215);
	}
	
	NiceSubstance(String baseTexture, int baseColor, BaseMaterial baseMaterial, String overlayTexture) {
		this(baseTexture, baseColor, baseMaterial, overlayTexture, 16777215);
	}

	NiceSubstance(String baseTexture, BaseMaterial baseMaterial, String overlayTexture){
		this(baseTexture, 16777215, baseMaterial, overlayTexture, 16777215);
	}
	
	NiceSubstance(String baseTexture, BaseMaterial baseMaterial, String overlayTexture,  int overlayColor){
		this(baseTexture, 16777215, baseMaterial, overlayTexture, overlayColor);
	}

	NiceSubstance(String baseTexture, int baseColor, BaseMaterial baseMaterial, String overlayTexture, int overlayColor){
		this.baseMaterial = baseMaterial;
		this.baseColor = baseColor;
		this.baseTexture = baseTexture;
		this.overlayColor = overlayColor;
		this.overlayTexture = overlayTexture;
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
