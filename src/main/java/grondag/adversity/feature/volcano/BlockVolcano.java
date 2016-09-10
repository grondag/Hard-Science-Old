package grondag.adversity.feature.volcano;

import java.util.Random;

import grondag.adversity.Adversity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockVolcano extends Block implements ITileEntityProvider {


	public BlockVolcano() {
		super(Material.rock);
		this.setCreativeTab(Adversity.tabAdversity);
		this.setHardness(2000.0F);
		this.setResistance(2000.0F);
		this.setStepSound(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 5);
        this.setRegistryName("blockVolcano");
        this.setUnlocalizedName(this.getRegistryName().toString());
	}


	@Override
	public int quantityDropped(Random random) {
		return 0;
	}



    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileVolcano();
    }

	
}