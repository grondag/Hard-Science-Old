package grondag.hard_science.init;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.Log;
import grondag.hard_science.gui.control.machine.BinaryGlTexture;
import grondag.hard_science.library.font.RasterFont;
import grondag.hard_science.library.render.TextureHelper;
import grondag.hard_science.machines.BasicBuilderTESR;
import grondag.hard_science.machines.BasicBuilderTileEntity;
import grondag.hard_science.machines.SmartChestTESR;
import grondag.hard_science.machines.SmartChestTileEntity;
import grondag.hard_science.materials.MatterCube;
import grondag.hard_science.materials.MatterCubeItemModel;
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
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
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
                else if(item instanceof MatterCube)
                {
                    event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"),
                            new MatterCubeItemModel((MatterCube) item));
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
        FONT_RENDERER_SMALL = new RasterFont(FONT_NAME_SMALL, FONT_SIZE_SMALL, 2);
        map.setTextureEntry(FONT_RENDERER_SMALL);
        FONT_RENDERER_LARGE = new RasterFont(FONT_NAME_LARGE, FONT_SIZE_LARGE, 2);
        map.setTextureEntry(FONT_RENDERER_LARGE);
        
        
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
    
    public static BinaryGlTexture TEX_MACHINE_ON_OFF;
    
    public static TextureManager TEX_MANAGER;
    
    public static int TEX_BLOCKS;
    public static ITextureObject ITEX_BLOCKS;
    
    public static TextureAtlasSprite SPRITE_REDSTONE_TORCH_LIT;
    public static TextureAtlasSprite SPRITE_REDSTONE_TORCH_UNLIT;

    
    public static String FONT_NAME_SMALL = "ubuntu-c.ttf";
    public static String FONT_NAME_LARGE = "ubuntu-m.ttf";
    public static int FONT_SIZE_SMALL = 512;
    public static int FONT_SIZE_LARGE = 512;
    public static RasterFont FONT_RENDERER_SMALL;
    public static RasterFont FONT_RENDERER_LARGE;
    public static String FONT_RESOURCE_STRING_SMALL = RasterFont.getSpriteResourceName(FONT_NAME_SMALL, FONT_SIZE_SMALL);
    public static String FONT_RESOURCE_STRING_LARGE = RasterFont.getSpriteResourceName(FONT_NAME_LARGE, FONT_SIZE_LARGE);
 
    public static int TEX_RADIAL_GAUGE_MINOR;
    public static int TEX_RADIAL_GAUGE_MAIN;
    public static int TEX_RADIAL_GAUGE_MARKS;
    public static int TEX_RADIAL_GAUGE_FULL_MARKS;

    public static int TEX_LINEAR_GAUGE_LEVEL;
    public static int TEX_LINEAR_GAUGE_MARKS;
//    public static int TEX_LINEAR_POWER_LEVEL;
    
    public static int TEX_POWER_BACKGROUND;
    public static int TEX_POWER_FOREGROUND;
//    public static int TEX_POWER_OUTER;
//    public static int TEX_POWER_INNER;
    
    public static int TEX_SYMBOL_BUILDER;
    public static int TEX_SYMBOL_CHEST;
    
    public static int TEX_NO;
    public static int TEX_MATERIAL_SHORTAGE;
    public static int TEX_ELECTRICITY;
    public static int TEX_CURRENT_SOURCE;
    public static int TEX_CMY;
    public static int TEX_FLAME;
    
    public static int COLOR_POWER = 0xFFFFBF;
    public static int COLOR_BATTERY = 0x00B1FF;
    public static int COLOR_BATTERY_DRAIN = 0xff4e00;
    public static int COLOR_FUEL_CELL = 0xFC8D59;
    public static int COLOR_FAILURE = 0xFFFF20;
    
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
        
        TEX_MACHINE_ON_OFF = new BinaryGlTexture(
                loadNonBlockTexture("hard_science:textures/blocks/on_hd_256.png"),
                loadNonBlockTexture("hard_science:textures/blocks/off_hd_256.png"));
        
        TEX_RADIAL_GAUGE_MINOR = loadNonBlockTexture("hard_science:textures/blocks/gauge_inner_256.png");
        TEX_RADIAL_GAUGE_MAIN = loadNonBlockTexture("hard_science:textures/blocks/gauge_main_256.png");
        TEX_RADIAL_GAUGE_MARKS = loadNonBlockTexture("hard_science:textures/blocks/gauge_background_256.png");
        TEX_RADIAL_GAUGE_FULL_MARKS = loadNonBlockTexture("hard_science:textures/blocks/gauge_marks_256.png");
        
        TEX_SYMBOL_BUILDER = loadNonBlockTexture("hard_science:textures/blocks/symbol_builder.png");
        TEX_SYMBOL_CHEST = loadNonBlockTexture("hard_science:textures/blocks/symbol_chest.png");
        
        TEX_LINEAR_GAUGE_LEVEL = loadNonBlockTexture("hard_science:textures/blocks/linear_level_128.png");
        TEX_LINEAR_GAUGE_MARKS = loadNonBlockTexture("hard_science:textures/blocks/linear_marks_128.png");
//        TEX_LINEAR_POWER_LEVEL = loadNonBlockTexture("hard_science:textures/blocks/linear_power_128.png");
        
        TEX_POWER_BACKGROUND = loadNonBlockTexture("hard_science:textures/blocks/power_background_128.png");
        TEX_POWER_FOREGROUND = loadNonBlockTexture("hard_science:textures/blocks/power_foreground_128.png");
//        TEX_POWER_OUTER = loadNonBlockTexture("hard_science:textures/blocks/power_outer_256.png");
//        TEX_POWER_INNER = loadNonBlockTexture("hard_science:textures/blocks/power_inner_256.png");
        
        TEX_NO = loadNonBlockTexture("hard_science:textures/blocks/no_128.png");
        TEX_MATERIAL_SHORTAGE = loadNonBlockTexture("hard_science:textures/blocks/material_shortage.png");
        
        TEX_ELECTRICITY = loadNonBlockTexture("hard_science:textures/blocks/electricity_64.png");
        TEX_CURRENT_SOURCE = loadNonBlockTexture("hard_science:textures/blocks/power_source_64.png");
        TEX_CMY = loadNonBlockTexture("hard_science:textures/blocks/cmy.png");
        TEX_FLAME = loadNonBlockTexture("hard_science:textures/blocks/flame_64.png");
        
        TEX_BLOCKS = Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId();
        
        ITEX_BLOCKS = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        
        SPRITE_REDSTONE_TORCH_LIT   = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_on");
        SPRITE_REDSTONE_TORCH_UNLIT = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_off");
         
        FONT_RENDERER_SMALL.postLoad();
        FONT_RENDERER_LARGE.postLoad();
        
        BasicBuilderTileEntity.initRenderSpecs();

    }
    
//    new VolumetricBufferSpec(HDPE_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_HDPE),
    
//    new VolumetricBufferSpec(FILLER_INGREDIENTS, MatterUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_MINERAL_FILLER),
//    new VolumetricBufferSpec(RESIN_A_INGREDIENTS, MatterUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_RESIN_A),
//    new VolumetricBufferSpec(RESIN_B_INGREDIENTS, MatterUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_RESIN_B),
//    new VolumetricBufferSpec(NANOLIGHT_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_NANO_LIGHTS),
    
//    new VolumetricBufferSpec(CYAN_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_DYE_CYAN),
//    new VolumetricBufferSpec(MAGENTA_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_DYE_MAGENTA),
//    new VolumetricBufferSpec(YELLOW_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_DYE_YELLOW),
//    new VolumetricBufferSpec(TiO2_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_TiO2)

    
    private static int loadNonBlockTexture(String location)
    {
    
        IResource resource;
        BufferedImage bufferedImage;
        
        try
        {
            resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(location));
            bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
        }
        catch (IOException e)
        {
            Log.error("Unable to load non-block texture", e);
            return -1;
        }
        
        int width = bufferedImage.getWidth() ;
        int height = bufferedImage.getHeight();
        
        int aint[] = new int[width * height];
        bufferedImage.getRGB(0, 0, width, height, aint, 0, width);
        
        ByteBuffer buff = TextureHelper.getBufferedTexture(aint);
        
        IntBuffer textureId = BufferUtils.createIntBuffer(1);

        GL11.glGenTextures(textureId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId.get(0));

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);

//        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

//        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, pixelWidth, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buff);
        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, width, height, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buff);
        
        return textureId.get(0);
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
                else if(item instanceof MatterCube)
                {
                    ModelBakery.registerItemVariants(item, item.getRegistryName());
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));     
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
        ClientRegistry.bindTileEntitySpecialRenderer(BasicBuilderTileEntity.class, BasicBuilderTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(SmartChestTileEntity.class, SmartChestTESR.INSTANCE);
        
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
