package grondag.adversity.feature.volcano;

import grondag.adversity.Adversity;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;



public class Volcano {

	// BIOMES
	public static BiomeGenBase		volcano;


	//public static BlockHotBasalt	blockHotBasalt;
	public static BlockVolcanicLava	blockVolcanicLava;
	//public static BlockHaze			blockHaze;
	//public static BlockHazeRising	blockHazeRising;
	//public static BlockAsh			blockAsh;

	// ITEMS
	public static Item				itemVolcanicLavaBucket;

	// FLUIDS
	public static Fluid				fluidVolcanicLava;

	// TILE ENTITIES
	//public static BlockVolcano		blockVolcano;

	public static void preInit(FMLPreInitializationEvent event) {


		//GameRegistry.registerBlock(Volcano.blockHaze = new BlockHaze(Material.air), "Haze");
		//GameRegistry.registerBlock(Volcano.blockHazeRising = new BlockHazeRising(Material.glass), "HazeRising");
		//GameRegistry.registerBlock(Volcano.blockAsh = new BlockAsh("Ash", Material.snow), "Ash");

		// FLUIDS
	    Volcano.fluidVolcanicLava = new Fluid("volcanic_lova", new ResourceLocation(Adversity.MODID, "blocks/lava_still"), new ResourceLocation(Adversity.MODID, "blocks/lava_flowing"))
	            .setLuminosity(15).setDensity(3000).setViscosity(6000).setTemperature(1300);

		FluidRegistry.registerFluid(Volcano.fluidVolcanicLava);
		Volcano.blockVolcanicLava = (BlockVolcanicLava) new BlockVolcanicLava(Volcano.fluidVolcanicLava, Material.lava)
				.setRegistryName("volcanic_lava");
		GameRegistry.registerBlock(Volcano.blockVolcanicLava, blockVolcanicLava.getRegistryName());


		// ITEMS
//		itemVolcanicLavaBucket = new ItemVolcanicLavaBucket(Volcano.blockVolcanicLava);
//		GameRegistry.registerItem(itemVolcanicLavaBucket, "ItemVolcanicLavaBucket");
//		FluidContainerRegistry.registerFluidContainer(Volcano.fluidVolcanicLava, new ItemStack(itemVolcanicLavaBucket),
//				new ItemStack(Items.bucket));

		
		// TILE ENTITIES
//		Volcano.blockVolcano = new BlockVolcano();
//		GameRegistry.registerBlock(Volcano.blockVolcano, "BlockVolcano");
//		GameRegistry.registerTileEntity(TileVolcano.class, "TileVolcano");
	}

	public static void init(FMLInitializationEvent event) {
//		Volcano.volcano = new BiomeVolcano(Config.BiomeIDs.volcano);
//		BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(Volcano.volcano, 0));
//		BiomeDictionary.registerBiomeType(Volcano.volcano, BiomeDictionary.Type.HOT);
		
		
	}

	public static void postInit(FMLPostInitializationEvent event) {

		// in vanilla worlds, plants shouldn't stop volcanic lava from spreading
		// TODO: probably need to add some more blocks here
		Blocks.fire.setFireInfo(Blocks.cactus, 5, 5);
		Blocks.fire.setFireInfo(Blocks.deadbush, 30, 100);
	}

}
