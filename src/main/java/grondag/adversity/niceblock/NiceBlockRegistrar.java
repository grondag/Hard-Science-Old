package grondag.adversity.niceblock;

import grondag.adversity.Adversity;
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
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
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
	 * NiceBlockModels contained here for handling during model bake and texture
	 * stitch
	 */
	private static LinkedList<ModelRegistration> allModels = new LinkedList<ModelRegistration>();

	/**
	 * Supports the getBlock(s)ForStyleAndSubstance methods
	 */
	private static Multimap<String, NiceBlock> lookupSnM = HashMultimap.create();

	// declare the block instances
	public static final NiceBlock raw1 = new NiceBlock(NiceStyle.RAW, new PlacementSimple(), 
			BaseMaterial.FLEXSTONE, 1);
			
	public static final NiceBlock smooth1 = new NiceBlock(NiceStyle.SMOOTH, 
			new PlacementSimple(), BaseMaterial.FLEXSTONE, 1);
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

	public static final NiceBlock columnSquareX1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_X,
			NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
	public static final NiceBlock columnSquareY1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_Y,
			NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
	public static final NiceBlock columnSquareZ1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_Z,
			NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);

	public static final NiceBlockNonCubic columnRoundX1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_X,
			NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);
	public static final NiceBlockNonCubic columnRoundY1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_Y,
			NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);
	public static final NiceBlockNonCubic columnRoundZ1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_Z,
			NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);

//	public static final NiceBlock hotBasalt = new NiceBlockHotBasalt("hot_basalt", NiceStyle.HOT_BASALT, new PlacementSimple(),
//			substance16Group[1]);

	public static final NiceBlock hotBasalt = new NiceBlockHotBasalt(NiceStyle.HOT_BASALT, 
			new PlacementSimple(), BaseMaterial.FLEXSTONE, 1);

	public static final NiceBlock bigTex = new NiceBlockPlus(NiceStyle.BIG_TEX, 
			new PlacementSimple(), BaseMaterial.FLEXSTONE, 16);
	
	/**
	 * Use to generate model resource location names with a consistent
	 * convention.
	 */
	public static String getModelResourceNameFromMeta(NiceBlock block, int meta) {
		return Adversity.MODID + ":" + block.getUnlocalizedName() + "." + meta;
	}

	/**
	 * Handles all the plumbing needed to make a block work except for the
	 * instantiation. It should never be necessary to call this method directly.
	 * This is called during pre-init for every block in allBlocks collection.
	 * NiceBlocks add themselves to the allBlocks collection automatically at
	 * instantiation.
	 */
	private static void registerBlockCompletely(NiceBlock block, FMLPreInitializationEvent event) {

		// actually register the block! Hurrah!
		GameRegistry.registerBlock(block, null, block.getUnlocalizedName());
		GameRegistry.registerItem(block.item, block.getUnlocalizedName());
		GameData.getBlockItemMap().put(block, block.item);

		// iterate all substance variants and add to collections for later
		// handling
		for (int i = 0; i < block.metaCount; i++) {

			lookupSnM.put(getSnMkey(block.style, i), block);

			if (event.getSide() == Side.CLIENT) {

				ModelResourceLocation mrlBlock = new ModelResourceLocation(getModelResourceNameFromMeta(block, i));
				ModelResourceLocation mrlItem = new ModelResourceLocation(getModelResourceNameFromMeta(block, i),
						"inventory");

				ModelLoader.setCustomModelResourceLocation(block.item, i, mrlItem);

				// Blocks need custom state mapper for two reasons
				// 1) To avoid creating mappings for unused substance indexes
				// (metadata values)
				// 2) To point them to our custom model instead of looking for a
				// json file
				ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper.instance);

				// prevents console spam about missing item models
				ModelBakery.addVariantName(block.item, getModelResourceNameFromMeta(block, i));

				// Create model for later event handling.
				NiceModel model = block.style.getModelController().getModel(i);
				allModels.add(new ModelRegistration(model, mrlBlock, mrlItem));

			}
		}
	}

	/**
	 * Provides consistent key construction for style/meta lookup.
	 */
	private static String getSnMkey(NiceStyle style, int meta) {
		return style.toString() + "." + meta;
	}

	/**
	 * Used to find sibling blocks for blocks that are part of a group with the
	 * same style and meta.
	 */
	public static Collection<NiceBlock> getBlocksForStyleAndMeta(NiceStyle style, int meta) {
		return lookupSnM.get(getSnMkey(style, meta));
	}

	/**
	 * Returns the first block with the same meta and substance. For most
	 * blocks, there will be only one result. If there is more than one, this
	 * method will yell at you and return the first result. For sibling blocks
	 * that have multiple blocks the same meta and substance, use
	 * getBlocksForStyleAndMeta.
	 */
	public static NiceBlock getBlockForStyleAndMeta(NiceStyle style, int meta) {
		Collection<NiceBlock> blocks = getBlocksForStyleAndMeta(style, meta);
		if (blocks.isEmpty()) {
			return null;
		} else {
			if (blocks.size() > 1) {
				Adversity.log
				.warn("getFirstBlockForStyleAndMeta found more than one block! This should not normally happen. "
						+ "style = " + style + ", meta = " + meta);
			}
			return blocks.toArray(new NiceBlock[0])[0];
		}
	}

	public static void preInit(FMLPreInitializationEvent event) {

		// In case we get called more than 1X.
		lookupSnM.clear();
		allModels.clear();

		// REGISTER ALL BLOCKS
		for (NiceBlock block : allBlocks) {
			registerBlockCompletely(block, event);
		}

		 GameRegistry.registerTileEntity(NiceTileEntity.class, "nicetileentity");
		 
		if (event.getSide() == Side.CLIENT) {

			// Register handlers for texture stitch and model bake events (they
			// are in this class)
			MinecraftForge.EVENT_BUS.register(instance);

			/**
			 * Register custom block highlighter for blocks with irregular
			 * hitboxes.
			 */
			MinecraftForge.EVENT_BUS.register(NiceBlockHighlighter.instance);
		}

	}

	/**
	 * Centralized event handler for NiceModel baking.
	 */
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) throws IOException {
		for (ModelRegistration reg : allModels) {
			reg.model.handleBakeEvent(event);
			event.modelRegistry.putObject(reg.mrlBlock, reg.model);
			event.modelRegistry.putObject(reg.mrlItem, reg.model);
		}
	}

	/**
	 * Centralized event handler for NiceModel texture stitch.
	 */
	@SubscribeEvent
	public void stitcherEventPre(TextureStitchEvent.Pre event) {
		for (ModelRegistration reg : allModels) {
			reg.model.handleTexturePreStitch(event);
		}
	}
	
	/**
	 * Centralized event handler for NiceModel texture stitch.
	 */
	@SubscribeEvent
	public void stitcherEventPost(TextureStitchEvent.Post event) {
		for (ModelRegistration reg : allModels) {
			reg.model.handleTexturePostStitch(event);
		}
	}

	/**
	 * Contains stuff we need to replace model references during model bake.
	 */
	private static class ModelRegistration{
		public final NiceModel model;
		public final ModelResourceLocation mrlBlock;
		public final ModelResourceLocation mrlItem;

		public ModelRegistration(NiceModel model, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem){
			this.model = model;
			this.mrlBlock = mrlBlock;
			this.mrlItem = mrlItem;
		}
	}

}
