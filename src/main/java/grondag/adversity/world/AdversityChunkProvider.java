package com.grondag.adversity.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderGenerate;

public class AdversityChunkProvider extends ChunkProviderGenerate {

	@Override
	public Chunk loadChunk(int p_73158_1_, int p_73158_2_) {
		// TODO Auto-generated method stub
		return super.loadChunk(p_73158_1_, p_73158_2_);
	}

	@Override
	public Chunk provideChunk(int p_73154_1_, int p_73154_2_) {
		// TODO Auto-generated method stub
		return super.provideChunk(p_73154_1_, p_73154_2_);
	}

	public AdversityChunkProvider(World p_i2006_1_, long p_i2006_2_, boolean p_i2006_4_) {
		super(p_i2006_1_, p_i2006_2_, p_i2006_4_);
	}

}
