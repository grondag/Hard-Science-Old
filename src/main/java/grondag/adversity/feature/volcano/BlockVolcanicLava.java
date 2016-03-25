package com.grondag.adversity.feature.volcano;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import com.grondag.adversity.Adversity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockVolcanicLava extends BlockFluidClassic {

	@SideOnly(Side.CLIENT)
	protected IIcon	stillIcon;
	@SideOnly(Side.CLIENT)
	protected IIcon	flowingIcon;

	public BlockVolcanicLava(Fluid fluid, Material material) {
		super(fluid, material);
		this.setCreativeTab(Adversity.tabAdversity);
		defaultDisplacements.put(Blocks.reeds, true);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return side == 0 || side == 1 ? this.stillIcon : this.flowingIcon;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister register) {
		this.stillIcon = register.registerIcon(Adversity.MODID + ":volcanic_lava_still");
		this.flowingIcon = register.registerIcon(Adversity.MODID + ":volcanic_lava_flow");
	}

	@Override
	public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
		return super.canDisplace(world, x, y, z);
	}

	@Override
	public boolean displaceIfPossible(World world, int x, int y, int z) {
		return super.displaceIfPossible(world, x, y, z);
	}

	@Override
	protected boolean canFlowInto(IBlockAccess world, int x, int y, int z) {
		if (world.getBlock(x, y, z).isAir(world, x, y, z))
			return true;

		final Block block = world.getBlock(x, y, z);
		if (block == this)
			return true;

		if (this.displacements.containsKey(block))
			return this.displacements.get(block);

		final Material material = block.getMaterial();
		if (material.blocksMovement() || material == Material.portal)
			return false;

		final int density = getDensity(world, x, y, z);
		if (density == Integer.MAX_VALUE)
			return true;

		if (this.density > density)
			return true;
		else
			return false;
	}

	private boolean isBasalt(Block b) {
		return b == Volcano.blockHotBasalt || b == Volcano.blockBasalt;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		super.updateTick(world, x, y, z, rand);

		if (!(world.getBlock(x, y - 1, z) == this || this.canDisplace(world, x, y - 1, z))) {
			int bonusChance = 0;
			if (isBasalt(world.getBlock(x - 1, y, z))) ++ bonusChance;
			if (isBasalt(world.getBlock(x + 1, y, z))) ++ bonusChance;
			if (isBasalt(world.getBlock(x, y, z - 1))) ++ bonusChance;
			if (isBasalt(world.getBlock(x, y, z + 1))) ++ bonusChance;
			if (bonusChance == 4) 
				bonusChance = 15;
			else if (bonusChance == 3)
				bonusChance = 9;

			if (rand.nextInt(16) <= world.getBlockMetadata(x, y, z) + bonusChance) {
				world.setBlock(x, y, z, Volcano.blockHotBasalt, 4, 3);
			}
		}
	}

	@Override
	public boolean isBurning(IBlockAccess world, int x, int y, int z) {
		return true;
	}

	@Override
	public int getMixedBrightnessForBlock(IBlockAccess p_149677_1_, int p_149677_2_, int p_149677_3_, int p_149677_4_) {
		// Always render at full brightness.
		// Value is equivalent to 15 << 20 | 15 << 4
		// No, I don't know why this works either, it just does.
		return 15728880;
	}

}
