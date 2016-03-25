package com.grondag.adversity.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class AdversityWorldType extends WorldType {

	public AdversityWorldType(String name) {
		super(name);
	}

	@Override
	public WorldChunkManager getChunkManager(World world) {

		return new AdversityChunkManager(world);
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions) {

		return new AdversityChunkProvider(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled());
	}

}
