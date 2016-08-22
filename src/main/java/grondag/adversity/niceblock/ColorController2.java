package grondag.adversity.niceblock;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.Rotation;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelController2;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;

import net.minecraft.world.IBlockAccess;

public class ColorController2 extends ModelController2
{
    public static final ModelStateGroup GROUP = 
            ModelStateGroup.find(ModelStateComponents.ROTATION, ModelStateComponents.TEXTURE_4, ModelStateComponents.BLOCK_COLORS);

    public static final ModelStateSet SET = 
            ModelStateSet.find(GROUP);

    public ColorController2(String textureName, int alternateCount, BlockRenderLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.bakedModelFactory = new ColorModelFactory2();
    }
    
    //TODO: remove
    @Override
    public long getDynamicShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 0L;
    }

}
