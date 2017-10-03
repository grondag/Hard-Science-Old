package grondag.hard_science.superblock.texture;


import static grondag.hard_science.library.world.Rotation.*;
import static grondag.hard_science.superblock.texture.TextureRotationType.*;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Configurator;
import grondag.hard_science.Configurator.Substances.Substance;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePalletteInfo;

public class Textures
{
    /** Collection of all mod textures */
    public static final TexturePalletteRegistry REGISTRY = new TexturePalletteRegistry();
    
    public static final int MAX_TEXTURES = 4096;
    
    //======================================================================
    //  SYSTEM TEXTURES - INITIAL RELEASE
    //======================================================================

    /**
     * Important that this come first so that it is the default value returned by modelState.
     * Is not meant for user selection. For CUT paint layer means should use same texture as base layer.
     * For DETAIL and OVERLAY layers, indicates those layers are disabled. 
     */
    public static final TexturePallette NONE = REGISTRY.addTexturePallette("noise_moderate", 
            new TexturePalletteInfo().withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.SPLIT_X_8)
            .withRotation(RANDOM.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.ALWAYS_HIDDEN));
    
    //======================================================================
    //  TEST/DEBUG TEXTURES - NOT LOADED UNLESS NEEDED
    //======================================================================
    
    // but still load placeholders so we don't mess up texture slot IDs
    
    public static final TexturePallette BIGTEX_TEST_SINGLE = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "bigtex_single" : "noise_moderate_0_0",
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SMALL).withLayout(TextureLayout.BIGTEX)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));
    
    public static final TexturePallette BIGTEX_TEST1  = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate",
            new TexturePalletteInfo().withVersionCount(4).withScale(TextureScale.TINY).withLayout(TextureLayout.SPLIT_X_8)
            .withRotation(RANDOM.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));
    
    public static final TexturePallette BIGTEX_TEST2 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate",
            new TexturePalletteInfo(BIGTEX_TEST1).withScale(TextureScale.SMALL));
    public static final TexturePallette BIGTEX_TEST3 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate",
            new TexturePalletteInfo(BIGTEX_TEST1).withScale(TextureScale.MEDIUM));
    public static final TexturePallette BIGTEX_TEST4 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate",
            new TexturePalletteInfo(BIGTEX_TEST1).withScale(TextureScale.LARGE));
    public static final TexturePallette BIGTEX_TEST5 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate",
            new TexturePalletteInfo(BIGTEX_TEST1).withScale(TextureScale.GIANT));

    public static final TexturePallette TEST = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0_0", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BIGTEX)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));
    public static final TexturePallette TEST_ZOOM = REGISTRY.addZoomedPallete(TEST);
    
    public static final TexturePallette TEST_90 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0_0", 
            new TexturePalletteInfo(TEST).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette TEST_90_ZOOM = REGISTRY.addZoomedPallete(TEST_90);
    
    public static final TexturePallette TEST_180 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0_0", 
            new TexturePalletteInfo(TEST).withRotation(FIXED.with(ROTATE_180)));
    public static final TexturePallette TEST_180_ZOOM = REGISTRY.addZoomedPallete(TEST_180);
    
    public static final TexturePallette TEST_270 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0_0", 
            new TexturePalletteInfo(TEST).withRotation(FIXED.with(ROTATE_270)));
    public static final TexturePallette TEST_270_ZOOM = REGISTRY.addZoomedPallete(TEST_270);
    
    public static final TexturePallette TEST_4X4 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SMALL).withLayout(TextureLayout.BIGTEX)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));
    
    public static final TexturePallette TEST_4x4_90 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0", 
            new TexturePalletteInfo(TEST_4X4).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette TEST_4x4_180 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0", 
            new TexturePalletteInfo(TEST_4X4).withRotation(FIXED.with(ROTATE_180)));
    public static final TexturePallette TEST_4x4_270 = REGISTRY.addTexturePallette(Configurator.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0", 
            new TexturePalletteInfo(TEST_4X4).withRotation(FIXED.with(ROTATE_270)));
    
    //======================================================================
    //  TILES - REGULAR
    //======================================================================
    public static final TexturePallette BLOCK_COBBLE = REGISTRY.addTexturePallette("cobble", 
            new TexturePalletteInfo().withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.SPLIT_X_8)
            .withRotation(RANDOM.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.STATIC_TILES));
    public static final TexturePallette BLOCK_COBBLE_ZOOM = REGISTRY.addZoomedPallete(BLOCK_COBBLE);
   
    public static final TexturePallette BLOCK_NOISE_STRONG = REGISTRY.addTexturePallette("noise_strong", new TexturePalletteInfo(BLOCK_COBBLE));
    public static final TexturePallette BLOCK_NOISE_STRONG_ZOOM = REGISTRY.addZoomedPallete(BLOCK_NOISE_STRONG);
    
    public static final TexturePallette BLOCK_NOISE_MODERATE = REGISTRY.addTexturePallette("noise_moderate", new TexturePalletteInfo(BLOCK_COBBLE));
    public static final TexturePallette BLOCK_NOISE_MODERATE_ZOOM = REGISTRY.addZoomedPallete(BLOCK_NOISE_MODERATE);
    
    public static final TexturePallette BLOCK_NOISE_SUBTLE = REGISTRY.addTexturePallette("noise_subtle", new TexturePalletteInfo(BLOCK_COBBLE));
    public static final TexturePallette BLOCK_NOISE_SUBTLE_ZOOM = REGISTRY.addZoomedPallete(BLOCK_NOISE_SUBTLE);
    
    //======================================================================
    //  BORDERS
    //======================================================================
    
    public static final TexturePallette BORDER_SMOOTH_BLEND = REGISTRY.addTexturePallette("border_smooth_blended", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));

    public static final TexturePallette MASONRY_SIMPLE = REGISTRY.addTexturePallette("masonry_simple", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.MASONRY_5)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));

    public static final TexturePallette BORDER_SINGLE_PINSTRIPE = addBorderSingle("border_single_pinstripe");
    public static final TexturePallette BORDER_INSET_PINSTRIPE = addBorderSingle("border_inset_pinstripe");
    public static final TexturePallette BORDER_GRITTY_INSET_PINSTRIPE = addBorderRandom("border_gritty_inset_pinstripe", false);
    public static final TexturePallette BORDER_SINGLE_LINE = addBorderSingle("border_single_line");
    public static final TexturePallette BORDER_SINGLE_BOLD_LINE = addBorderSingle("border_single_bold_line");
    public static final TexturePallette BORDER_SINGLE_FAT_LINE = addBorderSingle("border_single_fat_line");
    public static final TexturePallette BORDER_GRITTY_FAT_LINE = addBorderRandom("border_gritty_fat_line", false);
    public static final TexturePallette BORDER_DOUBLE_MIXED_LINES = addBorderSingle("border_double_mixed_lines");
    public static final TexturePallette BORDER_DOUBLE_PINSTRIPES = addBorderSingle("border_double_pinstripes");
    public static final TexturePallette BORDER_INSET_DOUBLE_PINSTRIPES = addBorderSingle("border_inset_double_pinstripes");
    public static final TexturePallette BORDER_TRIPLE_MIXED_LINES = addBorderSingle("border_triple_mixed_lines");
    public static final TexturePallette BORDER_DOUBLE_DOUBLE = addBorderSingle("border_double_double");
    public static final TexturePallette BORDER_WHITEWALL = addBorderSingle("border_whitewall");
    public static final TexturePallette BORDER_GRITTY_WHITEWALL = addBorderRandom("border_gritty_whitewall", false);
    
    public static final TexturePallette BORDER_PINSTRIPE_DASH = addBorderSingle("border_pinstripe_dash");
    public static final TexturePallette BORDER_INSET_DOTS_1 = addBorderSingle("border_inset_dots_1");
    public static final TexturePallette BORDER_INSET_DOTS_2 = addBorderSingle("border_inset_dots_2");
    public static final TexturePallette BORDER_INSET_PIN_DOTS = addBorderSingle("border_inset_pin_dots");
    public static final TexturePallette BORDER_CHANNEL_DOTS = addBorderSingle("border_channel_dots");
    public static final TexturePallette BORDER_CHANNEL_PIN_DOTS = addBorderSingle("border_channel_pin_dots");
    
    public static final TexturePallette BORDER_CHANNEL_CHECKERBOARD = addBorderSingle("border_channel_checkerboard");
    public static final TexturePallette BORDER_CHECKERBOARD = addBorderSingle("border_checkerboard");
    public static final TexturePallette BORDER_GRITTY_CHECKERBOARD = addBorderRandom("border_gritty_checkerboard", false);
    
    public static final TexturePallette BORDER_GROOVY_STRIPES = addBorderSingle("border_groovy_stripes");
    public static final TexturePallette BORDER_GRITTY_GROOVES = addBorderRandom("border_gritty_grooves", false);
    public static final TexturePallette BORDER_GROOVY_PINSTRIPES = addBorderSingle("border_groovy_pinstripes");
    public static final TexturePallette BORDER_GRITTY_PINSTRIPE_GROOVES = addBorderRandom("border_gritty_pinstripe_grooves", false);
    
    public static final TexturePallette BORDER_ZIGZAG = addBorderSingle("border_zigzag");
    public static final TexturePallette BORDER_INVERSE_ZIGZAG = addBorderSingle("border_inverse_zigzag");
    public static final TexturePallette BORDER_CAUTION = addBorderSingle("border_caution");
    public static final TexturePallette BORDER_FILMSTRIP = addBorderSingle("border_filmstrip");
    public static final TexturePallette BORDER_CHANNEL_LINES = addBorderSingle("border_channel_lines");
    public static final TexturePallette BORDER_SIGNAL = addBorderSingle("border_signal");
    public static final TexturePallette BORDER_GRITTY_SIGNAL = addBorderRandom("border_gritty_signal", false);
    public static final TexturePallette BORDER_LOGIC = addBorderRandomTile("border_logic");
    public static final TexturePallette BORDER_INVERSE_TILE_1 = addBorderRandomTile("border_inverse_logic_1");
    
    //======================================================================
    //  BIGTEX
    //======================================================================
    
    public static final TexturePallette BIGTEX_WEATHERED_STONE = REGISTRY.addTexturePallette("weathered_smooth_stone", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.STATIC_TILES));
    public static final TexturePallette BIGTEX_WEATHERED_STONE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_WEATHERED_STONE);
    public static final TexturePallette BIGTEX_WEATHERED_STONE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_WEATHERED_STONE_ZOOM);
    
    public static final TexturePallette BIGTEX_SANDSTONE = REGISTRY.addTexturePallette("sandstone", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_SANDSTONE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_SANDSTONE);
    public static final TexturePallette BIGTEX_SANDSTONE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_SANDSTONE_ZOOM);

    public static final TexturePallette BIGTEX_ASPHALT = REGISTRY.addTexturePallette("asphalt", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_ASPHALT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_ASPHALT);
    public static final TexturePallette BIGTEX_ASPHALT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_ASPHALT_ZOOM);

    public static final TexturePallette BIGTEX_WORN_ASPHALT = REGISTRY.addTexturePallette("worn_asphalt", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_WORN_ASPHALT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_WORN_ASPHALT);
    public static final TexturePallette BIGTEX_WORN_ASPHALT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_WORN_ASPHALT_ZOOM);

    public static final TexturePallette BIGTEX_WOOD = REGISTRY.addTexturePallette("wood", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE).withRotation(FIXED.with(ROTATE_NONE)));
    public static final TexturePallette BIGTEX_WOOD_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_WOOD);
    public static final TexturePallette BIGTEX_WOOD_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_WOOD_ZOOM);
    
    public static final TexturePallette BIGTEX_WOOD_FLIP = REGISTRY.addTexturePallette("wood", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette BIGTEX_WOOD_ZOOM_FLIP = REGISTRY.addZoomedPallete(BIGTEX_WOOD_FLIP);
    public static final TexturePallette BIGTEX_WOOD_ZOOM_X2_FLIP = REGISTRY.addZoomedPallete(BIGTEX_WOOD_ZOOM_FLIP);

    public static final TexturePallette BIGTEX_GRANITE = REGISTRY.addTexturePallette("granite", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_GRANITE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_GRANITE);
    public static final TexturePallette BIGTEX_GRANITE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_GRANITE_ZOOM);

    public static final TexturePallette BIGTEX_MARBLE = REGISTRY.addTexturePallette("marble", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_MARBLE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_MARBLE);
    public static final TexturePallette BIGTEX_MARBLE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_MARBLE_ZOOM);

    public static final TexturePallette BIGTEX_SLATE = REGISTRY.addTexturePallette("slate", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_SLATE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_SLATE);
    public static final TexturePallette BIGTEX_SLATE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_SLATE_ZOOM);

    public static final TexturePallette BIGTEX_ROUGH_ROCK = REGISTRY.addTexturePallette("rough_rock", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_ROUGH_ROCK_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_ROUGH_ROCK);
    public static final TexturePallette BIGTEX_ROUGH_ROCK_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_ROUGH_ROCK_ZOOM);

    public static final TexturePallette BIGTEX_CRACKED_EARTH = REGISTRY.addTexturePallette("cracked_earth", new TexturePalletteInfo(BIGTEX_WEATHERED_STONE));
    public static final TexturePallette BIGTEX_CRACKED_EARTH_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_CRACKED_EARTH);
    public static final TexturePallette BIGTEX_CRACKED_EARTH_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_CRACKED_EARTH_ZOOM);

    //======================================================================
    //  VOLCANO
    //======================================================================
    
    public static final TexturePallette BIGTEX_BASALT_CUT = REGISTRY.addTexturePallette("basalt_cut", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.STATIC_TILES));
    public static final TexturePallette BIGTEX_BASALT_CUT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_BASALT_CUT);
    public static final TexturePallette BIGTEX_BASALT_CUT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_BASALT_CUT_ZOOM);
    
    public static final TexturePallette BIGTEX_BASALT_COOL = REGISTRY.addTexturePallette("basalt_cool", new TexturePalletteInfo(BIGTEX_BASALT_CUT));
    public static final TexturePallette BIGTEX_BASALT_COOL_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_BASALT_COOL);
    public static final TexturePallette BIGTEX_BASALT_COOL_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_BASALT_COOL_ZOOM);
    
    public static final TexturePallette BIGTEX_LAVA = REGISTRY.addTexturePallette(Configurator.VOLCANO.enableVolcano ? "lava" : "clouds",  
            new TexturePalletteInfo(BIGTEX_BASALT_CUT).withLayout(TextureLayout.BIGTEX_ANIMATED).withGroups(TextureGroup.HIDDEN_TILES));
    
    public static final TexturePallette BIGTEX_BASALT_COOLING = REGISTRY.addTexturePallette("basalt_cooling", 
             new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX)
             .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_DETAILS));
    public static final TexturePallette BIGTEX_BASALT_WARM = REGISTRY.addTexturePallette("basalt_warm",  new TexturePalletteInfo(BIGTEX_BASALT_COOLING));
    public static final TexturePallette BIGTEX_BASALT_HOT = REGISTRY.addTexturePallette("basalt_hot", new TexturePalletteInfo(BIGTEX_BASALT_COOLING));
    public static final TexturePallette BIGTEX_BASALT_VERY_HOT = REGISTRY.addTexturePallette("basalt_very_hot", new TexturePalletteInfo(BIGTEX_BASALT_COOLING));
    
    public static final TexturePallette BIGTEX_BASALT_HINT = REGISTRY.addTexturePallette("basalt_hint", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_DETAILS));
    public static final TexturePallette BIGTEX_BASALT_HINT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_BASALT_HINT);
    public static final TexturePallette BIGTEX_BASALT_HINT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_BASALT_HINT_ZOOM);
    
    //======================================================================
    //  BIGTEX - ANIMATED
    //======================================================================

    public static final TexturePallette BIGTEX_FLUID_GLOW = REGISTRY.addTexturePallette("fluid_glow", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.DYNAMIC_DETAILS));
    public static final TexturePallette BIGTEX_FLUID_GLOW_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_FLUID_GLOW);
    public static final TexturePallette BIGTEX_FLUID_GLOW_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_FLUID_GLOW_ZOOM);

    public static final TexturePallette BIGTEX_FLUID_VORTEX = REGISTRY.addTexturePallette("fluid_vortex", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .withGroups(TextureGroup.DYNAMIC_DETAILS, TextureGroup.DYNAMIC_TILES)
            .withTicksPerFrame(2));
    public static final TexturePallette BIGTEX_FLUID_VORTEX_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_FLUID_VORTEX);
    public static final TexturePallette BIGTEX_FLUID_VORTEX_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_FLUID_VORTEX_ZOOM);

    public static final TexturePallette BIGTEX_CLOUDS = REGISTRY.addTexturePallette("clouds", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .withGroups(TextureGroup.DYNAMIC_DETAILS, TextureGroup.DYNAMIC_TILES));
    public static final TexturePallette BIGTEX_CLOUDS_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_CLOUDS);
    public static final TexturePallette BIGTEX_CLOUDS_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_CLOUDS_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD = REGISTRY.addTexturePallette("starfield", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SMALL).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .withGroups(TextureGroup.DYNAMIC_DETAILS, TextureGroup.DYNAMIC_TILES));
    public static final TexturePallette BIGTEX_STARFIELD_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD);
    public static final TexturePallette BIGTEX_STARFIELD_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_90 = REGISTRY.addTexturePallette("starfield", 
            new TexturePalletteInfo(BIGTEX_STARFIELD).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette BIGTEX_STARFIELD_90_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_90);
    public static final TexturePallette BIGTEX_STARFIELD_90_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_90_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_180 = REGISTRY.addTexturePallette("starfield", 
            new TexturePalletteInfo(BIGTEX_STARFIELD).withRotation(FIXED.with(ROTATE_180)));
    public static final TexturePallette BIGTEX_STARFIELD_180_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_180);
    public static final TexturePallette BIGTEX_STARFIELD_180_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_180_ZOOM);
    
      public static final TexturePallette BIGTEX_STARFIELD_270 = REGISTRY.addTexturePallette("starfield", 
            new TexturePalletteInfo(BIGTEX_STARFIELD).withRotation(FIXED.with(ROTATE_270)));
    public static final TexturePallette BIGTEX_STARFIELD_270_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_270);
    public static final TexturePallette BIGTEX_STARFIELD_270_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_270_ZOOM);
    
    //======================================================================
    //  DECALS
    //======================================================================
    public static final TexturePallette DECAL_SMALL_DOT = REGISTRY.addTexturePallette("small_dot", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BIGTEX)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_DETAILS));
    
    public static final TexturePallette DECAL_MEDIUM_DOT = REGISTRY.addTexturePallette("medium_dot", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_LARGE_DOT = REGISTRY.addTexturePallette("big_dot", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SMALL_SQUARE = REGISTRY.addTexturePallette("small_square", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_MEDIUM_SQUARE = REGISTRY.addTexturePallette("medium_square", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_LARGE_SQUARE = REGISTRY.addTexturePallette("big_square", new TexturePalletteInfo(DECAL_SMALL_DOT));

    public static final TexturePallette DECAL_SKINNY_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("skinny_diagonal_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THICK_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("thick_diagonal_cross_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THICK_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("thick_diagonal_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THIN_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("thin_diagonal_cross_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THIN_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("thin_diagonal_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THIN_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("thin_diagonal_cross_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THIN_DIAGONAL_BARS = REGISTRY.addTexturePallette("thin_diagonal_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SKINNY_DIAGNAL_CROSS_BARS = REGISTRY.addTexturePallette("skinny_diagonal_cross_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_BARS = REGISTRY.addTexturePallette("skinny_diagonal_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("diagonal_cross_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_DIAGONAL_BARS = REGISTRY.addTexturePallette("diagonal_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_FAT_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("fat_diagonal_cross_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_FAT_DIAGONAL_BARS = REGISTRY.addTexturePallette("fat_diagonal_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("diagonal_cross_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("diagonal_ridges", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SKINNY_BARS = REGISTRY.addTexturePallette("skinny_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_FAT_BARS = REGISTRY.addTexturePallette("fat_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THICK_BARS = REGISTRY.addTexturePallette("thick_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_THIN_BARS = REGISTRY.addTexturePallette("thin_bars", new TexturePalletteInfo(DECAL_SMALL_DOT));
    
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 
            new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette DECAL_THICK_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("thick_diagonal_ridges", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_THIN_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("thin_diagonal_ridges", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_THIN_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("thin_diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("skinny_diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_FAT_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("fat_diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("diagonal_ridges", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SKINNY_BARS_90 = REGISTRY.addTexturePallette("skinny_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_FAT_BARS_90 = REGISTRY.addTexturePallette("fat_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_THICK_BARS_90 = REGISTRY.addTexturePallette("thick_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_THIN_BARS_90 = REGISTRY.addTexturePallette("thin_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_90));

    public static final TexturePallette DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 
            new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(RANDOM.with(ROTATE_NONE)));
    public static final TexturePallette DECAL_THICK_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("thick_diagonal_ridges", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_THIN_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("thin_diagonal_ridges", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_THIN_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("thin_diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("skinny_diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_FAT_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("fat_diagonal_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("diagonal_ridges", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SKINNY_BARS_RANDOM = REGISTRY.addTexturePallette("skinny_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_FAT_BARS_RANDOM = REGISTRY.addTexturePallette("fat_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_THICK_BARS_RANDOM = REGISTRY.addTexturePallette("thick_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_THIN_BARS_RANDOM = REGISTRY.addTexturePallette("thin_bars", new TexturePalletteInfo(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    
    // Integrate after here at next compatibility break
    
    public static final TexturePallette BORDER_INVERSE_TILE_2 = addBorderRandomTile("border_inverse_logic_2");
    
    
    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("skinny_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_THICK_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("thick_diagonal_cross_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_THICK_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("thick_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("thin_diagonal_cross_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("thin_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("thin_diagonal_cross_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_BARS = REGISTRY.addTexturePallette("thin_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGNAL_CROSS_BARS = REGISTRY.addTexturePallette("skinny_diagonal_cross_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGONAL_BARS = REGISTRY.addTexturePallette("skinny_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("diagonal_cross_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_BARS = REGISTRY.addTexturePallette("diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_FAT_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("fat_diagonal_cross_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_FAT_DIAGONAL_BARS = REGISTRY.addTexturePallette("fat_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("diagonal_cross_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SMALL_DOT));

    
    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 
            new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette DECAL_SOFT_THICK_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("thick_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("thin_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("thin_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("skinny_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SOFT_FAT_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("fat_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));

    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 
            new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(RANDOM.with(ROTATE_NONE)));
    public static final TexturePallette DECAL_SOFT_THICK_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("thick_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("thin_diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SOFT_THIN_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("thin_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SOFT_SKINNY_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("skinny_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SOFT_FAT_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("fat_diagonal_bars_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final TexturePallette DECAL_SOFT_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("diagonal_ridges_seamless", new TexturePalletteInfo(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    
    public static final TexturePallette DECAL_BIG_TRIANGLE = REGISTRY.addTexturePallette("big_triangle", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_BIG_DIAMOND = REGISTRY.addTexturePallette("big_diamond", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_BIG_PENTAGON = REGISTRY.addTexturePallette("big_pentagon", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_BIG_HEXAGON = REGISTRY.addTexturePallette("big_hexagon", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_STAR_16 = REGISTRY.addTexturePallette("star_16", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_STAR_12 = REGISTRY.addTexturePallette("star_12", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_STAR_8 = REGISTRY.addTexturePallette("star_8", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_STAR_5 = REGISTRY.addTexturePallette("star_5", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_STAR_4 = REGISTRY.addTexturePallette("star_4", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_TWO_DOTS = REGISTRY.addTexturePallette("two_dots", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_TWO_DOTS_RANDOM = REGISTRY.addTexturePallette("two_dots", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(RANDOM.with(ROTATE_NONE)));
    public static final TexturePallette DECAL_DUST = REGISTRY.addTexturePallette("dust", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_MIX = REGISTRY.addTexturePallette("mix", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_NONE)));
    public static final TexturePallette DECAL_MIX_90 = REGISTRY.addTexturePallette("mix", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette DECAL_MIX_180 = REGISTRY.addTexturePallette("mix", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_180)));
    public static final TexturePallette DECAL_MIX_270 = REGISTRY.addTexturePallette("mix", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_270)));
    public static final TexturePallette DECAL_DRIP = REGISTRY.addTexturePallette("drip", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_FUNNEL = REGISTRY.addTexturePallette("funnel", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_ARROW = REGISTRY.addTexturePallette("arrow", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_NONE)));
    public static final TexturePallette DECAL_ARROW_90 = REGISTRY.addTexturePallette("arrow", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final TexturePallette DECAL_ARROW_180 = REGISTRY.addTexturePallette("arrow", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_180)));
    public static final TexturePallette DECAL_ARROW_270 = REGISTRY.addTexturePallette("arrow", new TexturePalletteInfo(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_270)));
    
    public static final TexturePallette MATERIAL_GRADIENT = REGISTRY.addTexturePallette("material_gradient", 
            new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BIGTEX)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.HIDDEN_TILES));

    public static final TexturePallette DECAL_BUILDER = REGISTRY.addTexturePallette("symbol_builder", new TexturePalletteInfo(DECAL_SMALL_DOT));
    public static final TexturePallette DECAL_CHEST = REGISTRY.addTexturePallette("symbol_chest", new TexturePalletteInfo(DECAL_SMALL_DOT));
    
    public static final TexturePallette MACHINE_POWER_ON = REGISTRY.addTexturePallette("on", new TexturePalletteInfo(MATERIAL_GRADIENT));
    public static final TexturePallette MACHINE_POWER_OFF = REGISTRY.addTexturePallette("off", new TexturePalletteInfo(MATERIAL_GRADIENT));
 
    public static final TexturePallette MACHINE_GAUGE_INNER = REGISTRY.addTexturePallette("gauge_inner_256", new TexturePalletteInfo(MATERIAL_GRADIENT));
    public static final TexturePallette MACHINE_GAUGE_MAIN = REGISTRY.addTexturePallette("gauge_main_256", new TexturePalletteInfo(MATERIAL_GRADIENT));
    public static final TexturePallette MACHINE_GAGUE_MARKS = REGISTRY.addTexturePallette("gauge_background_256", new TexturePalletteInfo(MATERIAL_GRADIENT));
    public static final TexturePallette MACHINE_GAUGE_FULL_MARKS = REGISTRY.addTexturePallette("gauge_marks_256", new TexturePalletteInfo(MATERIAL_GRADIENT));

    
    private static TexturePallette addBorderSingle(String textureName)
    {
        return REGISTRY.addTexturePallette(textureName, 
                new TexturePalletteInfo().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
                .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));
    }
    
    private static TexturePallette addBorderRandom(String textureName, boolean renderNoBorderAsTile)
    {
        return REGISTRY.addTexturePallette(textureName, 
                new TexturePalletteInfo().withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
                .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS)
                .withRenderNoBorderAsTile(renderNoBorderAsTile));
    }
    
    private static TexturePallette addBorderRandomTile(String textureName)
    {
        return REGISTRY.addTexturePallette(textureName, 
                new TexturePalletteInfo().withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
                .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
                .withGroups(TextureGroup.STATIC_BORDERS, TextureGroup.STATIC_TILES)
                .withRenderNoBorderAsTile(true));
    }
    
    public static List<TexturePallette> getTexturesForSubstanceAndPaintLayer(Substance substance, PaintLayer layer)
    {
        int searchFlags = 0;
        switch(layer)
        {
        case BASE:
        case CUT:
        case LAMP:
            searchFlags = TextureGroup.STATIC_TILES.bitFlag | TextureGroup.DYNAMIC_TILES.bitFlag;
            if(Configurator.BLOCKS.showHiddenTextures) searchFlags |= TextureGroup.HIDDEN_TILES.bitFlag;
            break;

        case MIDDLE:
        case OUTER:
            searchFlags = TextureGroup.STATIC_DETAILS.bitFlag | TextureGroup.DYNAMIC_DETAILS.bitFlag
                         | TextureGroup.STATIC_BORDERS.bitFlag | TextureGroup.DYNAMIC_BORDERS.bitFlag;
            
            if(Configurator.BLOCKS.showHiddenTextures) 
                    searchFlags |= (TextureGroup.HIDDEN_DETAILS.bitFlag | TextureGroup.HIDDEN_BORDERS.bitFlag);
            
            break;
            
        default:
            break;
        
        }
        
        ImmutableList.Builder<TexturePallette> builder = ImmutableList.builder();
        for(TexturePallette t : REGISTRY)
        {
            if((t.textureGroupFlags & searchFlags) != 0)
            {
                builder.add(t);
            }
        }
        
        return builder.build();
    }
}
