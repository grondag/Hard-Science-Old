package grondag.hard_science.init;

import java.io.IOException;
import java.util.Map;

import grondag.hard_science.HardScience;
import grondag.hard_science.Configurator;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.texture.CompressedAnimatedSprite;
import grondag.hard_science.superblock.texture.TextureLayout;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.varia.SuperDispatcher;
import grondag.hard_science.superblock.varia.SuperStateMapper;
import grondag.hard_science.superblock.varia.SuperDispatcher.DispatchDelegate;
import grondag.hard_science.virtualblock.VirtualBlock;
import grondag.hard_science.virtualblock.VirtualBlockBakedModel;
import grondag.hard_science.virtualblock.VirtualBlockTESR;
import grondag.hard_science.virtualblock.VirtualBlockTileEntity;
import grondag.hard_science.superblock.varia.SuperModelLoader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModModels
{
    public static final SuperDispatcher MODEL_DISPATCH = new SuperDispatcher();

    public static final VirtualBlockBakedModel VIRTUAL_BAKED_MODEL =  new VirtualBlockBakedModel();
    public static final String VIRTUAL_BLOCK_LOCATION = HardScience.MODID + ":" + VirtualBlock.VIRTUAL_BLOCK_NAME;
    
    @SubscribeEvent()
    public static void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        // SET UP COLOR ATLAS
        // {
        // NiceHues.INSTANCE.writeColorAtlas(event.getModConfigurationDirectory());
        // }
        
        ModModels.MODEL_DISPATCH.clear();
        for(DispatchDelegate delegate : ModModels.MODEL_DISPATCH.delegates)
        {
            event.getModelRegistry().putObject(new ModelResourceLocation(delegate.getModelResourceString()), delegate);
        }
        event.getModelRegistry().putObject(new ModelResourceLocation(VIRTUAL_BLOCK_LOCATION), VIRTUAL_BAKED_MODEL);
        event.getModelRegistry().putObject(new ModelResourceLocation(VIRTUAL_BLOCK_LOCATION, "inventory"), VIRTUAL_BAKED_MODEL);
        
        IForgeRegistry<Item> itemReg = GameRegistry.findRegistry(Item.class);
        
        for(Map.Entry<ResourceLocation, Item> entry: itemReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                Item item = entry.getValue();
                if(item instanceof SuperItemBlock)
                {
                    SuperBlock block = (SuperBlock)((ItemBlock)item).getBlock();
                    for (ItemStack stack : block.getSubItems())
                    {
                        event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName() + "." + stack.getMetadata(), "inventory"),
                                ModModels.MODEL_DISPATCH.getDelegate(block));
                    }
                }
                else
                {
                    // Not needed - will look for json files for normal items;
                }
            }
        }
    }

    /**
     * Register all textures that will be needed for associated models. 
     * Happens before model bake.
     */
    @SubscribeEvent
    public static void stitcherEventPre(TextureStitchEvent.Pre event)
    {
        TextureMap map = event.getMap();
        for(TexturePallette p : Textures.REGISTRY)
        {
            for(String s : p.getTexturesForPrestich())
            {
                ResourceLocation loc = new ResourceLocation(s);
                
                if(p.textureLayout == TextureLayout.BIGTEX_ANIMATED)
                {
                    if(map.getTextureExtry(loc.toString()) == null)
                    {
                        map.setTextureEntry(new CompressedAnimatedSprite(loc, p.ticksPerFrame));
                    }
                }
                else
                {
                    map.registerSprite(loc);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void stitcherEventPost(TextureStitchEvent.Post event)
    {
        if(Configurator.RENDER.enableAnimationStatistics && CompressedAnimatedSprite.perfLoadRead.runCount() > 0)
        {
            CompressedAnimatedSprite.perfCollectorLoad.outputStats();
            CompressedAnimatedSprite.perfCollectorLoad.clearStats();
            
//            Log.info("JPEG decoding " + CompressedAnimatedSprite.perfLoadJpeg.stats());
//            Log.info("Color conversion and alpha channel reconstruction " + CompressedAnimatedSprite.perfLoadAlpha.stats());
//            Log.info("Mipmap generation " + CompressedAnimatedSprite.perfLoadMipMap.stats());
//            Log.info("Transfer to buffer " + CompressedAnimatedSprite.perfLoadTransfer.stats());
            
            CompressedAnimatedSprite.reportMemoryUsage();
        }
        
        CompressedAnimatedSprite.tearDown();
    }

    @SubscribeEvent
    public static void modelRegistryEvent(ModelRegistryEvent event)
    {
       final SuperStateMapper mapper = new SuperStateMapper(MODEL_DISPATCH);
        
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
    
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                Block block = entry.getValue();
                if(block instanceof SuperBlock)
                {
                    ModelLoader.setCustomStateMapper(block, mapper);
                }
            }
        }
          
        IForgeRegistry<Item> itemReg = GameRegistry.findRegistry(Item.class);
        
        for(Map.Entry<ResourceLocation, Item> entry: itemReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                Item item = entry.getValue();
                if(item instanceof SuperItemBlock)
                {
                    
                    SuperBlock block = (SuperBlock)((ItemBlock)item).getBlock();
                    for (ItemStack stack : block.getSubItems())
                    {
                        String variantName = ModModels.MODEL_DISPATCH.getDelegate(block).getModelResourceString() + "." + stack.getMetadata();
                        ModelBakery.registerItemVariants(item, new ResourceLocation(variantName));
                        ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(), new ModelResourceLocation(variantName, "inventory"));     
                    }
                }
                // Would not actually do it this way if start using OBJ models
//                else if(item == ModItems.obj_test_model)
//                {
//                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName() + ".obj", "inventory"));
//                }
                else
                {
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
                }
            }
        }
        
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.virtual_block), 0, new ModelResourceLocation(ModBlocks.virtual_block.getRegistryName(), "inventory"));
        // Bind our TESR to our tile entity
        ClientRegistry.bindTileEntitySpecialRenderer(VirtualBlockTileEntity.class, VirtualBlockTESR.INSTANCE);
    }
    
    public static void preInit(FMLPreInitializationEvent event) 
    {
        ModelLoaderRegistry.registerLoader(new SuperModelLoader());
        //ModelLoaderRegistry.registerLoader(HSObjModelLoader.INSTANCE);
        //OBJLoader.INSTANCE.addDomain(HardScience.MODID);
    }
    
    public static void init(FMLInitializationEvent event)
    {
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                Block block = entry.getValue();
                if(block instanceof SuperBlock)
                {
                    // won't work in pre-init because BlockColors/ItemColors aren't instantiated yet
                    // Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(block.blockModelHelper.dispatcher, block);
                    Minecraft.getMinecraft().getItemColors().registerItemColorHandler(DummyColorHandler.INSTANCE, block);
                }
            }
        }
    }
}
