package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlockHotBasalt;
import grondag.adversity.niceblock.NiceBlockNonCubic;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.model.NiceModel;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper;
import grondag.adversity.niceblock.support.NicePlacement;
import grondag.adversity.niceblock.support.NicePlacement.PlacementBigBlock;
import grondag.adversity.niceblock.support.NicePlacement.PlacementSimple;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Contains instances for all NiceBlocks and handles all creation and
 * registration of same. Includes handling of associated items, textures and
 * models. It is also the subscriber for model bake and texture stitch events,
 * but handles these simply by calling handler methods on the models associated
 * with the blocks.
 */
public class NiceBlockRegistrar {

	private static final NiceBlockRegistrar instance = new NiceBlockRegistrar();

	/**
	 * NiceBlocks add themselves here so that we can easily iterate them during
	 * registration
	 */
	public static LinkedList<NiceBlock> allBlocks = new LinkedList<NiceBlock>();

	/**
	 * Model dispatchers add themselves here for handling during model bake and texture stitch
	 */
	public static LinkedList<ModelDispatcher> allDispatchers = new LinkedList<ModelDispatcher>();

	// DECLARE MODEL DISPATCH INSTANCES
	
	// DECLARE BLOCK INSTANCES
	
	
	// declare the block instances
//	public static final NiceBlock raw1 = new NiceBlock(NiceStyle.RAW, new PlacementSimple(), 
//			BaseMaterial.FLEXSTONE, 1);
//			
//	public static final NiceBlock smooth1 = new NiceBlock(NiceStyle.SMOOTH, 
//			new PlacementSimple(), BaseMaterial.FLEXSTONE, 1);
//	public static final NiceBlock largeBrick1 = new NiceBlock("large_brick_1", NiceStyle.LARGE_BRICKS,
//			new PlacementSimple(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock smallBrick1 = new NiceBlock("small_brick_1", NiceStyle.SMALL_BRICKS,
//			new PlacementSimple(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigBlockA1 = new NiceBlock("big_block_a_1", NiceStyle.BIG_WORN,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigBlockB1 = new NiceBlock("big_block_b_1", NiceStyle.BIG_WORN,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigBlockC1 = new NiceBlock("big_block_c_1", NiceStyle.BIG_WORN,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigBlockD1 = new NiceBlock("big_block_d_1", NiceStyle.BIG_WORN,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigBlockE1 = new NiceBlock("big_block_e_1", NiceStyle.BIG_WORN,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigWeathered1 = new NiceBlock("big_weathered_1", NiceStyle.BIG_WEATHERED,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock bigOrnate1 = new NiceBlock("big_ornate_1", NiceStyle.BIG_ORNATE,
//			new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock masonryA1 = new NiceBlock("masonry_a_1", NiceStyle.MASONRY_A,
//			NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock masonryB1 = new NiceBlock("masonry_b_1", NiceStyle.MASONRY_B,
//			NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock masonryC1 = new NiceBlock("masonry_c_1", NiceStyle.MASONRY_C,
//			NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock masonryD1 = new NiceBlock("masonry_d_1", NiceStyle.MASONRY_D,
//			NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
//	public static final NiceBlock masonryE1 = new NiceBlock("masonry_e_1", NiceStyle.MASONRY_E,
//			NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);

//	public static final NiceBlock columnSquareX1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_X,
//			NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
//	public static final NiceBlock columnSquareY1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_Y,
//			NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
//	public static final NiceBlock columnSquareZ1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_Z,
//			NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
//
//	public static final NiceBlockNonCubic columnRoundX1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_X,
//			NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);
//	public static final NiceBlockNonCubic columnRoundY1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_Y,
//			NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);
//	public static final NiceBlockNonCubic columnRoundZ1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_Z,
//			NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);

//	public static final NiceBlock hotBasalt = new NiceBlockHotBasalt("hot_basalt", NiceStyle.HOT_BASALT, new PlacementSimple(),
//			substance16Group[1]);

//	public static final NiceBlock hotBasalt = new NiceBlockHotBasalt(NiceStyle.HOT_BASALT, 
//			new PlacementSimple(), BaseMaterial.FLEXSTONE, 1);
//
//	public static final NiceBlock bigTex = new NiceBlockPlus(NiceStyle.BIG_TEX, 
//			new PlacementSimple(), BaseMaterial.FLEXSTONE, 16);
	
	/**
	 * Use to generate model resource location names with a consistent
	 * convention.
	 */
//	public static String getModelResourceNameForBlock(NiceBlock block) {
//		return Adversity.MODID + ":" + block.getUnlocalizedName();
//	}

	/**
	 * Handles all the plumbing needed to make a block work except for the
	 * instantiation. It should never be necessary to call this method directly.
	 * This is called during pre-init for every block in allBlocks collection.
	 * NiceBlocks add themselves to the allBlocks collection automatically at
	 * instantiation.
	 */
	/**
	private static void registerBlockCompletely(NiceBlock block, FMLPreInitializationEvent event) {

		// actually register the block! Hurrah!
		GameRegistry.registerBlock(block, NiceItemBlock.class);
		block.item.registerSelf();

		if (event.getSide() == Side.CLIENT) {

//			ModelResourceLocation mrlBlock = new ModelResourceLocation(getModelResourceNameForBlock(block));
			
	         // Blocks need custom state mapper for two reasons
            // 1) To avoid creating mappings for unused substance indexes
            // (metadata values)
            // 2) To point them to our custom model instead of looking for a
            // json file
            ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper.instance);
            
//			ModelResourceLocation mrlItem = new ModelResourceLocation(getModelResourceNameFromMeta(block, i),
//					"inventory");

//			ModelLoader.setCustomModelResourceLocation(block.item, i, mrlItem);



			// prevents console spam about missing item models
			//ModelBakery.addVariantName(block.item, getModelResourceNameFromMeta(block, i));
			
//			ModelBakery.registerItemVariants(item, names);

			// Create model for later event handling.
//			NiceModel model = block.style.getModelController().getModel(i);
//			allModels.add(new ModelRegistration(model, mrlBlock, mrlItem));
			
			
//	        ModelResourceLocation blueModel = new ModelResourceLocation(getRegistryName() + "_blue", "inventory");
//	        ModelResourceLocation redModel = new ModelResourceLocation(getRegistryName() + "_red", "inventory");
//
//	        ModelBakery.registerItemVariants(this, blueModel, redModel);
//
//	        ModelLoader.setCustomMeshDefinition(this, stack -> {
//	            if (isBlue(stack)) {
//	                return blueModel;
//	            } else {
//	                return redModel;
//	            }
//	        });

		}
	}
	*/

	public static void preInit(FMLPreInitializationEvent event) {

		// REGISTER ALL BLOCKS
		for (NiceBlock block : allBlocks) {
		    GameRegistry.registerBlock(block, NiceItemBlock.class);
		    block.item.registerSelf();

	        if (event.getSide() == Side.CLIENT) {
	            ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper.instance);
	        }
		}

		GameRegistry.registerTileEntity(NiceTileEntity.class, "nicetileentity");
		 
		if (event.getSide() == Side.CLIENT) {

			// Register event handlers for nice blocks (they are in this class)
			MinecraftForge.EVENT_BUS.register(instance);

			// Register custom block highlighter for blocks with irregular hitboxes.
			MinecraftForge.EVENT_BUS.register(NiceBlockHighlighter.instance);
		}

	}

	public static void init(FMLInitializationEvent event)
	{
	       for (NiceBlock block : allBlocks) {
	           ModelResourceLocation itemModelResourceLocation 
	               = new ModelResourceLocation(block.blockModelHelper.dispatcher.getModelResourceString(), "inventory");
	           for(int i = 0; i < block.blockModelHelper.getMetaCount(); i++)
	           {
	               ModelLoader.setCustomModelResourceLocation(block.item, i, itemModelResourceLocation);
	           }
	       }

    }
        
	/**
	 * Centralized event handler for NiceModel baking.
	 */
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) throws IOException {
		for (ModelDispatcher dispatcher : allDispatchers) {
		    dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);
			event.modelRegistry.putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
		}
	}

	/**
	 * Centralized event handler for NiceModel texture stitch.
	 */
	@SubscribeEvent
	public void stitcherEventPre(TextureStitchEvent.Pre event) {
		for (ModelDispatcher dispatcher : allDispatchers) {
		    dispatcher.controller.handleTexturePreStitch(event);
		}
		for (NiceColor color : NiceColor.values())
		{
		    event.map.registerSprite(new ResourceLocation(color.getParticleTextureName()));
		}
	}
	
//	/**
//	 * Centralized event handler for NiceModel texture stitch.
//	 */
//	@SubscribeEvent
//	public void stitcherEventPost(TextureStitchEvent.Post event) {
//		for (ModelRegistration reg : allModels) {
//			reg.model.handleTexturePostStitch(event);
//		}
//	}

//	/**
//	 * Contains stuff we need to replace model references during model bake.
//	 */
//	private static class ModelRegistration{
//		public final NiceModel model;
//		public final ModelResourceLocation mrlBlock;
//		public final ModelResourceLocation mrlItem;
//
//		public ModelRegistration(NiceModel model, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem){
//			this.model = model;
//			this.mrlBlock = mrlBlock;
//			this.mrlItem = mrlItem;
//		}
//	}

}
