package com.grondag.adversity.feature.drylands;

import net.minecraft.world.biome.BiomeGenDesert;

public class BiomeDrylandHills extends BiomeGenDesert {

	public BiomeDrylandHills(int id) {
		super(id);
		this.theBiomeDecorator.reedsPerChunk = 0;
		this.theBiomeDecorator.deadBushPerChunk = 1;
		this.theBiomeDecorator.cactiPerChunk = 5;
		this.enableRain = true;
		this.biomeName = "Dryland Hills";
	}

}