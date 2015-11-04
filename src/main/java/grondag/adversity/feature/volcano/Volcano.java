package grondag.adversity.feature.volcano;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import grondag.adversity.Adversity;
import grondag.adversity.Config;
import grondag.adversity.feature.volcano.BlockBasalt.EnumStyle;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;



public class Volcano {

	// BIOMES
	public static BiomeGenBase		volcano;

	// BLOCKS
	public static BlockBasalt		blockBasalt;
	//public static BlockHotBasalt	blockHotBasalt;
	//public static BlockVolcanicLava	blockVolcanicLava;
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

		// BLOCKS
		blockBasalt = (BlockBasalt) new BlockBasalt().setUnlocalizedName("basalt").setCreativeTab(Adversity.tabAdversity);
		GameRegistry.registerBlock(blockBasalt, ItemBlockBasalt.class, "basalt");
		
		if(event.getSide()==Side.CLIENT){
			
		    // Need this to filter out unused state combinations
			// otherwise model loader will try to create hundreds of meaningless combinations.
			ModelLoader.setCustomStateMapper(Volcano.blockBasalt,  blockBasalt.new CustomStateMapper());

			
			Item itemBlockVariants = GameRegistry.findItem("adversity", "basalt");
	
		    // need to add the variants to the bakery so it knows what models are available for rendering the different subtypes
		    EnumStyle[] allStyles = EnumStyle.values();
		    for (EnumStyle style : allStyles) {
		    	ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + style.toString());
		    }
		}
		
		//GameRegistry.registerBlock(Volcano.blockHotBasalt = new BlockHotBasalt("HotBasalt", Material.rock),
		//		ItemBlockHotBasalt.class, "HotBasalt");

		//GameRegistry.registerBlock(Volcano.blockHaze = new BlockHaze(Material.air), "Haze");
		//GameRegistry.registerBlock(Volcano.blockHazeRising = new BlockHazeRising(Material.glass), "HazeRising");
		//GameRegistry.registerBlock(Volcano.blockAsh = new BlockAsh("Ash", Material.snow), "Ash");


		// FLUIDS
//		Volcano.fluidVolcanicLava = new Fluid("Volcanic Lava").setLuminosity(15).setDensity(3000).setViscosity(6000)
//				.setTemperature(1300);
//		FluidRegistry.registerFluid(Volcano.fluidVolcanicLava);
//		Volcano.blockVolcanicLava = (BlockVolcanicLava) new BlockVolcanicLava(Volcano.fluidVolcanicLava, Material.lava)
//				.setBlockName("VolcanicLava");
//		GameRegistry.registerBlock(Volcano.blockVolcanicLava, "VolcanicLava");
//		Volcano.fluidVolcanicLava.setUnlocalizedName(Volcano.blockVolcanicLava.getUnlocalizedName());

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
		
		if(event.getSide()==Side.CLIENT){
			
		    Item itemBlockVariants = GameRegistry.findItem("adversity", "basalt");
		    ModelResourceLocation itemModelResourceLocation;
		    
		    // need to add the variants to the bakery so it knows what models are available for rendering the different subtypes
		    EnumStyle[] allStyles = EnumStyle.values();
		    for (EnumStyle style : allStyles) {
		    	itemModelResourceLocation = new ModelResourceLocation("adversity:basalt_" + style.toString(), "inventory");
			    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlockVariants, style.getMetadata(), itemModelResourceLocation);
		    }
		    		    
		}
		
		
	}

	public static void postInit(FMLPostInitializationEvent event) {

		// in vanilla worlds, plants shouldn't stop volcanic lava from spreading
		// TODO: probably need to add some more blocks here
		Blocks.fire.setFireInfo(Blocks.cactus, 5, 5);
		Blocks.fire.setFireInfo(Blocks.deadbush, 30, 100);
	}

}

