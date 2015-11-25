package grondag.adversity.niceblocks;

import grondag.adversity.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.material.Material;

public enum NiceSubstance {
	BASALT(0, "basalt", BaseMaterial.DRESSED_STONE);
	
	public final int id;
	public final String name;
	public final BaseMaterial baseMaterial;
	
	
	NiceSubstance(int id, String name, BaseMaterial baseMaterial){
		this.id = id;
		this.name = name;
		this.baseMaterial = baseMaterial;
	}
	
	public String resourceName(){
		return baseMaterial.name + "_" + name;
	}
	
	public enum BaseMaterial{
		DRESSED_STONE(0,"dressed_stone", Material.rock, Block.soundTypeStone),
		COMPOSITE(1,"composite", Material.rock, Block.soundTypeStone),
		DURAPLAST(2,"duraplast", Material.iron, Block.soundTypeMetal);
		
		public final int id;
		public final String name;
		public final Material material;
		public final SoundType stepSound;
		
		public final int hardness;
		public final int resistance;
		public final String harvestTool;
		public final int harvestLevel;

		
		BaseMaterial(int id, String name, Material material, SoundType sound){
			this.id = id;
			this.name = name;
			this.material = material;
			this.stepSound = sound;
			
			Config.Substance props = Config.substances.get(name);
			this.hardness = props.hardness;
			this.resistance = props.resistance;
			this.harvestTool = props.harvestTool;
			this.harvestLevel = props.harvestLevel;
			
		}
	}
	
}
