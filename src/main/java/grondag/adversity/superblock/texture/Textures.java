package grondag.adversity.superblock.texture;


import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Configurator;
import grondag.adversity.Configurator.Substances.Substance;
import grondag.adversity.library.render.LightingMode;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.texture.TexturePalletteRegistry.TexturePallette;
import net.minecraft.util.BlockRenderLayer;

import static grondag.adversity.superblock.texture.TextureRotationType.*;
import static grondag.adversity.library.world.Rotation.*;

public class Textures
{
    /** Collection of all mod textures */
    public static final TexturePalletteRegistry REGISTRY = new TexturePalletteRegistry();
    
    static final LightingMode[] LIGHTING_BOTH = {LightingMode.FULLBRIGHT, LightingMode.SHADED};
    static final LightingMode[] LIGHTING_FULLBRIGHT_ONLY = {LightingMode.FULLBRIGHT};
    static final LightingMode[] LIGHTING_SHADED_ONLY = {LightingMode.SHADED};
    
    
    public static final int MAX_TEXTURES = 4096;
    
    //======================================================================
    //  SYSTEM TEXTURES - INITIAL RELEASE
    //======================================================================

    /**
     * Important that this come first so that it is the default value returned by modelState.
     * Is not meant for user selection. For CUT paint layer means should use same texture as base layer.
     * For DETAIL and OVERLAY layers, indicates those layers are disabled. 
     */
    public static final TexturePallette NONE = REGISTRY.addTexturePallette("noise_moderate", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.ALWAYS_HIDDEN);
    

    //FIXME : don't load test textures if not enabled
    public static final TexturePallette BIGTEX_TEST_SINGLE = REGISTRY.addTexturePallette("bigtex_single", 1, TextureScale.SMALL, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);
    public static final TexturePallette BIGTEX_TEST1 = REGISTRY.addTexturePallette("bigtex", 4, TextureScale.TINY, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);
    public static final TexturePallette BIGTEX_TEST2 = REGISTRY.addTexturePallette("bigtex", 4, TextureScale.SMALL, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);
    public static final TexturePallette BIGTEX_TEST3 = REGISTRY.addTexturePallette("bigtex", 4, TextureScale.MEDIUM, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);
    public static final TexturePallette BIGTEX_TEST4 = REGISTRY.addTexturePallette("bigtex", 4, TextureScale.LARGE, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);
    public static final TexturePallette BIGTEX_TEST5 = REGISTRY.addTexturePallette("bigtex", 4, TextureScale.GIANT, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);

    //======================================================================
    //  TILES - REGULAR
    //======================================================================
    public static final TexturePallette BLOCK_COBBLE = REGISTRY.addTexturePallette("cobble", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_COBBLE_ZOOM = REGISTRY.addZoomedPallete(BLOCK_COBBLE);
   
    public static final TexturePallette BLOCK_NOISE_STRONG = REGISTRY.addTexturePallette("noise_strong", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_NOISE_STRONG_ZOOM = REGISTRY.addZoomedPallete(BLOCK_NOISE_STRONG);
    
    public static final TexturePallette BLOCK_NOISE_MODERATE = REGISTRY.addTexturePallette("noise_moderate", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_NOISE_MODERATE_ZOOM = REGISTRY.addZoomedPallete(BLOCK_NOISE_MODERATE);
    
    public static final TexturePallette BLOCK_NOISE_SUBTLE = REGISTRY.addTexturePallette("noise_subtle", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, RANDOM.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_NOISE_SUBTLE_ZOOM = REGISTRY.addZoomedPallete(BLOCK_NOISE_SUBTLE);
    
    //======================================================================
    //  BORDERS
    //======================================================================
    
    public static final TexturePallette BORDER_SMOOTH_BLEND = REGISTRY.addTexturePallette("border_smooth_blended", 1, TextureScale.SINGLE, TextureLayout.BORDER_13, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_BORDERS);

    public static final TexturePallette MASONRY_SIMPLE = REGISTRY.addTexturePallette("masonry_simple", 1, TextureScale.SINGLE, TextureLayout.MASONRY_5, FIXED.with(ROTATE_NONE), BlockRenderLayer.CUTOUT_MIPPED, TextureGroup.STATIC_BORDERS);

    
    //======================================================================
    //  BIGTEX
    //======================================================================
    
    public static final TexturePallette BIGTEX_WEATHERED_STONE = REGISTRY.addTexturePallette("weathered_smooth_stone", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_WEATHERED_STONE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_WEATHERED_STONE);
    public static final TexturePallette BIGTEX_WEATHERED_STONE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_WEATHERED_STONE_ZOOM);
    
    public static final TexturePallette BIGTEX_SANDSTONE = REGISTRY.addTexturePallette("sandstone", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_SANDSTONE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_SANDSTONE);
    public static final TexturePallette BIGTEX_SANDSTONE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_SANDSTONE_ZOOM);

    public static final TexturePallette BIGTEX_ASPHALT = REGISTRY.addTexturePallette("asphalt", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_ASPHALT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_ASPHALT);
    public static final TexturePallette BIGTEX_ASPHALT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_ASPHALT_ZOOM);

    public static final TexturePallette BIGTEX_WORN_ASPHALT = REGISTRY.addTexturePallette("worn_asphalt", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_WORN_ASPHALT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_WORN_ASPHALT);
    public static final TexturePallette BIGTEX_WORN_ASPHALT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_WORN_ASPHALT_ZOOM);

    public static final TexturePallette BIGTEX_WOOD = REGISTRY.addTexturePallette("wood", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_WOOD_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_WOOD);
    public static final TexturePallette BIGTEX_WOOD_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_WOOD_ZOOM);
    
    public static final TexturePallette BIGTEX_WOOD_FLIP = REGISTRY.addTexturePallette("wood", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_WOOD_ZOOM_FLIP = REGISTRY.addZoomedPallete(BIGTEX_WOOD_FLIP);
    public static final TexturePallette BIGTEX_WOOD_ZOOM_X2_FLIP = REGISTRY.addZoomedPallete(BIGTEX_WOOD_ZOOM_FLIP);

    public static final TexturePallette BIGTEX_GRANITE = REGISTRY.addTexturePallette("granite", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_GRANITE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_GRANITE);
    public static final TexturePallette BIGTEX_GRANITE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_GRANITE_ZOOM);

    public static final TexturePallette BIGTEX_MARBLE = REGISTRY.addTexturePallette("marble", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_MARBLE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_MARBLE);
    public static final TexturePallette BIGTEX_MARBLE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_MARBLE_ZOOM);

    public static final TexturePallette BIGTEX_SLATE = REGISTRY.addTexturePallette("slate", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_SLATE_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_SLATE);
    public static final TexturePallette BIGTEX_SLATE_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_SLATE_ZOOM);

    public static final TexturePallette BIGTEX_ROUGH_ROCK = REGISTRY.addTexturePallette("rough_rock", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_ROUGH_ROCK_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_ROUGH_ROCK);
    public static final TexturePallette BIGTEX_ROUGH_ROCK_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_ROUGH_ROCK_ZOOM);

    public static final TexturePallette BIGTEX_CRACKED_EARTH = REGISTRY.addTexturePallette("cracked_earth", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_CRACKED_EARTH_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_CRACKED_EARTH);
    public static final TexturePallette BIGTEX_CRACKED_EARTH_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_CRACKED_EARTH_ZOOM);

    
    //======================================================================
    //  VOLCANO
    //======================================================================
    
    public static final TexturePallette BIGTEX_BASALT_CUT = REGISTRY.addTexturePallette("basalt_cut", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_BASALT_CUT_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_BASALT_CUT);
    public static final TexturePallette BIGTEX_BASALT_CUT_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_BASALT_CUT_ZOOM);
    
    public static final TexturePallette BIGTEX_BASALT_COOL = REGISTRY.addTexturePallette("basalt_cool", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_BASALT_COOL_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_BASALT_COOL);
    public static final TexturePallette BIGTEX_BASALT_COOL_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_BASALT_COOL_ZOOM);
    
    public static final TexturePallette BIGTEX_LAVA = REGISTRY.addTexturePallette("lava", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.HIDDEN_TILES);
    public static final TexturePallette BIGTEX_BASALT_COOLING = REGISTRY.addTexturePallette("basalt_cooling", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette BIGTEX_BASALT_WARM = REGISTRY.addTexturePallette("basalt_warm", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette BIGTEX_BASALT_HOT = REGISTRY.addTexturePallette("basalt_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette BIGTEX_BASALT_VERY_HOT = REGISTRY.addTexturePallette("basalt_very_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    
    //======================================================================
    //  BIGTEX - ANIMATED
    //======================================================================

    public static final TexturePallette BIGTEX_FLUID_GLOW = REGISTRY.addTexturePallette(animatedTextureName("fluid_glow"), 1, animatedTextueScale(TextureScale.MEDIUM), TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_FLUID_GLOW_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_FLUID_GLOW);
    public static final TexturePallette BIGTEX_FLUID_GLOW_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_FLUID_GLOW_ZOOM);

    public static final TexturePallette BIGTEX_FLUID_VORTEX = REGISTRY.addTexturePallette(animatedTextureName("fluid_vortex"), 1, animatedTextueScale(TextureScale.MEDIUM), TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_FLUID_VORTEX_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_FLUID_VORTEX);
    public static final TexturePallette BIGTEX_FLUID_VORTEX_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_FLUID_VORTEX_ZOOM);

    public static final TexturePallette BIGTEX_FLUID_VORTEX_S = REGISTRY.addTexturePallette(animatedTextureName("fluid_vortex"), 1, animatedTextueScale(TextureScale.MEDIUM), TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_FLUID_VORTEX_S_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_FLUID_VORTEX_S);
    public static final TexturePallette BIGTEX_FLUID_VORTEX_S_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_FLUID_VORTEX_S_ZOOM);
    
    public static final TexturePallette BIGTEX_CLOUDS = REGISTRY.addTexturePallette(animatedTextureName("clouds"), 1, animatedTextueScale(TextureScale.MEDIUM), TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_CLOUDS_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_CLOUDS);
    public static final TexturePallette BIGTEX_CLOUDS_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_CLOUDS_ZOOM);
    
    public static final TexturePallette BIGTEX_CLOUDS_S = REGISTRY.addTexturePallette(animatedTextureName("clouds"), 1, animatedTextueScale(TextureScale.MEDIUM), TextureLayout.BIGTEX, CONSISTENT.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_CLOUDS_S_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_CLOUDS_S);
    public static final TexturePallette BIGTEX_CLOUDS_S_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_CLOUDS_S_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_STARFIELD_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD);
    public static final TexturePallette BIGTEX_STARFIELD_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_S = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.SOLID, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_STARFIELD_S_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_S);
    public static final TexturePallette BIGTEX_STARFIELD_S_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_S_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_90 = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_STARFIELD_90_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_90);
    public static final TexturePallette BIGTEX_STARFIELD_90_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_90_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_90_S = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.SOLID, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_STARFIELD_90_S_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_90_S);
    public static final TexturePallette BIGTEX_STARFIELD_90_S_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_90_S_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_180 = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_180), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_STARFIELD_180_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_180);
    public static final TexturePallette BIGTEX_STARFIELD_180_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_180_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_180_S = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_180), BlockRenderLayer.SOLID, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_STARFIELD_180_S_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_180_S);
    public static final TexturePallette BIGTEX_STARFIELD_180_S_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_180_S_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_270 = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_270), BlockRenderLayer.TRANSLUCENT, TextureGroup.DYNAMIC_DETAILS);
    public static final TexturePallette BIGTEX_STARFIELD_270_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_270);
    public static final TexturePallette BIGTEX_STARFIELD_270_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_270_ZOOM);
    
    public static final TexturePallette BIGTEX_STARFIELD_270_S = REGISTRY.addTexturePallette(animatedTextureName("starfield"), 1, animatedTextueScale(TextureScale.SMALL), TextureLayout.BIGTEX, FIXED.with(ROTATE_270), BlockRenderLayer.SOLID, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_STARFIELD_270_S_ZOOM = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_270_S);
    public static final TexturePallette BIGTEX_STARFIELD_270_S_ZOOM_X2 = REGISTRY.addZoomedPallete(BIGTEX_STARFIELD_270_S_ZOOM);
    //FIXME: reorganize textures after this point before release
    
    //======================================================================
    //  DECALS
    //======================================================================
    public static final TexturePallette DECAL_SMALL_DOT = REGISTRY.addTexturePallette("small_dot", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_MEDIUM_DOT = REGISTRY.addTexturePallette("medium_dot", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_LARGE_DOT = REGISTRY.addTexturePallette("big_dot", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SMALL_SQUARE = REGISTRY.addTexturePallette("small_square", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_MEDIUM_SQUARE = REGISTRY.addTexturePallette("medium_square", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_LARGE_SQUARE = REGISTRY.addTexturePallette("big_square", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);

    public static final TexturePallette DECAL_SKINNY_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("thick_diagonal_cross_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("thick_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("thin_diagonal_cross_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("thin_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("thin_diagonal_cross_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_BARS = REGISTRY.addTexturePallette("thin_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_DIAGNAL_CROSS_BARS = REGISTRY.addTexturePallette("skinny_diagnal_cross_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_BARS = REGISTRY.addTexturePallette("skinny_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("diagonal_cross_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_BARS = REGISTRY.addTexturePallette("diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_DIAGONAL_CROSS_BARS = REGISTRY.addTexturePallette("fat_diagonal_cross_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_DIAGONAL_BARS = REGISTRY.addTexturePallette("fat_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_CROSS_RIDGES = REGISTRY.addTexturePallette("diagonal_cross_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_RIDGES = REGISTRY.addTexturePallette("diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_BARS = REGISTRY.addTexturePallette("skinny_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_BARS = REGISTRY.addTexturePallette("fat_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_BARS = REGISTRY.addTexturePallette("thick_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_BARS = REGISTRY.addTexturePallette("thin_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("thick_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("thin_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("thin_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("skinny_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_DIAGONAL_BARS_90 = REGISTRY.addTexturePallette("fat_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_RIDGES_90 = REGISTRY.addTexturePallette("diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_BARS_90 = REGISTRY.addTexturePallette("skinny_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_BARS_90 = REGISTRY.addTexturePallette("fat_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_BARS_90 = REGISTRY.addTexturePallette("thick_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_BARS_90 = REGISTRY.addTexturePallette("thin_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, FIXED.with(ROTATE_90), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);

    public static final TexturePallette DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("skinny_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("thick_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("thin_diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("thin_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("skinny_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_DIAGONAL_BARS_RANDOM = REGISTRY.addTexturePallette("fat_diagonal_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_DIAGONAL_RIDGES_RANDOM = REGISTRY.addTexturePallette("diagonal_ridges", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_SKINNY_BARS_RANDOM = REGISTRY.addTexturePallette("skinny_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_FAT_BARS_RANDOM = REGISTRY.addTexturePallette("fat_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THICK_BARS_RANDOM = REGISTRY.addTexturePallette("thick_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette DECAL_THIN_BARS_RANDOM = REGISTRY.addTexturePallette("thin_bars", 1, TextureScale.SINGLE, TextureLayout.BIGTEX, RANDOM.with(ROTATE_NONE), BlockRenderLayer.TRANSLUCENT, TextureGroup.STATIC_DETAILS);

    /** gives appropriate texture name for big-tex animated (or not, if turned off) textures based on user config */
    private static String animatedTextureName(String baseName)
    {
        if(Configurator.RENDER.enableAnimatedTextures)
        {
            return baseName + (Configurator.RENDER.useLargeAnimatedTextures ? "_anim" : "_anim_low");
        }
        else
        {
            return baseName + "_static";
        }
    }
    
    /** gives base (no-zoom) texture scale for big-tex animated textures based on user config */
    private static TextureScale animatedTextueScale(TextureScale largerScale)
    {
        return (Configurator.RENDER.enableAnimatedTextures && !Configurator.RENDER.useLargeAnimatedTextures)
                ? TextureScale.values()[largerScale.ordinal() - 1] : largerScale;
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

        case DETAIL:
            searchFlags = TextureGroup.STATIC_DETAILS.bitFlag | TextureGroup.DYNAMIC_DETAILS.bitFlag;
            if(Configurator.BLOCKS.showHiddenTextures) searchFlags |= TextureGroup.HIDDEN_DETAILS.bitFlag;
            break;
            
        case OVERLAY:
            searchFlags = TextureGroup.STATIC_BORDERS.bitFlag | TextureGroup.DYNAMIC_BORDERS.bitFlag;
            if(Configurator.BLOCKS.showHiddenTextures) searchFlags |= TextureGroup.HIDDEN_BORDERS.bitFlag;
            break;
            
        default:
            break;
        
        }
        
        ImmutableList.Builder<TexturePallette> builder = ImmutableList.builder();
        for(TexturePallette t : REGISTRY)
        {
            if((t.textureGroup.bitFlag & searchFlags) != 0)
            {
                builder.add(t);
            }
        }
        
        return builder.build();
    }
    
}
