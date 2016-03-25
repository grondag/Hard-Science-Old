package com.grondag.adversity.feature.drylands;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;

import com.grondag.adversity.Config;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Drylands {
	private static long					seed;
	private static Random				rand			= new Random(1);
	private static NoiseGeneratorPerlin	field_147430_m	= new NoiseGeneratorPerlin(rand, 4);
	private static double[]				stoneNoise		= new double[256];

	// BIOMES
	public static BiomeGenBase			drylandFlats;
	public static BiomeGenBase			drylandMesa;
	public static BiomeGenBase			drylandHills;

	public static void preInit(FMLPreInitializationEvent event) {

	}

	public static void init(FMLInitializationEvent event) {

		Drylands.drylandHills = new BiomeDrylandHills(Config.BiomeIDs.drylandHills);
		BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(Drylands.drylandHills, 0));
		BiomeManager.addSpawnBiome(Drylands.drylandHills);
		BiomeDictionary.registerBiomeType(Drylands.drylandHills, BiomeDictionary.Type.HOT);

		Drylands.drylandMesa = new BiomeDrylandMesa(Config.BiomeIDs.drylandMesa);
		BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(Drylands.drylandMesa, 0));
		BiomeManager.addSpawnBiome(Drylands.drylandMesa);
		BiomeDictionary.registerBiomeType(Drylands.drylandMesa, BiomeDictionary.Type.HOT);

		Drylands.drylandFlats = new BiomeDrylandFlats(Config.BiomeIDs.drylandFlats);
		BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(Drylands.drylandFlats, 0));
		BiomeManager.addSpawnBiome(Drylands.drylandFlats);
		BiomeDictionary.registerBiomeType(Drylands.drylandFlats, BiomeDictionary.Type.HOT);
	}

	public static void postInit(FMLPostInitializationEvent event) {

	}

	private static boolean isWater(Block block) {
		return block == Blocks.water || block == Blocks.flowing_water || block == Blocks.ice || block == Blocks.snow
				|| block == Blocks.snow_layer;
	}

	private static boolean isEmpty(Block block) {
		return block == Blocks.air || block == null;
	}

	private static boolean isOcean(BiomeGenBase biome) {
		return biome == BiomeGenBase.ocean || biome == BiomeGenBase.deepOcean;
	}

	private static boolean isSolid(World world, int x, int y, int z) {
		if (world.isAirBlock(x, y, z))
			return false;
		final Block block = world.getBlock(x, y, z);
		return block.getMaterial().blocksMovement();
	}

	private static void dryLandify(int posIndex, BiomeGenBase oldBiome, Block[] blockArray, byte[] metaArray) {

		Block b;
		Block ba;
		final Block waterFiller;
		final byte dirtMeta = 0;
		final Block dirtFiller;
		final boolean replaceStone = false;
		final Block stoneFiller = Blocks.stone;
		final int sandCount = 0;
		final boolean fillActive = false;
		final Block fillBlock = Blocks.sand;
		int fillCount = 0;

		// boolean isMesa = isChangedToMesa(oldBiome);

		// if (oldBiome == BiomeGenBase.ocean || oldBiome == BiomeGenBase.deepOcean || oldBiome == BiomeGenBase.beach
		// || oldBiome == BiomeGenBase.coldBeach || oldBiome == BiomeGenBase.frozenOcean || oldBiome ==
		// BiomeGenBase.frozenOcean
		// || oldBiome == BiomeGenBase.frozenRiver || oldBiome == BiomeGenBase.mushroomIsland || oldBiome ==
		// BiomeGenBase.mushroomIslandShore
		// || oldBiome == BiomeGenBase.river || oldBiome == BiomeGenBase.stoneBeach) {
		// dirtFiller = Blocks.stone;
		// dirtMeta = 0;
		// waterFiller = Blocks.sand;
		// replaceStone = false;
		// } else {
		// waterFiller = Blocks.air;
		// dirtFiller = Blocks.sand;
		// dirtMeta = 1;
		// replaceStone = true;
		// stoneFiller = Blocks.hardened_clay;
		// }
		//
		for (int y = 254; y > 0; --y) {
			b = blockArray[y + posIndex];
			ba = blockArray[y + 1 + posIndex];
			// if (y == 62 && (ba == null || ba == Blocks.air)){
			// blockArray[y+posIndex] = Blocks.sand;
			// metaArray[y+posIndex] = 0;
			// } else if (isWater(b)) {
			// blockArray[y+posIndex] = Blocks.sand;
			// metaArray[y+posIndex] = 0;
			// } else if (b == Blocks.dirt || b == Blocks.grass || b == Blocks.gravel || b == Blocks.mycelium){
			// blockArray[y+posIndex] = Blocks.hardened_clay;
			// }
			if (isWater(b)) {
				if (y > 50) {
					if (ba == Blocks.sand) {
						blockArray[y + posIndex] = Blocks.sand;
						metaArray[y + posIndex] = 0;
					}
					blockArray[y + posIndex] = Blocks.air;
				} else {
					blockArray[y + posIndex] = Blocks.sand;
				}
			} else if (isEmpty(b)) {
				// if (ba == Blocks.sand){
				// blockArray[y+1+posIndex] = Blocks.sandstone;
				// }
			} else if (ba == Blocks.air) {
				if (y >= 50 && y <= 78) {
					blockArray[y + posIndex] = Blocks.sand;
					metaArray[y + posIndex] = 0;
					fillCount = 5;
				}
			} else if (fillCount > 2) {
				blockArray[y + posIndex] = Blocks.sand;
				metaArray[y + posIndex] = 0;
				--fillCount;
			} else if (fillCount > 0) {
				blockArray[y + posIndex] = Blocks.sandstone;
				--fillCount;
			}
		}
	}

	public static void replaceBiomeBlocks(IChunkProvider chunkProvider, int chunkX, int chunkZ, Block[] blockArray,
			byte[] metaArray, BiomeGenBase[] biomeArray, World world) {

		if (seed != world.getSeed()) {
			seed = world.getSeed();
			rand = new Random(world.getSeed());
		}
		rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		field_147430_m = new NoiseGeneratorPerlin(rand, 4);
		stoneNoise = new double[256];
		final int heightMap[] = new int[256];
		Block b;

		final double d0 = 0.03125D;
		stoneNoise = field_147430_m.func_151599_a(stoneNoise, chunkX * 16, chunkZ * 16, 16, 16, d0 * 2.0D, d0 * 2.0D,
				1.0D);

		for (int k = 0; k < 16; ++k) {
			for (int l = 0; l < 16; ++l) {

				final int posIndex = (l * 16 + k) * blockArray.length / 256;
				// int heightIndex = k*16+l;
				//
				for (int y = 254; y > 0; --y) {
					b = blockArray[y + posIndex];
					if (isWater(b)) {

						blockArray[y + posIndex] = Blocks.stone;
					}

				}
				// if (y>54){
				// blockArray[y+posIndex] = Blocks.air;
				// } else {
				// blockArray[y+posIndex] = Blocks.sand;
				// heightMap[heightIndex]=y;
				// }
				// } else if (! isEmpty(b) && heightMap[heightIndex]==0) {
				// heightMap[heightIndex]=y;
				// if (y <= 62) {
				// blockArray[y+posIndex] = Blocks.sand;
				// }
				// }
				// }

				if (isOcean(biomeArray[k * 16 + l])) {
					BiomeGenBase.desert.genTerrainBlocks(world, rand, blockArray, metaArray, chunkX * 16 + k, chunkZ
							* 16 + l, stoneNoise[l + k * 16]);
					biomeArray[k * 16 + l] = Drylands.drylandFlats;

				} else if (biomeArray[k * 16 + l].rootHeight >= 1.0F) {
					BiomeGenBase.mesa.genTerrainBlocks(world, rand, blockArray, metaArray, chunkX * 16 + k, chunkZ * 16
							+ l, stoneNoise[l + k * 16]);
					biomeArray[k * 16 + l] = Drylands.drylandMesa;

				} else {
					BiomeGenBase.desert.genTerrainBlocks(world, rand, blockArray, metaArray, chunkX * 16 + k, chunkZ
							* 16 + l, stoneNoise[l + k * 16]);
					biomeArray[k * 16 + l] = Drylands.drylandHills;
				}
			}
		}

	}

}
