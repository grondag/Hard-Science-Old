package grondag.hard_science.init;


import static grondag.exotic_matter.model.TextureRotationType.CONSISTENT;
import static grondag.exotic_matter.model.TextureRotationType.FIXED;
import static grondag.exotic_matter.model.TextureRotationType.RANDOM;
import static grondag.exotic_matter.world.Rotation.ROTATE_180;
import static grondag.exotic_matter.world.Rotation.ROTATE_270;
import static grondag.exotic_matter.world.Rotation.ROTATE_90;
import static grondag.exotic_matter.world.Rotation.ROTATE_NONE;

import grondag.exotic_matter.model.ITexturePalette;
import grondag.exotic_matter.model.TextureGroup;
import grondag.exotic_matter.model.TextureLayout;
import grondag.exotic_matter.model.TexturePaletteRegistry;
import grondag.exotic_matter.model.TexturePaletteSpec;
import grondag.exotic_matter.model.TextureRenderIntent;
import grondag.exotic_matter.model.TextureScale;

public class ModTextures
{
    
    //======================================================================
    //  BORDERS
    //======================================================================
    
    public static final ITexturePalette BORDER_SMOOTH_BLEND = TexturePaletteRegistry.addTexturePallette("border_smooth_blended", "border_smooth_blended", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));

    public static final ITexturePalette MASONRY_SIMPLE = TexturePaletteRegistry.addTexturePallette("masonry_simple", "masonry_simple", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.MASONRY_5)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));

    public static final ITexturePalette BORDER_SINGLE_PINSTRIPE = TexturePaletteRegistry.addBorderSingle("border_single_pinstripe");
    public static final ITexturePalette BORDER_INSET_PINSTRIPE = TexturePaletteRegistry.addBorderSingle("border_inset_pinstripe");
    public static final ITexturePalette BORDER_GRITTY_INSET_PINSTRIPE = TexturePaletteRegistry.addBorderRandom("border_gritty_inset_pinstripe", false, false);
    public static final ITexturePalette BORDER_SINGLE_LINE = TexturePaletteRegistry.addBorderSingle("border_single_line");
    public static final ITexturePalette BORDER_SINGLE_BOLD_LINE = TexturePaletteRegistry.addBorderSingle("border_single_bold_line");
    public static final ITexturePalette BORDER_SINGLE_FAT_LINE = TexturePaletteRegistry.addBorderSingle("border_single_fat_line");
    public static final ITexturePalette BORDER_GRITTY_FAT_LINE = TexturePaletteRegistry.addBorderRandom("border_gritty_fat_line", false, false);
    public static final ITexturePalette BORDER_DOUBLE_MIXED_LINES = TexturePaletteRegistry.addBorderSingle("border_double_mixed_lines");
    public static final ITexturePalette BORDER_DOUBLE_PINSTRIPES = TexturePaletteRegistry.addBorderSingle("border_double_pinstripes");
    public static final ITexturePalette BORDER_INSET_DOUBLE_PINSTRIPES = TexturePaletteRegistry.addBorderSingle("border_inset_double_pinstripes");
    public static final ITexturePalette BORDER_TRIPLE_MIXED_LINES = TexturePaletteRegistry.addBorderSingle("border_triple_mixed_lines");
    public static final ITexturePalette BORDER_DOUBLE_DOUBLE = TexturePaletteRegistry.addBorderSingle("border_double_double");
    public static final ITexturePalette BORDER_WHITEWALL = TexturePaletteRegistry.addBorderSingle("border_whitewall");
    public static final ITexturePalette BORDER_GRITTY_WHITEWALL = TexturePaletteRegistry.addBorderRandom("border_gritty_whitewall", false, false);
    
    public static final ITexturePalette BORDER_PINSTRIPE_DASH = TexturePaletteRegistry.addBorderSingle("border_pinstripe_dash");
    public static final ITexturePalette BORDER_INSET_DOTS_1 = TexturePaletteRegistry.addBorderSingle("border_inset_dots_1");
    public static final ITexturePalette BORDER_INSET_DOTS_2 = TexturePaletteRegistry.addBorderSingle("border_inset_dots_2");
    public static final ITexturePalette BORDER_INSET_PIN_DOTS = TexturePaletteRegistry.addBorderSingle("border_inset_pin_dots");
    public static final ITexturePalette BORDER_CHANNEL_DOTS = TexturePaletteRegistry.addBorderSingle("border_channel_dots");
    public static final ITexturePalette BORDER_CHANNEL_PIN_DOTS = TexturePaletteRegistry.addBorderSingle("border_channel_pin_dots");
    
    public static final ITexturePalette BORDER_CHANNEL_CHECKERBOARD = TexturePaletteRegistry.addBorderSingle("border_channel_checkerboard");
    public static final ITexturePalette BORDER_CHECKERBOARD = TexturePaletteRegistry.addBorderSingle("border_checkerboard");
    public static final ITexturePalette BORDER_GRITTY_CHECKERBOARD = TexturePaletteRegistry.addBorderRandom("border_gritty_checkerboard", false, false);
    
    public static final ITexturePalette BORDER_GROOVY_STRIPES = TexturePaletteRegistry.addBorderSingle("border_groovy_stripes");
    public static final ITexturePalette BORDER_GRITTY_GROOVES = TexturePaletteRegistry.addBorderRandom("border_gritty_grooves", false, false);
    public static final ITexturePalette BORDER_GROOVY_PINSTRIPES = TexturePaletteRegistry.addBorderSingle("border_groovy_pinstripes");
    public static final ITexturePalette BORDER_GRITTY_PINSTRIPE_GROOVES = TexturePaletteRegistry.addBorderRandom("border_gritty_pinstripe_grooves", false, false);
    
    public static final ITexturePalette BORDER_ZIGZAG = TexturePaletteRegistry.addBorderSingle("border_zigzag");
    public static final ITexturePalette BORDER_INVERSE_ZIGZAG = TexturePaletteRegistry.addBorderSingle("border_inverse_zigzag");
    public static final ITexturePalette BORDER_CAUTION = TexturePaletteRegistry.addBorderSingle("border_caution");
    public static final ITexturePalette BORDER_FILMSTRIP = TexturePaletteRegistry.addBorderSingle("border_filmstrip");
    public static final ITexturePalette BORDER_CHANNEL_LINES = TexturePaletteRegistry.addBorderSingle("border_channel_lines");
    public static final ITexturePalette BORDER_SIGNAL = TexturePaletteRegistry.addBorderSingle("border_signal");
    public static final ITexturePalette BORDER_GRITTY_SIGNAL = TexturePaletteRegistry.addBorderRandom("border_gritty_signal", false, false);
    public static final ITexturePalette BORDER_LOGIC = TexturePaletteRegistry.addBorderRandom("border_logic", true, false);
    public static final ITexturePalette BORDER_INVERSE_TILE_1 = TexturePaletteRegistry.addBorderRandom("border_inverse_logic_1", true, true);
    public static final ITexturePalette BORDER_INVERSE_TILE_2 = TexturePaletteRegistry.addBorderRandom("border_inverse_logic_2", true, true);

    //======================================================================
    //  BIGTEX
    //======================================================================
    
    public static final ITexturePalette BIGTEX_WEATHERED_STONE = TexturePaletteRegistry.addTexturePallette("weathered_smooth_stone", "weathered_smooth_stone", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.STATIC_TILES));
    public static final ITexturePalette BIGTEX_WEATHERED_STONE_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WEATHERED_STONE);
    public static final ITexturePalette BIGTEX_WEATHERED_STONE_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WEATHERED_STONE_ZOOM);
    
    public static final ITexturePalette BIGTEX_SANDSTONE = TexturePaletteRegistry.addTexturePallette("sandstone", "sandstone", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_SANDSTONE_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_SANDSTONE);
    public static final ITexturePalette BIGTEX_SANDSTONE_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_SANDSTONE_ZOOM);

    public static final ITexturePalette BIGTEX_ASPHALT = TexturePaletteRegistry.addTexturePallette("asphalt", "asphalt", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_ASPHALT_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_ASPHALT);
    public static final ITexturePalette BIGTEX_ASPHALT_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_ASPHALT_ZOOM);

    public static final ITexturePalette BIGTEX_WORN_ASPHALT = TexturePaletteRegistry.addTexturePallette("worn_asphalt", "worn_asphalt", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_WORN_ASPHALT_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WORN_ASPHALT);
    public static final ITexturePalette BIGTEX_WORN_ASPHALT_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WORN_ASPHALT_ZOOM);

    public static final ITexturePalette BIGTEX_WOOD = TexturePaletteRegistry.addTexturePallette("wood", "wood", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE).withRotation(FIXED.with(ROTATE_NONE)));
    public static final ITexturePalette BIGTEX_WOOD_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WOOD);
    public static final ITexturePalette BIGTEX_WOOD_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WOOD_ZOOM);
    
    public static final ITexturePalette BIGTEX_WOOD_FLIP = TexturePaletteRegistry.addTexturePallette("wood", "wood", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette BIGTEX_WOOD_ZOOM_FLIP = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WOOD_FLIP);
    public static final ITexturePalette BIGTEX_WOOD_ZOOM_X2_FLIP = TexturePaletteRegistry.addZoomedPallete(BIGTEX_WOOD_ZOOM_FLIP);

    public static final ITexturePalette BIGTEX_GRANITE = TexturePaletteRegistry.addTexturePallette("granite", "granite", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_GRANITE_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_GRANITE);
    public static final ITexturePalette BIGTEX_GRANITE_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_GRANITE_ZOOM);

    public static final ITexturePalette BIGTEX_MARBLE = TexturePaletteRegistry.addTexturePallette("marble", "marble", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_MARBLE_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_MARBLE);
    public static final ITexturePalette BIGTEX_MARBLE_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_MARBLE_ZOOM);

    public static final ITexturePalette BIGTEX_SLATE = TexturePaletteRegistry.addTexturePallette("slate", "slate", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_SLATE_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_SLATE);
    public static final ITexturePalette BIGTEX_SLATE_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_SLATE_ZOOM);

    public static final ITexturePalette BIGTEX_ROUGH_ROCK = TexturePaletteRegistry.addTexturePallette("rough_rock", "rough_rock", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_ROUGH_ROCK_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_ROUGH_ROCK);
    public static final ITexturePalette BIGTEX_ROUGH_ROCK_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_ROUGH_ROCK_ZOOM);

    public static final ITexturePalette BIGTEX_CRACKED_EARTH = TexturePaletteRegistry.addTexturePallette("cracked_earth", "cracked_earth", new TexturePaletteSpec(BIGTEX_WEATHERED_STONE));
    public static final ITexturePalette BIGTEX_CRACKED_EARTH_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_CRACKED_EARTH);
    public static final ITexturePalette BIGTEX_CRACKED_EARTH_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_CRACKED_EARTH_ZOOM);
    
    //======================================================================
    //  BIGTEX - ANIMATED
    //======================================================================

    public static final ITexturePalette BIGTEX_FLUID_GLOW = TexturePaletteRegistry.addTexturePallette("fluid_glow", "fluid_glow", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.DYNAMIC_DETAILS));
    public static final ITexturePalette BIGTEX_FLUID_GLOW_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_FLUID_GLOW);
    public static final ITexturePalette BIGTEX_FLUID_GLOW_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_FLUID_GLOW_ZOOM);

    public static final ITexturePalette BIGTEX_FLUID_VORTEX = TexturePaletteRegistry.addTexturePallette("fluid_vortex", "fluid_vortex", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .withGroups(TextureGroup.DYNAMIC_DETAILS, TextureGroup.DYNAMIC_TILES)
            .withTicksPerFrame(2));
    public static final ITexturePalette BIGTEX_FLUID_VORTEX_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_FLUID_VORTEX);
    public static final ITexturePalette BIGTEX_FLUID_VORTEX_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_FLUID_VORTEX_ZOOM);

    public static final ITexturePalette BIGTEX_CLOUDS = TexturePaletteRegistry.addTexturePallette("clouds", "clouds", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.MEDIUM).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(CONSISTENT.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .withGroups(TextureGroup.DYNAMIC_DETAILS, TextureGroup.DYNAMIC_TILES));
    public static final ITexturePalette BIGTEX_CLOUDS_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_CLOUDS);
    public static final ITexturePalette BIGTEX_CLOUDS_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_CLOUDS_ZOOM);
    
    public static final ITexturePalette BIGTEX_STARFIELD = TexturePaletteRegistry.addTexturePallette("starfield", "starfield", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.SMALL).withLayout(TextureLayout.BIGTEX_ANIMATED)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .withGroups(TextureGroup.DYNAMIC_DETAILS, TextureGroup.DYNAMIC_TILES));
    public static final ITexturePalette BIGTEX_STARFIELD_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD);
    public static final ITexturePalette BIGTEX_STARFIELD_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_ZOOM);
    
    public static final ITexturePalette BIGTEX_STARFIELD_90 = TexturePaletteRegistry.addTexturePallette("starfield_90", "starfield", 
            new TexturePaletteSpec(BIGTEX_STARFIELD).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette BIGTEX_STARFIELD_90_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_90);
    public static final ITexturePalette BIGTEX_STARFIELD_90_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_90_ZOOM);
    
    public static final ITexturePalette BIGTEX_STARFIELD_180 = TexturePaletteRegistry.addTexturePallette("starfield_180", "starfield", 
            new TexturePaletteSpec(BIGTEX_STARFIELD).withRotation(FIXED.with(ROTATE_180)));
    public static final ITexturePalette BIGTEX_STARFIELD_180_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_180);
    public static final ITexturePalette BIGTEX_STARFIELD_180_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_180_ZOOM);
    
      public static final ITexturePalette BIGTEX_STARFIELD_270 = TexturePaletteRegistry.addTexturePallette("starfield_270", "starfield", 
            new TexturePaletteSpec(BIGTEX_STARFIELD).withRotation(FIXED.with(ROTATE_270)));
    public static final ITexturePalette BIGTEX_STARFIELD_270_ZOOM = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_270);
    public static final ITexturePalette BIGTEX_STARFIELD_270_ZOOM_X2 = TexturePaletteRegistry.addZoomedPallete(BIGTEX_STARFIELD_270_ZOOM);
    
    //======================================================================
    //  DECALS
    //======================================================================
    public static final ITexturePalette DECAL_SMALL_DOT = TexturePaletteRegistry.addTexturePallette("small_dot", "small_dot", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BIGTEX)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_DETAILS));
    
    public static final ITexturePalette DECAL_MEDIUM_DOT = TexturePaletteRegistry.addTexturePallette("medium_dot", "medium_dot", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_LARGE_DOT = TexturePaletteRegistry.addTexturePallette("big_dot", "big_dot", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SMALL_SQUARE = TexturePaletteRegistry.addTexturePallette("small_square", "small_square", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_MEDIUM_SQUARE = TexturePaletteRegistry.addTexturePallette("medium_square", "medium_square", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_LARGE_SQUARE = TexturePaletteRegistry.addTexturePallette("big_square", "big_square", new TexturePaletteSpec(DECAL_SMALL_DOT));

    public static final ITexturePalette DECAL_SKINNY_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_ridges", "skinny_diagonal_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THICK_DIAGONAL_CROSS_RIDGES = TexturePaletteRegistry.addTexturePallette("thick_diagonal_cross_ridges", "thick_diagonal_cross_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THICK_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("thick_diagonal_ridges", "thick_diagonal_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_CROSS_RIDGES = TexturePaletteRegistry.addTexturePallette("thin_diagonal_cross_ridges", "thin_diagonal_cross_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("thin_diagonal_ridges", "thin_diagonal_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("thin_diagonal_cross_bars", "thin_diagonal_cross_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("thin_diagonal_bars", "thin_diagonal_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SKINNY_DIAGNAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_cross_bars", "skinny_diagonal_cross_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SKINNY_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_bars", "skinny_diagonal_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_DIAGONAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("diagonal_cross_bars", "diagonal_cross_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("diagonal_bars", "diagonal_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_FAT_DIAGONAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("fat_diagonal_cross_bars", "fat_diagonal_cross_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_FAT_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("fat_diagonal_bars", "fat_diagonal_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_DIAGONAL_CROSS_RIDGES = TexturePaletteRegistry.addTexturePallette("diagonal_cross_ridges", "diagonal_cross_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("diagonal_ridges", "diagonal_ridges", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SKINNY_BARS = TexturePaletteRegistry.addTexturePallette("skinny_bars", "skinny_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_FAT_BARS = TexturePaletteRegistry.addTexturePallette("fat_bars", "fat_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THICK_BARS = TexturePaletteRegistry.addTexturePallette("thick_bars", "thick_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_THIN_BARS = TexturePaletteRegistry.addTexturePallette("thin_bars", "thin_bars", new TexturePaletteSpec(DECAL_SMALL_DOT));
    
    public static final ITexturePalette DECAL_SKINNY_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_ridges_90", "skinny_diagonal_ridges", 
            new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette DECAL_THICK_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("thick_diagonal_ridges_90", "thick_diagonal_ridges", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("thin_diagonal_ridges_90", "thin_diagonal_ridges", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("thin_diagonal_bars_90", "thin_diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SKINNY_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_bars_90", "skinny_diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("diagonal_bars_90", "diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_FAT_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("fat_diagonal_bars_90", "fat_diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("diagonal_ridges_90", "diagonal_ridges", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SKINNY_BARS_90 = TexturePaletteRegistry.addTexturePallette("skinny_bars_90", "skinny_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_FAT_BARS_90 = TexturePaletteRegistry.addTexturePallette("fat_bars_90", "fat_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_THICK_BARS_90 = TexturePaletteRegistry.addTexturePallette("thick_bars_90", "thick_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_THIN_BARS_90 = TexturePaletteRegistry.addTexturePallette("thin_bars_90", "thin_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_90));

    public static final ITexturePalette DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_ridges_random", "skinny_diagonal_ridges", 
            new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(RANDOM.with(ROTATE_NONE)));
    public static final ITexturePalette DECAL_THICK_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("thick_diagonal_ridges_random", "thick_diagonal_ridges", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("thin_diagonal_ridges_random", "thin_diagonal_ridges", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_THIN_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("thin_diagonal_bars_random", "thin_diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SKINNY_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_bars_random", "skinny_diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("diagonal_bars_random", "diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_FAT_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("fat_diagonal_bars_random", "fat_diagonal_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("diagonal_ridges_random", "diagonal_ridges", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SKINNY_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("skinny_bars_random", "skinny_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_FAT_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("fat_bars_random", "fat_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_THICK_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("thick_bars_random", "thick_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_THIN_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("thin_bars_random", "thin_bars", new TexturePaletteSpec(DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM));
    
    
    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_ridges_seamless", "skinny_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_THICK_DIAGONAL_CROSS_RIDGES = TexturePaletteRegistry.addTexturePallette("thick_diagonal_cross_ridges_seamless", "thick_diagonal_cross_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_THICK_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("thick_diagonal_ridges_seamless", "thick_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_CROSS_RIDGES = TexturePaletteRegistry.addTexturePallette("thin_diagonal_cross_ridges_seamless", "thin_diagonal_cross_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("thin_diagonal_ridges_seamless", "thin_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("thin_diagonal_cross_bars_seamless", "thin_diagonal_cross_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("thin_diagonal_bars_seamless", "thin_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGNAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_cross_bars_seamless", "skinny_diagonal_cross_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_bars_seamless", "skinny_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("diagonal_cross_bars_seamless", "diagonal_cross_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("diagonal_bars_seamless", "diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_FAT_DIAGONAL_CROSS_BARS = TexturePaletteRegistry.addTexturePallette("fat_diagonal_cross_bars_seamless", "fat_diagonal_cross_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_FAT_DIAGONAL_BARS = TexturePaletteRegistry.addTexturePallette("fat_diagonal_bars_seamless", "fat_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_CROSS_RIDGES = TexturePaletteRegistry.addTexturePallette("diagonal_cross_ridges_seamless", "diagonal_cross_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_RIDGES = TexturePaletteRegistry.addTexturePallette("diagonal_ridges_seamless", "diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SMALL_DOT));

    
    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_ridges_90", "skinny_diagonal_ridges", 
            new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette DECAL_SOFT_THICK_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("thick_diagonal_ridges_seamless_90", "thick_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("thin_diagonal_ridges_seamless_90", "thin_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("thin_diagonal_bars_seamless_90", "thin_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_bars_seamless_90", "skinny_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("diagonal_bars_seamless_90", "diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SOFT_FAT_DIAGONAL_BARS_90 = TexturePaletteRegistry.addTexturePallette("fat_diagonal_bars_seamless_90", "fat_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_RIDGES_90 = TexturePaletteRegistry.addTexturePallette("diagonal_ridges_seamless_90", "diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90));

    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_ridges_random", "skinny_diagonal_ridges", 
            new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(RANDOM.with(ROTATE_NONE)));
    public static final ITexturePalette DECAL_SOFT_THICK_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("thick_diagonal_ridges_seamless_random", "thick_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("thin_diagonal_ridges_seamless_random", "thin_diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SOFT_THIN_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("thin_diagonal_bars_seamless_random", "thin_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SOFT_SKINNY_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("skinny_diagonal_bars_seamless_random", "skinny_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("diagonal_bars_seamless_random", "diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SOFT_FAT_DIAGONAL_BARS_RANDOM = TexturePaletteRegistry.addTexturePallette("fat_diagonal_bars_seamless_random", "fat_diagonal_bars_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    public static final ITexturePalette DECAL_SOFT_DIAGONAL_RIDGES_RANDOM = TexturePaletteRegistry.addTexturePallette("diagonal_ridges_seamless_random", "diagonal_ridges_seamless", new TexturePaletteSpec(DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM));
    
    public static final ITexturePalette DECAL_BIG_TRIANGLE = TexturePaletteRegistry.addTexturePallette("big_triangle", "big_triangle", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_BIG_DIAMOND = TexturePaletteRegistry.addTexturePallette("big_diamond", "big_diamond", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_BIG_PENTAGON = TexturePaletteRegistry.addTexturePallette("big_pentagon", "big_pentagon", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_BIG_HEXAGON = TexturePaletteRegistry.addTexturePallette("big_hexagon", "big_hexagon", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_STAR_16 = TexturePaletteRegistry.addTexturePallette("star_16", "star_16", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_STAR_12 = TexturePaletteRegistry.addTexturePallette("star_12", "star_12", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_STAR_8 = TexturePaletteRegistry.addTexturePallette("star_8", "star_8", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_STAR_5 = TexturePaletteRegistry.addTexturePallette("star_5", "star_5", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_STAR_4 = TexturePaletteRegistry.addTexturePallette("star_4", "star_4", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_TWO_DOTS = TexturePaletteRegistry.addTexturePallette("two_dots", "two_dots", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_TWO_DOTS_RANDOM = TexturePaletteRegistry.addTexturePallette("two_dots", "two_dots", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(RANDOM.with(ROTATE_NONE)));
    public static final ITexturePalette DECAL_DUST = TexturePaletteRegistry.addTexturePallette("dust", "dust", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_MIX = TexturePaletteRegistry.addTexturePallette("mix", "mix", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_NONE)));
    public static final ITexturePalette DECAL_MIX_90 = TexturePaletteRegistry.addTexturePallette("mix_90", "mix", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette DECAL_MIX_180 = TexturePaletteRegistry.addTexturePallette("mix_180", "mix", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_180)));
    public static final ITexturePalette DECAL_MIX_270 = TexturePaletteRegistry.addTexturePallette("mix_270", "mix", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_270)));
    public static final ITexturePalette DECAL_DRIP = TexturePaletteRegistry.addTexturePallette("drip", "drip", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_FUNNEL = TexturePaletteRegistry.addTexturePallette("funnel", "funnel", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_ARROW = TexturePaletteRegistry.addTexturePallette("arrow", "arrow", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_NONE)));
    public static final ITexturePalette DECAL_ARROW_90 = TexturePaletteRegistry.addTexturePallette("arrow_90", "arrow", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette DECAL_ARROW_180 = TexturePaletteRegistry.addTexturePallette("arrow_180", "arrow", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_180)));
    public static final ITexturePalette DECAL_ARROW_270 = TexturePaletteRegistry.addTexturePallette("arrow_270", "arrow", new TexturePaletteSpec(DECAL_SMALL_DOT).withRotation(FIXED.with(ROTATE_270)));
    
    public static final ITexturePalette MATERIAL_GRADIENT = TexturePaletteRegistry.addTexturePallette("arrow", "material_gradient", 
            new TexturePaletteSpec().withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BIGTEX)
            .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.HIDDEN_TILES));

    public static final ITexturePalette DECAL_BUILDER = TexturePaletteRegistry.addTexturePallette("symbol_builder", "symbol_builder", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_CHEST = TexturePaletteRegistry.addTexturePallette("symbol_chest", "symbol_chest", new TexturePaletteSpec(DECAL_SMALL_DOT));
    
    public static final ITexturePalette MACHINE_POWER_ON = TexturePaletteRegistry.addTexturePallette("on", "on", new TexturePaletteSpec(MATERIAL_GRADIENT));
    public static final ITexturePalette MACHINE_POWER_OFF = TexturePaletteRegistry.addTexturePallette("off", "off", new TexturePaletteSpec(MATERIAL_GRADIENT));
 
    public static final ITexturePalette MACHINE_GAUGE_INNER = TexturePaletteRegistry.addTexturePallette("gauge_inner", "gauge_inner_256", new TexturePaletteSpec(MATERIAL_GRADIENT));
    public static final ITexturePalette MACHINE_GAUGE_MAIN = TexturePaletteRegistry.addTexturePallette("gauge_main", "gauge_main_256", new TexturePaletteSpec(MATERIAL_GRADIENT));
    public static final ITexturePalette MACHINE_GAGUE_MARKS = TexturePaletteRegistry.addTexturePallette("gauge_background", "gauge_background_256", new TexturePaletteSpec(MATERIAL_GRADIENT));
    public static final ITexturePalette MACHINE_GAUGE_FULL_MARKS = TexturePaletteRegistry.addTexturePallette("gauge_marks", "gauge_marks_256", new TexturePaletteSpec(MATERIAL_GRADIENT));

    public static final ITexturePalette MACHINE_POWER_BACKGROUND = TexturePaletteRegistry.addTexturePallette("power_background", "power_background_128", new TexturePaletteSpec(MATERIAL_GRADIENT));
    public static final ITexturePalette MACHINE_POWER_FOREGROUND = TexturePaletteRegistry.addTexturePallette("power_foreground", "power_foreground_128", new TexturePaletteSpec(MATERIAL_GRADIENT));
    public static final ITexturePalette DECAL_NO = TexturePaletteRegistry.addTexturePallette("no", "no_128", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_MATERIAL_SHORTAGE = TexturePaletteRegistry.addTexturePallette("material_shortage", "material_shortage", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_ELECTRICITY = TexturePaletteRegistry.addTexturePallette("electricity", "electricity_64", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_CMY = TexturePaletteRegistry.addTexturePallette("cmy", "cmy", new TexturePaletteSpec(DECAL_SMALL_DOT));
    public static final ITexturePalette DECAL_FLAME = TexturePaletteRegistry.addTexturePallette("flame", "flame_64", new TexturePaletteSpec(DECAL_SMALL_DOT));

    public static final ITexturePalette TILE_DOTS = TexturePaletteRegistry.addTexturePallette("dots", "dots", 
            new TexturePaletteSpec().withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.SPLIT_X_8)
            .withRotation(RANDOM.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT).withGroups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS));

    public static final ITexturePalette TILE_DOTS_SUBTLE = TexturePaletteRegistry.addTexturePallette("dots_subtle", "dots_subtle", new TexturePaletteSpec(TILE_DOTS));
    public static final ITexturePalette TILE_DOTS_INVERSE = TexturePaletteRegistry.addTexturePallette("dots_inverse", "dots_inverse", new TexturePaletteSpec(TILE_DOTS));
    public static final ITexturePalette TILE_DOTS_INVERSE_SUBTLE = TexturePaletteRegistry.addTexturePallette("dots_inverse_subtle", "dots_inverse_subtle", new TexturePaletteSpec(TILE_DOTS));


}
