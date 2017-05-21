package grondag.adversity.superblock.texture;


import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Configurator.Substances.Substance;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.util.BlockRenderLayer;

public class Textures
{
    public static final TexturePalletteProvider ALL_TEXTURES = new TexturePalletteProvider();
    
    static final LightingMode[] LIGHTING_BOTH = {LightingMode.FULLBRIGHT, LightingMode.SHADED};
    static final LightingMode[] LIGHTING_FULLBRIGHT_ONLY = {LightingMode.FULLBRIGHT};
    static final LightingMode[] LIGHTING_SHADED_ONLY = {LightingMode.SHADED};
    
    static final BlockRenderLayer[] SOLID_ONLY = {BlockRenderLayer.SOLID};
    static final BlockRenderLayer[] CUTOUT_M_ONLY = {BlockRenderLayer.CUTOUT_MIPPED};
    static final BlockRenderLayer[] CUTOUT_ONLY = {BlockRenderLayer.CUTOUT};
    static final BlockRenderLayer[] TRANS_ONLY = {BlockRenderLayer.TRANSLUCENT};
    static final BlockRenderLayer[] SOLID_AND_CUTOUT_M = {BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT_MIPPED};
    static final BlockRenderLayer[] SOLID_AND_CUTOUT = {BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT};
    static final BlockRenderLayer[] SOLID_AND_TRANS = {BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT};
    
    
    public static final int MAX_TEXTURES = 4096;
      
    public static final TexturePallette BLOCK_COBBLE = ALL_TEXTURES.addTexturePallette("cobble", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_COBBLE_ZOOM = ALL_TEXTURES.addZoomedPallete(BLOCK_COBBLE);
   
    public static final TexturePallette BLOCK_RAW_FLEXSTONE = ALL_TEXTURES.addTexturePallette("raw_flexstone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_RAW_FLEXSTONE_ZOOM = ALL_TEXTURES.addZoomedPallete(BLOCK_RAW_FLEXSTONE);
    
    public static final TexturePallette BLOCK_RAW_DURASTONE = ALL_TEXTURES.addTexturePallette("raw_durastone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_RAW_DURASTONE_ZOOM = ALL_TEXTURES.addZoomedPallete(BLOCK_RAW_DURASTONE);
    
    public static final TexturePallette BLOCK_COLORED_STONE = ALL_TEXTURES.addTexturePallette("colored_stone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY, TextureGroup.STATIC_TILES);
    public static final TexturePallette BLOCK_COLORED_STONE_ZOOM = ALL_TEXTURES.addZoomedPallete(BLOCK_COLORED_STONE);
    
    public static final TexturePallette BIGTEX_WEATHERED_STONE = ALL_TEXTURES.addTexturePallette("weathered_smooth_stone", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_SHADED_ONLY, SOLID_ONLY, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_WEATHERED_STONE_ZOOM = ALL_TEXTURES.addZoomedPallete(BIGTEX_WEATHERED_STONE);
    
    public static final TexturePallette BIGTEX_BASALT_CUT = ALL_TEXTURES.addTexturePallette("basalt_cut", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_BASALT_CUT_ZOOM = ALL_TEXTURES.addZoomedPallete(BIGTEX_BASALT_CUT);

    public static final TexturePallette BIGTEX_BASALT_COOL = ALL_TEXTURES.addTexturePallette("basalt_cool", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_BASALT_COOL_ZOOM = ALL_TEXTURES.addZoomedPallete(BIGTEX_BASALT_COOL);
    public static final TexturePallette BIGTEX_BASALT_COOL_ZOOM_X2 = ALL_TEXTURES.addZoomedPallete(BIGTEX_BASALT_COOL_ZOOM);

    
    public static final TexturePallette BIGTEX_LAVA = ALL_TEXTURES.addTexturePallette("lava", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.DYNAMIC_TILES);
    public static final TexturePallette BIGTEX_BASALT_COOLING = ALL_TEXTURES.addTexturePallette("basalt_cooling", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette BIGTEX_BASALT_WARM = ALL_TEXTURES.addTexturePallette("basalt_warm", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette BIGTEX_BASALT_HOT = ALL_TEXTURES.addTexturePallette("basalt_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_DETAILS);
    public static final TexturePallette BIGTEX_BASALT_VERY_HOT = ALL_TEXTURES.addTexturePallette("basalt_very_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_DETAILS);
    
    
    public static final TexturePallette BORDER_TEST = ALL_TEXTURES.addTexturePallette("bordertest", 1, TextureScale.SINGLE, TextureLayout.BORDER_13, false, LIGHTING_SHADED_ONLY, TRANS_ONLY, TextureGroup.STATIC_BORDERS);

    public static final TexturePallette MASONRY_TEST = ALL_TEXTURES.addTexturePallette("masonrytest", 1, TextureScale.SINGLE, TextureLayout.MASONRY_5, false, LIGHTING_SHADED_ONLY, CUTOUT_M_ONLY, TextureGroup.STATIC_BORDERS);

    public static final TexturePallette BIGTEX_TEST1 = ALL_TEXTURES.addTexturePallette("bigtex", 4, TextureScale.TINY, TextureLayout.SPLIT_X_8, true, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_TEST2 = ALL_TEXTURES.addTexturePallette("bigtex", 4, TextureScale.SMALL, TextureLayout.SPLIT_X_8, true, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_TEST3 = ALL_TEXTURES.addTexturePallette("bigtex", 4, TextureScale.MEDIUM, TextureLayout.SPLIT_X_8, true, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_TEST4 = ALL_TEXTURES.addTexturePallette("bigtex", 4, TextureScale.LARGE, TextureLayout.SPLIT_X_8, true, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    public static final TexturePallette BIGTEX_TEST5 = ALL_TEXTURES.addTexturePallette("bigtex", 4, TextureScale.GIANT, TextureLayout.SPLIT_X_8, true, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);

    public static final TexturePallette BIGTEX_TEST_SINGLE = ALL_TEXTURES.addTexturePallette("bigtex_single", 1, TextureScale.SMALL, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS, TextureGroup.STATIC_TILES);
    
    public static List<TexturePallette> getTexturesForSubstanceAndPaintLayer(Substance substance, PaintLayer layer)
    {
        //TODO: temporary hack
        
        int searchFlags = 0;
        switch(layer)
        {
        case BASE:
        case CUT:
        case LAMP:
            searchFlags = TextureGroup.STATIC_TILES.bitFlag | TextureGroup.DYNAMIC_TILES.bitFlag;
            break;

        case DETAIL:
            searchFlags = TextureGroup.STATIC_DETAILS.bitFlag | TextureGroup.DYNAMIC_DETAILS.bitFlag;
            break;
            
        case OVERLAY:
            searchFlags = TextureGroup.STATIC_BORDERS.bitFlag | TextureGroup.DYNAMIC_BORDERS.bitFlag;
            break;
            
        default:
            break;
        
        }
        
        ImmutableList.Builder<TexturePallette> builder = ImmutableList.builder();
        for(TexturePallette t : ALL_TEXTURES)
        {
            if((t.textureGroup.bitFlag & searchFlags) != 0)
            {
                builder.add(t);
            }
        }
        
        return builder.build();
    }
    
}
