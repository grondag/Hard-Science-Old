package grondag.adversity.feature.volcano;

import java.util.Random;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockVolcanicLava extends BlockFluidClassic {

	public BlockVolcanicLava(Fluid fluid, Material material) {
		super(fluid, material);
		this.setCreativeTab(Adversity.tabAdversity);
		defaultDisplacements.put(Blocks.reeds, true);
	}


	@Override
    public boolean canDisplace(IBlockAccess world, BlockPos pos)
    {
        return super.canDisplace(world, pos);
    }


    @Override
    public boolean displaceIfPossible(World world, BlockPos pos)
    {
        return super.displaceIfPossible(world, pos);
    }


	@Override
    protected boolean canFlowInto(IBlockAccess world, BlockPos pos)
    {
		if (world.isAirBlock(pos))
			return true;

		final IBlockState state = world.getBlockState(pos);
		final Block block = state.getBlock();
		if (block == this)
			return true;

		if (this.displacements.containsKey(block))
			return this.displacements.get(block);

		final Material material = block.getMaterial(state);
		if (material.blocksMovement() || material == Material.portal)
			return false;

		final int density = getDensity(world, pos);
		if (density == Integer.MAX_VALUE)
			return true;

		if (this.density > density)
			return true;
		else
			return false;
	}

	private boolean isBasalt(Block b) {
		return b == NiceBlockRegistrar.BLOCK_HOT_BASALT || b == NiceBlockRegistrar.BLOCK_COOL_BASALT;
	}

	
	@Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
		super.updateTick(world, pos, state, rand);

		if (!(world.getBlockState(pos.down()).getBlock() == this || this.canDisplace(world, pos.down()))) {
			int bonusChance = 0;
			if (isBasalt(world.getBlockState(pos.east()).getBlock())) ++ bonusChance;
			if (isBasalt(world.getBlockState(pos.west()).getBlock())) ++ bonusChance;
			if (isBasalt(world.getBlockState(pos.north()).getBlock())) ++ bonusChance;
			if (isBasalt(world.getBlockState(pos.south()).getBlock())) ++ bonusChance;
			if (bonusChance == 4) 
				bonusChance = 15;
			else if (bonusChance == 3)
				bonusChance = 9;

			if (rand.nextInt(16) <= world.getBlockState(pos).getValue(NiceBlock.META) + bonusChance) {
				world.setBlockState(pos, NiceBlockRegistrar.BLOCK_HOT_BASALT.getDefaultState().withProperty(NiceBlock.META, 3));
			}
		}
	}

	
	@Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

	
    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return 15 << 20 | 15 << 4;
	}

}
