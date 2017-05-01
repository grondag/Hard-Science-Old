package grondag.adversity.niceblock.texture;

import java.util.ArrayList;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.texture.TextureProvider.Texture;
import net.minecraft.util.BlockRenderLayer;

public class TextureProviders
{
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

    public static final ArrayList<TextureProvider> ALL_TEXTURE_PROVIDERS = new ArrayList<TextureProvider>();

    public static final TextureProvider BLOCK_INDIVIDUAL = new TextureProvider();
    
    public static final Texture TEX_BLOCK_COBBLE;
    public static final Texture TEX_BLOCK_RAW_FLEXSTONE;
    public static final Texture TEX_BLOCK_RAW_DURASTONE;
    public static final Texture TEX_BLOCK_COLORED_STONE;
    
    
    public static final TextureProvider BIG_TEX = new TextureProvider();
    
    public static final Texture TEX_BT_WEATHERED_STONE;
    public static final Texture TEX_BT_LAVA;
    public static final Texture TEX_BT_BASALT_COOL;
    public static final Texture TEX_BT_BASALT_COOLING;
    public static final Texture TEX_BT_BASALT_WARM;
    public static final Texture TEX_BT_BASALT_HOT;
    public static final Texture TEX_BT_BASALT_VERY_HOT;
    public static final Texture TEX_BT_BASALT_CUT;
    
    public static final TextureProvider BORDERS = new TextureProvider();
    
    public static final Texture TEX_BORDER_TEST;
    
    public static final TextureProvider MASONRY = new TextureProvider();
    
    public static final Texture TEX_MASONRY_TEST;

    static
    {
        ALL_TEXTURE_PROVIDERS.add(BLOCK_INDIVIDUAL);
        TEX_BLOCK_COBBLE = BLOCK_INDIVIDUAL.addTexture("cobble", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
        TEX_BLOCK_RAW_FLEXSTONE = BLOCK_INDIVIDUAL.addTexture("raw_flexstone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
        TEX_BLOCK_RAW_DURASTONE = BLOCK_INDIVIDUAL.addTexture("raw_durastone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
        TEX_BLOCK_COLORED_STONE = BLOCK_INDIVIDUAL.addTexture("colored_stone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
        
        
        ALL_TEXTURE_PROVIDERS.add(BIG_TEX);
        TEX_BT_WEATHERED_STONE = BIG_TEX.addTexture("weathered_smooth_stone", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_SHADED_ONLY, SOLID_ONLY);
        TEX_BT_LAVA = BIG_TEX.addTexture("lava", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        TEX_BT_BASALT_COOL = BIG_TEX.addTexture("basalt_cool", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        TEX_BT_BASALT_COOLING = BIG_TEX.addTexture("basalt_cooling", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        TEX_BT_BASALT_WARM = BIG_TEX.addTexture("basalt_warm", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        TEX_BT_BASALT_HOT = BIG_TEX.addTexture("basalt_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        TEX_BT_BASALT_VERY_HOT = BIG_TEX.addTexture("basalt_very_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        TEX_BT_BASALT_CUT = BIG_TEX.addTexture("basalt_cut", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
        
        
        ALL_TEXTURE_PROVIDERS.add(BORDERS);
        TEX_BORDER_TEST = BORDERS.addTexture("bordertest", 1, TextureScale.SINGLE, TextureLayout.BORDER_13, false, LIGHTING_SHADED_ONLY, TRANS_ONLY);

        ALL_TEXTURE_PROVIDERS.add(MASONRY);
        TEX_MASONRY_TEST = MASONRY.addTexture("masonrytest", 1, TextureScale.SINGLE, TextureLayout.MASONRY_5, false, LIGHTING_SHADED_ONLY, CUTOUT_M_ONLY);

    }
}
