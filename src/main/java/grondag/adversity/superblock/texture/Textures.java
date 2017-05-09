package grondag.adversity.superblock.texture;


import grondag.adversity.library.model.quadfactory.LightingMode;
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
      
    public static final TexturePallette BLOCK_COBBLE = ALL_TEXTURES.addTexturePallette("cobble", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final TexturePallette BLOCK_RAW_FLEXSTONE = ALL_TEXTURES.addTexturePallette("raw_flexstone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final TexturePallette BLOCK_RAW_DURASTONE = ALL_TEXTURES.addTexturePallette("raw_durastone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final TexturePallette BLOCK_COLORED_STONE = ALL_TEXTURES.addTexturePallette("colored_stone", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    
    
    public static final TexturePallette BIGTEX_WEATHERED_STONE = ALL_TEXTURES.addTexturePallette("weathered_smooth_stone", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_SHADED_ONLY, SOLID_ONLY);
    public static final TexturePallette BIGTEX_LAVA = ALL_TEXTURES.addTexturePallette("lava", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final TexturePallette BIGTEX_BASALT_COOL = ALL_TEXTURES.addTexturePallette("basalt_cool", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final TexturePallette BIGTEX_BASALT_COOLING = ALL_TEXTURES.addTexturePallette("basalt_cooling", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final TexturePallette BIGTEX_BASALT_WARM = ALL_TEXTURES.addTexturePallette("basalt_warm", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final TexturePallette BIGTEX_BASALT_HOT = ALL_TEXTURES.addTexturePallette("basalt_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final TexturePallette BIGTEX_BASALT_VERY_HOT = ALL_TEXTURES.addTexturePallette("basalt_very_hot", 1, TextureScale.MEDIUM, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    public static final TexturePallette BIGTEX_BASALT_CUT = ALL_TEXTURES.addTexturePallette("basalt_cut", 1, TextureScale.LARGE, TextureLayout.BIGTEX, false, LIGHTING_BOTH, SOLID_AND_TRANS);
    
    
    public static final TexturePallette BORDER_TEST = ALL_TEXTURES.addTexturePallette("bordertest", 1, TextureScale.SINGLE, TextureLayout.BORDER_13, false, LIGHTING_SHADED_ONLY, TRANS_ONLY);

    public static final TexturePallette MASONRY_TEST = ALL_TEXTURES.addTexturePallette("masonrytest", 1, TextureScale.SINGLE, TextureLayout.MASONRY_5, false, LIGHTING_SHADED_ONLY, CUTOUT_M_ONLY);

    
}
