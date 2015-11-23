package grondag.adversity.feature.volcano;

import net.minecraft.block.BlockCactus;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
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
import grondag.adversity.library.NiceBigBlockModel;
import grondag.adversity.library.NiceBlock.EnumStyle;
import grondag.adversity.library.NiceItemBlock2;
import grondag.adversity.library.TextureLoader;
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
	public static BlockBasalt2		blockBasalt2;
	public static NiceBigBlockModel	blockBasaltBBM;
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
		blockBasalt2 = (BlockBasalt2) new BlockBasalt2().setUnlocalizedName("basalt2").setCreativeTab(Adversity.tabAdversity);
		GameRegistry.registerBlock(blockBasalt, ItemBlockBasalt.class, "basalt");
		GameRegistry.registerBlock(blockBasalt2, NiceItemBlock2.class, "basalt2");
		
		if(event.getSide()==Side.CLIENT){
			
			 MinecraftForge.EVENT_BUS.register(new TextureLoader("basalt", 256));
			 
			blockBasaltBBM = new NiceBigBlockModel("basalt");
			
		    StateMapperBase ignoreState = new StateMapperBase() {
		        @Override
		        protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
		          return blockBasaltBBM.modelResourceLocation;
		        }
		      };
		      
		      ModelLoader.setCustomStateMapper(blockBasalt2, ignoreState);

		      // ModelBakeEvent will be used to add our ISmartBlockModel to the ModelManager's registry (the
		      //  registry used to map all the ModelResourceLocations to IBlockModels).  For the stone example there is a map from
		      // ModelResourceLocation("minecraft:granite#normal") to an IBakedModel created from models/block/granite.json.
		      // For the camouflage block, it will map from
		      // CamouflageISmartBlockModelFactory.modelResourceLocation to our CamouflageISmartBlockModelFactory instance
		      MinecraftForge.EVENT_BUS.register(blockBasaltBBM);
			 
			 
		    // Need this to filter out unused state combinations
			// otherwise model loader will try to create hundreds of meaningless combinations.
			ModelLoader.setCustomStateMapper(Volcano.blockBasalt,  blockBasalt.new CustomStateMapper());
//			ModelLoader.setCustomStateMapper(Volcano.blockBasalt2,  (new StateMap.Builder()).addPropertiesToIgnore(new IProperty[] {BlockBasalt2.PROP_STYLE}).build());
			
			Item itemBlockVariants = GameRegistry.findItem("adversity", "basalt");
	
		    // need to add the variants to the bakery so it knows what models are available for rendering the different subtypes
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.ROUGH.toString());
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.SMOOTH.toString());
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.COLUMN_X.toString());
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.BRICK_BIG_A.toString());

			itemBlockVariants = GameRegistry.findItem("adversity", "basalt2");
			
		    // need to add the variants to the bakery so it knows what models are available for rendering the different subtypes
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt2_" + EnumStyle.ROUGH.toString());
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt2_" + EnumStyle.SMOOTH.toString());
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt2_" + EnumStyle.BRICK_BIG_A.toString());
		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt2_" + EnumStyle.BLOCK_BIG_A.toString());

		
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

// if you want to use your own texture, you can add it to the texture map using code similar to this:
//   MinecraftForge.EVENT_BUS.register(new StitcherAddDigitsTexture());
