package com.grondag.adversity.feature.volcano;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;

import com.grondag.adversity.Adversity;
import com.grondag.adversity.deprecate.OddBlock;

public class BlockBasalt extends OddBlock {

	public BlockBasalt(String unlocalizedName, Material material) {
		super(unlocalizedName, material);
		this.setBlockTextureName(Adversity.MODID + ":basalt0");
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypeStone);
		this.setHardness(2);
		this.setResistance(10);
	}

	@Override
	public void registerBlockIcons(IIconRegister reg) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 16; j++) {
				this.icons[i * 16 + j] = reg.registerIcon(Adversity.MODID + ":basalt" + i);
			}
		}
	}
}
