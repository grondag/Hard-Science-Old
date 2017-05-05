package grondag.adversity.superblock.texture;


import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.superblock.texture.TextureProvider2.Texture;
import net.minecraft.util.BlockRenderLayer;

public class Textures
{
    public static final TextureProvider2 ALL_TEXTURES = new TextureProvider2();
    
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
      
    public static final Texture BLOCK_COBBLE = ALL_TEXTURES.addTexture("cobble", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final Texture BLOCK_RAW_FLEXSTONE = ALL_TEXTURES.addTexture("raw_flexstone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final Texture BLOCK_RAW_DURASTONE = ALL_TEXTURES.addTexture("raw_durastone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final Texture BLOCK_COLORED_STONE = ALL_TEXTURES.addTexture("colored_stone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    
    
    public static final Texture BIGTEX_WEATHERED_STONE = ALL_TEXTURES.addTexture("weathered_smooth_stone", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final Texture BIGTEX_LAVA = ALL_TEXTURES.addTexture("lava", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final Texture BIGTEX_BASALT_COOL = ALL_TEXTURES.addTexture("basalt_cool", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final Texture BIGTEX_BASALT_COOLING = ALL_TEXTURES.addTexture("basalt_cooling", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final Texture BIGTEX_BASALT_WARM = ALL_TEXTURES.addTexture("basalt_warm", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final Texture BIGTEX_BASALT_HOT = ALL_TEXTURES.addTexture("basalt_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final Texture BIGTEX_BASALT_VERY_HOT = ALL_TEXTURES.addTexture("basalt_very_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final Texture BIGTEX_BASALT_CUT = ALL_TEXTURES.addTexture("basalt_cut", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    
    
    public static final Texture BORDER_TEST = ALL_TEXTURES.addTexture("bordertest", 1, TextureScale.SINGLE, TextureLayout.BORDER_13, false, LIGHTING_SHADED_ONLY, TRANS_ONLY);

    public static final Texture MASONRY_TEST = ALL_TEXTURES.addTexture("masonrytest", 1, TextureScale.SINGLE, TextureLayout.MASONRY_5, false, LIGHTING_SHADED_ONLY, CUTOUT_M_ONLY);

    
}
