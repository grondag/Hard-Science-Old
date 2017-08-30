package grondag.hard_science.init;

import java.io.IOException;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.library.font.FontLoader;
import grondag.hard_science.library.font.TrueTypeFont;
import grondag.hard_science.machines.BasicBuilderTileEntity;
import grondag.hard_science.machines.SmartChestTileEntity;
import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.texture.CompressedAnimatedSprite;
import grondag.hard_science.superblock.texture.TextureLayout;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.varia.SuperDispatcher;
import grondag.hard_science.superblock.varia.SuperDispatcher.DispatchDelegate;
import grondag.hard_science.superblock.varia.SuperModelLoader;
import grondag.hard_science.superblock.varia.SuperStateMapper;
import grondag.hard_science.virtualblock.VirtualBlockTESR;
import grondag.hard_science.virtualblock.VirtualBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModModels
{
    public static final SuperDispatcher MODEL_DISPATCH = new SuperDispatcher();

    
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
        
//        event.getModelRegistry().putObject(new ModelResourceLocation(VIRTUAL_BLOCK_LOCATION), VIRTUAL_BAKED_MODEL);
//        event.getModelRegistry().putObject(new ModelResourceLocation(VIRTUAL_BLOCK_LOCATION, "inventory"), VIRTUAL_BAKED_MODEL);
        
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
    
    public static int TEX_MACHINE_ON;
    public static int TEX_MACHINE_OFF;
    public static int TEX_BLOCKS;
    public static TextureAtlasSprite SPRITE_REDSTONE_TORCH_LIT;
    public static TrueTypeFont FONT_ORBITRON;
    
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
        
   
        TEX_MACHINE_ON = loadNonBlockTexture("hard_science:textures/blocks/on_hd_256.png").getGlTextureId();
       
        TEX_MACHINE_OFF = loadNonBlockTexture("hard_science:textures/blocks/off_hd_256.png").getGlTextureId();
        
        TEX_BLOCKS = Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId();
        
        SPRITE_REDSTONE_TORCH_LIT = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_on");
        
        FONT_ORBITRON = FontLoader.createFont(new ResourceLocation(HardScience.MODID + ":fonts/orbitron_light.ttf"), 64, true);
      
    }

    
    private static ITextureObject loadNonBlockTexture(String location)
    {
    
        ResourceLocation loc = new ResourceLocation(location);
        ITextureObject result = new SimpleTexture(loc);
        Minecraft.getMinecraft().getTextureManager().loadTexture(loc, result);
        GlStateManager.bindTexture(result.getGlTextureId());
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST );
        //this one is "normal" setting for MC
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR );
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        return result;
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
        
        // Bind TESR to tile entity
        ClientRegistry.bindTileEntitySpecialRenderer(SuperTileEntity.class, SuperBlockTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(SuperModelTileEntity.class, SuperBlockTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(VirtualBlockTileEntity.class, VirtualBlockTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(BasicBuilderTileEntity.class, MachineTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(SmartChestTileEntity.class, MachineTESR.INSTANCE);
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
