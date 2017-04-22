package grondag.adversity.niceblock.base;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import net.minecraft.util.BlockRenderLayer;

public class ModelAppearanceLists
{
    public static final ModelAppearanceList BLOCK_SOLID;
    
    static
    {
        BLOCK_SOLID = new ModelAppearanceList(LightingMode.SHADED, BlockRenderLayer.SOLID, ModelStateComponents.COLORS_BLOCK);
        BLOCK_SOLID.add(new ModelAppearance("raw_flexstone", 4));
    }
}
