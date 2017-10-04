package grondag.hard_science.feature.volcano;

import java.util.Random;

import grondag.hard_science.HardScience;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class VolcanoBlock extends Block implements ITileEntityProvider {


	public VolcanoBlock() {
		super(Material.ROCK);
		this.setCreativeTab(HardScience.tabMod);
		this.setResistance(2000.0F);
		this.setSoundType(SoundType.STONE);
		this.setBlockUnbreakable();
        this.setRegistryName("volcano_block");
        this.setUnlocalizedName(this.getRegistryName().toString());
	}


	@Override
	public int quantityDropped(Random random) {
		return 0;
	}



    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new VolcanoTileEntity();
    }

	
}