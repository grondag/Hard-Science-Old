package com.grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;

import com.grondag.adversity.Adversity;

public class ItemVolcanicLavaBucket extends ItemBucket {

	public ItemVolcanicLavaBucket(Block fluidblock) {
		super(fluidblock);
		this.setCreativeTab(Adversity.tabAdversity);
		this.setContainerItem(Items.bucket);
		this.setUnlocalizedName("volcanicLavaBucket");
		this.setTextureName(Adversity.MODID + ":" + "volcanic_lava_bucket");
	}

}
