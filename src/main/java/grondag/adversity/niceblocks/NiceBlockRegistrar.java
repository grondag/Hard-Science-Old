package grondag.adversity.niceblocks;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
import grondag.adversity.niceblocks.client.NiceModel;

public class NiceBlockRegistrar {

	//define our substance groupings
	private static final NiceSubstance[][] substance16Group =
			{{NiceSubstance.BASALT}};
	
	// declare the block instances
	public final static NiceBlock raw1 = new NiceBlock("raw1", NiceBlockStyle.RAW, substance16Group[0]);
	public final static NiceBlock smooth1 = new NiceBlock("smooth1", NiceBlockStyle.SMOOTH, substance16Group[0]);
	public final static NiceBlock bigBlockA1 = new NiceBlock("bigBlockA1", NiceBlockStyle.BIG_WORN, substance16Group[0]);
	public final static NiceBlock bigBlockB1 = new NiceBlock("bigBlockB1", NiceBlockStyle.BIG_WORN, substance16Group[0]);
	public final static NiceBlock bigBlockC1 = new NiceBlock("bigBlockC1", NiceBlockStyle.BIG_WORN, substance16Group[0]);
	public final static NiceBlock bigBlockD1 = new NiceBlock("bigBlockD1", NiceBlockStyle.BIG_WORN, substance16Group[0]);
	public final static NiceBlock bigBlockE1 = new NiceBlock("bigBlockE1", NiceBlockStyle.BIG_WORN, substance16Group[0]);
	
	private final static NiceBlockRegistrar instance = new NiceBlockRegistrar();
	
	/**
	 * NiceBlocks add themselves here so that we can easily iterate them during registration
	 */
	public static LinkedList<NiceBlock> allBlocks = new LinkedList<NiceBlock>();
	
	private static LinkedList<NiceModel> allModels = new LinkedList<NiceModel>();
	private static Map<String, NiceBlock> lookupSnS = new HashMap<String, NiceBlock>();
	
	private static void registerBlockCompletely(NiceBlock block, boolean doClientStuff){
		
		// actually register the block! Hurrah!
		GameRegistry.registerBlock(block, NiceItemBlock2.class, block.getUnlocalizedName());

		// Blocks need custom state mapper for two reasons
		// 1) To avoid creating mappings for unused substance indexes (metadata values)
		// 2) To point them to our custom model instead of looking for a json file
		ModelLoader.setCustomStateMapper(block,  NiceBlockStateMapper.instance);
		
		
		// iterate all substance variance and add to collections for later handling
		for(NiceSubstance substance: block.substances){
			String location = block.style.getResourceLocationForSubstance(substance);
			
			lookupSnS.put(location, block);

			if(doClientStuff){
				// Create model for later event handling.
				// Java gonna make us jump through a bunch of hoops - hold on to your butts!
				Constructor<?> ctor;
				try {
					ctor = block.style.modelClass.getConstructor(NiceBlockStyle.class, NiceSubstance.class);
					
					try {
						NiceModel model = (NiceModel)ctor.newInstance(block.style, substance);
						allModels.add(model);
						
					} catch (InstantiationException e) {
						Adversity.log.warn("Unable to instantiate block model for class style/substance:" + location);
					} catch (IllegalAccessException e) {
						Adversity.log.warn("Unable to access instantiation for block model for class style/substance:" + location);
					} catch (IllegalArgumentException e) {
						Adversity.log.warn("Bad argument while instantiating block model for class style/substance:" + location);
					} catch (InvocationTargetException e) {
						Adversity.log.warn("Exception happened while instantiating block model for class style/substance:" + location);
					}
					
				} catch (NoSuchMethodException e) {
					Adversity.log.warn("Unable to find constructor for block model class for style/substance:" + location);
				} catch (SecurityException e) {
					Adversity.log.warn("Unable to access constructor for block model class for style/substance:" + location);
				}
				

			}
		}
	
	}
	
	public static NiceBlock getBlockForStyleAndSubstance(NiceBlockStyle style, NiceSubstance substance){
		return lookupSnS.get(style.getResourceLocationForSubstance(substance));
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
			registerBlockCompletely(block, event.getSide()==Side.CLIENT);
		}
		
		if(event.getSide()==Side.CLIENT){
			
			// Register handlers for texture stitch and model bake events (they are in this class)
			MinecraftForge.EVENT_BUS.register(instance);
			 
			
			//TODO:  ADD ITEM VARIENTS FOR SUB BLOCKS
//			Item itemBlockVariants = GameRegistry.findItem("adversity", "basalt");
//	
//		    // need to add the variants to the bakery so it knows what models are available for rendering the different subtypes
//		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.ROUGH.toString());
//		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.SMOOTH.toString());
//		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.COLUMN_X.toString());
//		    ModelBakery.addVariantName(itemBlockVariants, "adversity:basalt_" + EnumStyle.BRICK_BIG_A.toString());

		
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
