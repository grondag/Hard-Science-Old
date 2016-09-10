package grondag.adversity.feature.volcano;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class Volcano {

	// BIOMES
	public static BiomeGenBase		volcano;


	//public static BlockHotBasalt	blockHotBasalt;
//	public static BlockVolcanicLava	blockVolcanicLava;
	//public static BlockHaze			blockHaze;
	//public static BlockHazeRising	blockHazeRising;
	//public static BlockAsh			blockAsh;

	// ITEMS
//	public static Item				itemVolcanicLavaBucket;

//	// FLUIDS
//	public static Fluid				fluidVolcanicLava;

	// TILE ENTITIES
	public static BlockVolcano		blockVolcano = new BlockVolcano();

	public static void preInit(FMLPreInitializationEvent event) {

 
		//GameRegistry.registerBlock(Volcano.blockHaze = new BlockHaze(Material.air), "Haze");
		//GameRegistry.registerBlock(Volcano.blockHazeRising = new BlockHazeRising(Material.glass), "HazeRising");
		//GameRegistry.registerBlock(Volcano.blockAsh = new BlockAsh("Ash", Material.snow), "Ash");

		// FLUIDS
//	    Volcano.fluidVolcanicLava = new Fluid("volcanic_lava", new ResourceLocation(Adversity.MODID, "blocks/volcanic_lava_still"), new ResourceLocation(Adversity.MODID, "blocks/volcanic_lava_flow"))
//	            .setLuminosity(0).setDensity(3000).setViscosity(12000).setTemperature(2000);
        
//		FluidRegistry.registerFluid(fluidVolcanicLava);
//		blockVolcanicLava = (BlockVolcanicLava) new BlockVolcanicLava(Volcano.fluidVolcanicLava, Material.lava)
//				.setRegistryName("volcanic_lava");
//		GameRegistry.register(blockVolcanicLava);
		GameRegistry.register(blockVolcano);
		GameRegistry.register(new ItemBlock(blockVolcano).setRegistryName(blockVolcano.getRegistryName().getResourcePath()));
//        GameRegistry.register(new ItemBlock(blockVolcanicLava).setRegistryName(blockVolcanicLava.getRegistryName().getResourcePath()));
//        FluidRegistry.addBucketForFluid(fluidVolcanicLava);
        if(event.getSide() == Side.CLIENT)
        {
//            final ModelResourceLocation volcanicLavaLocation = new ModelResourceLocation(Adversity.MODID.toLowerCase() + ":block/fluids", "volcanic_lava");

  //          Item volcanicLavaItem = Item.getItemFromBlock(blockVolcanicLava);
             // no need to pass the locations here, since they'll be loaded by the block model logic.
  //          ModelBakery.registerItemVariants(volcanicLavaItem);
//            ModelLoader.setCustomMeshDefinition(volcanicLavaItem, new ItemMeshDefinition()
//            {
//                public ModelResourceLocation getModelLocation(ItemStack stack)
//                {
//                    return volcanicLavaLocation;
//                }
//            });
     
//            ModelLoader.setCustomStateMapper(blockVolcanicLava, new StateMapperBase()
//            {
//                protected ModelResourceLocation getModelResourceLocation(IBlockState state)
//                {
//                    return volcanicLavaLocation;
//                }
//            });
        }

		// ITEMS
//		itemVolcanicLavaBucket = new ItemVolcanicLavaBucket(Volcano.blockVolcanicLava);
//		GameRegistry.registerItem(itemVolcanicLavaBucket, "ItemVolcanicLavaBucket");
//		FluidContainerRegistry.registerFluidContainer(Volcano.fluidVolcanicLava, new ItemStack(itemVolcanicLavaBucket),
//				new ItemStack(Items.bucket));

		
		// TILE ENTITIES
		Volcano.blockVolcano = new BlockVolcano();
//		GameRegistry.registerBlock(Volcano.blockVolcano, "BlockVolcano");
		GameRegistry.registerTileEntity(TileVolcano.class, "TileVolcano");
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
