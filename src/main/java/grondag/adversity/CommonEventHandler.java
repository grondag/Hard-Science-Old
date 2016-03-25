package com.grondag.adversity.event;

import net.minecraftforge.event.terraingen.ChunkProviderEvent.ReplaceBiomeBlocks;

import com.grondag.adversity.feature.drylands.Drylands;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonEventHandler {

	@SubscribeEvent
	public void onReplaceBiomeBlocks(ReplaceBiomeBlocks.ReplaceBiomeBlocks event) {
		if (event.world.provider.dimensionId == 0) {
			Drylands.replaceBiomeBlocks(event.chunkProvider, event.chunkX, event.chunkZ, event.blockArray,
					event.metaArray, event.biomeArray, event.world);
			event.setResult(Result.DENY);
		}
	}

	// public Map<Block, Item> buckets = new HashMap<Block, Item>();
	//
	// @SubscribeEvent
	// public void onBucketFill(FillBucketEvent event) {
	// ItemStack result = fillBucket(event.world, event.target);
	// if (result == null) {
	// return;
	// }
	// event.result = result;
	// event.setResult(Result.ALLOW);
	// }
	//
	// private ItemStack fillBucket(World world, MovingObjectPosition pos) {
	// Block block = world.getBlock(pos.blockX, pos.blockY, pos.blockZ);
	//
	// Item bucket = buckets.get(block);
	// if (bucket != null && world.getBlockMetadata(pos.blockX, pos.blockY, pos.blockZ) == 0) {
	// world.setBlockToAir(pos.blockX, pos.blockY, pos.blockZ);
	// return new ItemStack(bucket);
	// }
	// else {
	// return null;
	// }
	// }
}
