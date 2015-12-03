package grondag.adversity.niceblocks;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import grondag.adversity.Adversity;
import grondag.adversity.niceblocks.client.NiceCookbook;
import grondag.adversity.niceblocks.client.NiceCookbookColumn;
import grondag.adversity.niceblocks.client.NiceCookbookConnectedCorners;
import grondag.adversity.niceblocks.client.NiceCookbookMasonry;
import grondag.adversity.niceblocks.client.NiceModel;
import grondag.adversity.niceblocks.NicePlacement.PlacementSimple;
import grondag.adversity.niceblocks.NicePlacement.PlacementBigBlock;

public class NiceBlockRegistrar {
	
	private final static NiceBlockRegistrar instance = new NiceBlockRegistrar();
	
	/**
	 * NiceBlocks add themselves here so that we can easily iterate them during registration
	 */
	public static LinkedList<NiceBlock> allBlocks = new LinkedList<NiceBlock>();
	
	private static LinkedList<NiceModel> allModels = new LinkedList<NiceModel>();
	private static Multimap<String, NiceBlock> lookupSnS = HashMultimap.create();


	//define our substance groupings
	private static final NiceSubstance[][] substance16Group =
			{{NiceSubstance.BASALT}};
	
	// declare the block instances
	public final static NiceBlock raw1 = new NiceBlock("raw_1", NiceBlockStyle.RAW, new PlacementSimple(), substance16Group[0]);
	public final static NiceBlock smooth1 = new NiceBlock("smooth_1", NiceBlockStyle.SMOOTH, new PlacementSimple(), substance16Group[0]);
	public final static NiceBlock largeBrick1 = new NiceBlock("large_brick_1", NiceBlockStyle.LARGE_BRICKS, new PlacementSimple(), substance16Group[0]);
	public final static NiceBlock smallBrick1 = new NiceBlock("small_brick_1", NiceBlockStyle.SMALL_BRICKS, new PlacementSimple(), substance16Group[0]);
	public final static NiceBlock bigBlockA1 = new NiceBlock("big_block_a_1", NiceBlockStyle.BIG_WORN, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock bigBlockB1 = new NiceBlock("big_block_b_1", NiceBlockStyle.BIG_WORN, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock bigBlockC1 = new NiceBlock("big_block_c_1", NiceBlockStyle.BIG_WORN, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock bigBlockD1 = new NiceBlock("big_block_d_1", NiceBlockStyle.BIG_WORN, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock bigBlockE1 = new NiceBlock("big_block_e_1", NiceBlockStyle.BIG_WORN, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock bigWeathered1 = new NiceBlock("big_weathered_1", NiceBlockStyle.BIG_WEATHERED, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock bigOrnate1 = new NiceBlock("big_ornate_1", NiceBlockStyle.BIG_ORNATE, new PlacementBigBlock(), substance16Group[0]);
	public final static NiceBlock masonryA1 = new NiceBlock("masonry_a_1", NiceBlockStyle.MASONRY_A, NiceBlockStyle.makeMasonryPlacer(), substance16Group[0]);
	public final static NiceBlock masonryB1 = new NiceBlock("masonry_b_1", NiceBlockStyle.MASONRY_B, NiceBlockStyle.makeMasonryPlacer(), substance16Group[0]);
	public final static NiceBlock masonryC1 = new NiceBlock("masonry_c_1", NiceBlockStyle.MASONRY_C, NiceBlockStyle.makeMasonryPlacer(), substance16Group[0]);
	public final static NiceBlock masonryD1 = new NiceBlock("masonry_d_1", NiceBlockStyle.MASONRY_D, NiceBlockStyle.makeMasonryPlacer(), substance16Group[0]);
	public final static NiceBlock masonryE1 = new NiceBlock("masonry_e_1", NiceBlockStyle.MASONRY_E, NiceBlockStyle.makeMasonryPlacer(), substance16Group[0]);

	public final static NiceBlock columnX1 = new NiceBlock("column_x_1", NiceBlockStyle.COLUMN_X, NiceBlockStyle.makeColumnPlacer(), substance16Group[0]);	
	public final static NiceBlock columnY1 = new NiceBlock("column_y_1", NiceBlockStyle.COLUMN_Y, NiceBlockStyle.makeColumnPlacer(), substance16Group[0]);
	public final static NiceBlock columnZ1 = new NiceBlock("column_z_1", NiceBlockStyle.COLUMN_Z, NiceBlockStyle.makeColumnPlacer(), substance16Group[0]);

	
	private static void registerBlockCompletely(NiceBlock block, FMLPreInitializationEvent event){
		
		// actually register the block! Hurrah!
		GameRegistry.registerBlock(block, block.name);

		// Blocks need custom state mapper for two reasons
		// 1) To avoid creating mappings for unused substance indexes (metadata values)
		// 2) To point them to our custom model instead of looking for a json file
		ModelLoader.setCustomStateMapper(block,  NiceBlockStateMapper.instance);
		
		
		// iterate all substance variants and add to collections for later handling
		for(int i = 0; i< block.substances.length; i++){
			ModelResourceLocation mrlBlock = NiceBlockStateMapper.instance.getModelResourceLocation(
					block.getDefaultState().withProperty(NiceBlock.PROP_SUBSTANCE_INDEX, i));
			ModelResourceLocation mrlItem = new ModelResourceLocation(block.name, "inventory");
			
			
			lookupSnS.put(getSnSkey(block.style, block.substances[i]), block);

			if(event.getSide()==Side.CLIENT){
				// Create model for later event handling.
				// Java gonna make us jump through a bunch of hoops - hold on to your butts!
				// TODO: finding constructor should probably be outside loop
				Constructor<?> ctor;
				try {
					ctor = block.style.modelClass.getConstructor(NiceBlockStyle.class, NiceSubstance.class, 
							ModelResourceLocation.class, ModelResourceLocation.class);
					
					try {
						NiceModel model = (NiceModel)ctor.newInstance(block.style, block.substances[i], mrlBlock, mrlItem);
						allModels.add(model);
						
					} catch (InstantiationException e) {
						Adversity.log.warn("Unable to instantiate block model for:" + mrlBlock);
					} catch (IllegalAccessException e) {
						Adversity.log.warn("Unable to access instantiation for block model for:" + mrlBlock);
					} catch (IllegalArgumentException e) {
						Adversity.log.warn("Bad argument while instantiating block model for:" + mrlBlock);
					} catch (InvocationTargetException e) {
						Adversity.log.warn("Exception happened while instantiating block model for:" + mrlBlock);
					}
					
				} catch (NoSuchMethodException e) {
					Adversity.log.warn("Unable to find constructor for block model class for:" + mrlBlock);
				} catch (SecurityException e) {
					Adversity.log.warn("Unable to access constructor for block model class for:" + mrlBlock);
				}
				
				
				// ADD ITEM VARIANTS FOR SUB BLOCKS
				// TODO: finding item should probably be outside loop
				Item itemVariant = Item.getItemFromBlock(block);
				
				ModelLoader.setCustomModelResourceLocation(itemVariant, i, mrlItem);

			}
		}
	}
	
	private static String getSnSkey(NiceBlockStyle style, NiceSubstance substance){
		return style.toString() + "." + substance.id;
	}
	public static Collection<NiceBlock> getBlocksForStyleAndSubstance(NiceBlockStyle style, NiceSubstance substance){
		return lookupSnS.get(getSnSkey(style, substance)); 
	}

	/** 
	 * Returns the first block with the same style and substance. 
	 * For most blocks, there will be only one result and this method
	 * will yell at your if there is more than one and return the first result.
	 * For sibling blocks that have multiple blocks the same style and substance,
	 * use getBlocksForStyleAndSubstace.
	 */
	public static NiceBlock getBlockForStyleAndSubstance(NiceBlockStyle style, NiceSubstance substance){
		Collection<NiceBlock> blocks = getBlocksForStyleAndSubstance(style, substance);
		if(blocks.isEmpty()){
			return null;
		} else {
			if(blocks.size() > 1){
				Adversity.log.warn("getFirstBlockForStyleAndSubstance found more than one block! This should not normally happen. "
						+ "style = " + style + ", substance = " + substance);
			}
			return blocks.toArray(new NiceBlock[0])[0];
		}
	}
	
	private static String getKeyForStyleAndSubstance(NiceBlockStyle style, NiceSubstance substance){
		return style.toString() + substance.id;
	}
	
	public static void preInit(FMLPreInitializationEvent event) {

		// In case we get called more than 1X.
		lookupSnS.clear();
		allModels.clear();
		
		// REGISTER ALL BLOCKS
		for(NiceBlock block : allBlocks){
			registerBlockCompletely(block, event);
		}
		
		if(event.getSide()==Side.CLIENT){
			
			// Register handlers for texture stitch and model bake events (they are in this class)
			MinecraftForge.EVENT_BUS.register(instance);
		
		}

	}
	
	public static void init(FMLInitializationEvent event) {
		
		if(event.getSide()==Side.CLIENT){
			
			//TODO: FIgure out how the hell items work
//		    Item itemBlockVariants = GameRegistry.findItem("adversity", "basalt");
//		    ModelResourceLocation itemModelResourceLocation;
//		    
//		    // need to add the variants to the bakery so it knows what models are available for rendering the different subtypes
//		    EnumStyle[] allStyles = EnumStyle.values();
//		    for (EnumStyle style : allStyles) {
//		    	itemModelResourceLocation = new ModelResourceLocation("adversity:basalt_" + style.toString(), "inventory");
//			    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlockVariants, style.getMetadata(), itemModelResourceLocation);
//		    }
		    		    
		}
		
		
	}


	
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) throws IOException
	{
		for(NiceModel model : allModels){
			model.handleBakeEvent(event);
		}
	}
	
	@SubscribeEvent
	public void stitcherEventPre(TextureStitchEvent.Pre event) {
		for(NiceModel model : allModels){
			model.handleTextureStitchEvent(event);
		}
	}

	
}