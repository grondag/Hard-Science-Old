package grondag.adversity.niceblock.texture;

import grondag.adversity.library.model.quadfactory.LightingMode;
import net.minecraft.util.BlockRenderLayer;

public class TextureProviders
{

    public static final LightingMode[] LIGHTING_BOTH = {LightingMode.FULLBRIGHT, LightingMode.SHADED};
    public static final LightingMode[] LIGHTING_FULLBRIGHT_ONLY = {LightingMode.FULLBRIGHT};
    public static final LightingMode[] LIGHTING_SHADED_ONLY = {LightingMode.SHADED};
    
    public static final BlockRenderLayer[] SOLID_ONLY = {BlockRenderLayer.SOLID};
    public static final BlockRenderLayer[] CUTOUT_M_ONLY = {BlockRenderLayer.CUTOUT_MIPPED};
    public static final BlockRenderLayer[] CUTOUT_ONLY = {BlockRenderLayer.CUTOUT};
    public static final BlockRenderLayer[] TRANS_ONLY = {BlockRenderLayer.TRANSLUCENT};
    public static final BlockRenderLayer[] SOLID_AND_CUTOUT_M = {BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT_MIPPED};
    public static final BlockRenderLayer[] SOLID_AND_CUTOUT = {BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT};
    public static final BlockRenderLayer[] SOLID_AND_TRANS = {BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT};

    
}
