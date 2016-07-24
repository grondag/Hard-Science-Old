package grondag.adversity.niceblock;

import net.minecraft.util.BlockRenderLayer;

public class CSGController extends ColorController
{

    public CSGController(String textureName, int alternateCount, BlockRenderLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.bakedModelFactory = new CSGModelFactory(this);
    }

}
