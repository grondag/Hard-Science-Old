package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class NormalTestController extends ModelController
{
    protected NormalTestController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, boolean isShaded,
            boolean useRotatedTexturesAsAlternates)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, useRotatedTexturesAsAlternates);
    }

    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 0;
    }

    @Override
    public int getShapeCount()
    {
        return 1;
    }

}
