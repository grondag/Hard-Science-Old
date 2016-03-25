package com.grondag.adversity.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;

public class AdversityChunkManager extends WorldChunkManager {

	public AdversityChunkManager() {
		super();
	}

	public AdversityChunkManager(long seed, WorldType worldType) {
		super(seed, worldType);
	}

	public AdversityChunkManager(World world) {
		super(world);
	}
}
