package com.grondag.adversity.feature.volcano;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.grondag.adversity.Adversity;
import com.grondag.adversity.deprecate.OddBlock;
import com.grondag.adversity.lib.OddUtils;

public class BlockHotBasalt extends OddBlock {

	public BlockHotBasalt(String unlocalizedName, Material material) {
		super(unlocalizedName, material);
		this.setBlockTextureName(Adversity.MODID + ":basalt0_0");
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypeStone);
		this.setTickRandomly(true);
	}

	@Override
	public void registerBlockIcons(IIconRegister reg) {

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 16; j++) {
				this.icons[i * 16 + j] = reg.registerIcon(Adversity.MODID + ":basalt" + i + "_" + j);
			}
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {

			OddUtils.igniteSurroundingBlocks(world, x, y, z);

			final int meta = world.getBlockMetadata(x, y, z);

			// necessary to prevent too many block updates on large volcanoes
			if (meta == 0 || world.getBlock(x - 1, y, z) != Blocks.air && world.getBlock(x + 1, y, z) != Blocks.air
					&& world.getBlock(x, y, z - 1) != Blocks.air && world.getBlock(x, y, z + 1) != Blocks.air
					&& world.getBlock(x, y + 1, z) != Blocks.air) {
				world.setBlock(x, y, z, Volcano.blockBasalt, 0, 2);
			} else {
				world.setBlockMetadataWithNotify(x, y, z, meta - 1, 2);
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

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return 2 / (world.getBlockMetadata(x, y, z) + 1);
	}

	@Override
	public float getExplosionResistance(Entity entity) {
		return 10;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX,
			double explosionY, double explosionZ) {
		return 10 / (world.getBlockMetadata(x, y, z) + 1);
	}

}
