package com.grondag.adversity.feature.volcano;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.grondag.adversity.Adversity;

public class BlockHazeRising extends BlockHaze {

	// acknowledgement: got the idea for this from Reika's steam blocks in ReactorCraft

	public BlockHazeRising(Material material) {
		super(material);
		this.setBlockName("hazeRising");
		this.setLightOpacity(0);
		this.setTickRandomly(true);
		this.setResistance(3600000);
		this.setBlockTextureName(Adversity.MODID + ":haze");
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
	}

	@Override
	public int tickRate(World world) {
		return 15;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		if (y < 127 && world.getBlock(x, y + 1, z) == Blocks.air) {
			world.setBlock(x, y + 1, z, this, 0, 2);
			world.scheduleBlockUpdate(x, y + 1, z, this, this.tickRate(world));
		}
		world.setBlockToAir(x, y, z);
	}

}