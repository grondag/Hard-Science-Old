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
import grondag.hard_science.gui.control.machine.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.library.font.FontLoader;
import grondag.hard_science.library.font.RasterFont;
import grondag.hard_science.library.font.TrueTypeFont;
import grondag.hard_science.library.render.QuadBakery;
import grondag.hard_science.library.render.SimpleItemBlockModel;
import grondag.hard_science.library.render.TextureHelper;
import grondag.hard_science.machines.BasicBuilderTESR;
import grondag.hard_science.machines.BasicBuilderTileEntity;
import grondag.hard_science.machines.SmartChestTESR;
import grondag.hard_science.machines.SmartChestTileEntity;
import grondag.hard_science.materials.ResourceCube;
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
                else if(item instanceof ResourceCube)
                {
                    event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"),
                            new SimpleItemBlockModel(QuadBakery.createCubeWithTexture(Textures.BLOCK_NOISE_SUBTLE_ZOOM.getSampleTextureName(), 0xFFAAAAFF), true, true));
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
        map.registerSprite(new ResourceLocation(FONT_RESOURCE_STRING));
        
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
    public static TextureAtlasSprite SPRITE_STONE;
    public static TextureAtlasSprite SPRITE_GLASS;
    public static TextureAtlasSprite SPRITE_WOOD;
    public static TextureAtlasSprite SPRITE_GLOWSTONE;
    public static TextureAtlasSprite SPRITE_CYAN_DYE;
    public static TextureAtlasSprite SPRITE_MAGENTA_DYE;
    public static TextureAtlasSprite SPRITE_YELLOW_DYE;
    public static TextureAtlasSprite SPRITE_BLACK_DYE;
    
    public static RasterFont FONT_RENDERER;
    public static String FONT_RESOURCE_STRING = "hard_science:blocks/league_gothic-regular_subset";
    // format is character code, pixelWidth, height, x, y
    public static int[][] FONT_DATA = {
            { 43, 43, 85, 4, 0 },
            { 45, 18, 85, 55, 0 },
            { 37, 41, 85, 81, 0 },
            { 46, 13, 85, 130, 0 },
            { 61, 43, 85, 151, 0 },
            { 63, 27, 85, 202, 0 },
            { 33, 16, 85, 237, 0 },
            { 47, 29, 85, 261, 0 },
            { 48, 30, 85, 298, 0 },
            { 49, 19, 85, 336, 0 },
            { 50, 30, 85, 363, 0 },
            { 51, 29, 85, 401, 0 },
            { 52, 28, 85, 438, 0 },
            { 53, 29, 85, 474, 0 },
            { 54, 29, 85, 4, 85 },
            { 55, 24, 85, 41, 85 },
            { 56, 29, 85, 73, 85 },
            { 57, 29, 85, 110, 85 },
            { 65, 31, 85, 147, 85 },
            { 66, 30, 85, 186, 85 },
            { 67, 30, 85, 224, 85 },
            { 68, 31, 85, 262, 85 },
            { 69, 26, 85, 301, 85 },
            { 70, 25, 85, 335, 85 },
            { 71, 30, 85, 368, 85 },
            { 72, 31, 85, 406, 85 },
            { 73, 15, 85, 445, 85 },
            { 74, 17, 85, 468, 85 },
            { 75, 32, 85, 4, 170 },
            { 76, 24, 85, 44, 170 },
            { 77, 41, 85, 76, 170 },
            { 78, 34, 85, 125, 170 },
            { 79, 30, 85, 167, 170 },
            { 80, 30, 85, 205, 170 },
            { 81, 30, 85, 243, 170 },
            { 82, 30, 85, 281, 170 },
            { 83, 28, 85, 319, 170 },
            { 84, 26, 85, 355, 170 },
            { 85, 30, 85, 389, 170 },
            { 86, 29, 85, 427, 170 },
            { 87, 42, 85, 464, 170 },
            { 88, 31, 85, 4, 255 },
            { 89, 29, 85, 43, 255 },
            { 90, 26, 85, 80, 255 },
            { 97, 27, 85, 114, 255 },
            { 98, 29, 85, 149, 255 },
            { 99, 27, 85, 186, 255 },
            { 100, 28, 85, 221, 255 },
            { 101, 27, 85, 257, 255 },
            { 102, 20, 85, 292, 255 },
            { 103, 29, 85, 320, 255 },
            { 104, 29, 85, 357, 255 },
            { 105, 15, 85, 394, 255 },
            { 106, 14, 85, 417, 255 },
            { 107, 26, 85, 439, 255 },
            { 108, 15, 85, 473, 255 },
            { 109, 42, 85, 4, 340 },
            { 110, 29, 85, 54, 340 },
            { 111, 28, 85, 91, 340 },
            { 112, 29, 85, 127, 340 },
            { 113, 28, 85, 164, 340 },
            { 114, 20, 85, 200, 340 },
            { 115, 26, 85, 228, 340 },
            { 116, 21, 85, 262, 340 },
            { 117, 29, 85, 291, 340 },
            { 118, 25, 85, 328, 340 },
            { 119, 38, 85, 361, 340 },
            { 120, 26, 85, 407, 340 },
            { 121, 26, 85, 441, 340 },
            { 122, 22, 85, 475, 340 }};
    
    public static TextureAtlasSprite FONT_SPRITE;
    
    public static int TEX_RADIAL_GAUGE_MINOR;
    public static int TEX_RADIAL_GAUGE_MAIN;
    public static int TEX_RADIAL_GAUGE_MARKS;
    public static int TEX_RADIAL_GAUGE_FULL_MARKS;

    public static int TEX_LINEAR_GAUGE_LEVEL;
    public static int TEX_LINEAR_GAUGE_MARKS;
    public static int TEX_LINEAR_POWER_LEVEL;
    
    public static int TEX_POWER_BACKGROUND;
    public static int TEX_POWER_OUTER;
    public static int TEX_POWER_INNER;
    
    public static int TEX_SYMBOL_BUILDER;
    public static int TEX_SYMBOL_CHEST;
    
    public static int TEX_NO;
    public static int TEX_MATERIAL_SHORTAGE;
    
    public static final RadialGaugeSpec[] BASIC_BUILDER_GAUGE_SPECS = new RadialGaugeSpec[8];
    
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
        TEX_LINEAR_POWER_LEVEL = loadNonBlockTexture("hard_science:textures/blocks/linear_power_128.png");
        
        TEX_POWER_BACKGROUND = loadNonBlockTexture("hard_science:textures/blocks/power_background_256.png");
        TEX_POWER_OUTER = loadNonBlockTexture("hard_science:textures/blocks/power_outer_256.png");
        TEX_POWER_INNER = loadNonBlockTexture("hard_science:textures/blocks/power_inner_256.png");
        
        TEX_NO = loadNonBlockTexture("hard_science:textures/blocks/no_128.png");
        TEX_MATERIAL_SHORTAGE = loadNonBlockTexture("hard_science:textures/blocks/material_shortage.png");
        
        TEX_BLOCKS = Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId();
        
        ITEX_BLOCKS = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        
        SPRITE_REDSTONE_TORCH_LIT   = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_on");
        SPRITE_REDSTONE_TORCH_UNLIT = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_off");
        SPRITE_STONE                = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone");
        SPRITE_GLASS                = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/glass");
        SPRITE_WOOD                 = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/log_oak_top");
        SPRITE_GLOWSTONE            = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/glowstone_dust");
        SPRITE_CYAN_DYE             = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/dye_powder_cyan");
        SPRITE_MAGENTA_DYE          = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/dye_powder_magenta");
        SPRITE_YELLOW_DYE           = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/dye_powder_yellow");
        SPRITE_BLACK_DYE            = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/dye_powder_black");
        
        FONT_SPRITE                 = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(FONT_RESOURCE_STRING);
        
        FONT_RENDERER = new RasterFont(FONT_SPRITE, 2, FONT_DATA);
        
        BASIC_BUILDER_GAUGE_SPECS[0] = new RadialGaugeSpec(4, RenderBounds.BOUNDS_GAUGE[4], 1.0, ModModels.SPRITE_CYAN_DYE, 0x00FFFF);
        BASIC_BUILDER_GAUGE_SPECS[1] = new RadialGaugeSpec(5, RenderBounds.BOUNDS_GAUGE[5], 1.0, ModModels.SPRITE_MAGENTA_DYE, 0xFF00FF);
        BASIC_BUILDER_GAUGE_SPECS[2] = new RadialGaugeSpec(6, RenderBounds.BOUNDS_GAUGE[6], 1.0, ModModels.SPRITE_YELLOW_DYE, 0xFFFF00);
        BASIC_BUILDER_GAUGE_SPECS[3] = new RadialGaugeSpec(7, RenderBounds.BOUNDS_GAUGE[7], 1.0, ModModels.SPRITE_BLACK_DYE, 0x555555);
    
        BASIC_BUILDER_GAUGE_SPECS[4] = new RadialGaugeSpec(1, RenderBounds.BOUNDS_GAUGE[0], 0.75, ModModels.SPRITE_STONE, 0x7f7f7f);
        BASIC_BUILDER_GAUGE_SPECS[5] = new RadialGaugeSpec(0, RenderBounds.BOUNDS_GAUGE[2], 0.75, ModModels.SPRITE_WOOD, 0xa78653);
        BASIC_BUILDER_GAUGE_SPECS[6] = new RadialGaugeSpec(2, RenderBounds.BOUNDS_GAUGE[1], 0.75, ModModels.SPRITE_GLASS, 0xaafcff);
        BASIC_BUILDER_GAUGE_SPECS[7] = new RadialGaugeSpec(3, RenderBounds.BOUNDS_GAUGE[3], 1.0, ModModels.SPRITE_GLOWSTONE, 0xffffd5);
    }

    
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
                else if(item instanceof ResourceCube)
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
