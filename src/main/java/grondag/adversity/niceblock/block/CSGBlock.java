package grondag.adversity.niceblock.block;

import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;


public class CSGBlock extends NiceBlockPlus
{

    public CSGBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }

    //Necessary for correct AO lighting
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return false;
    }

    @Override
    public boolean needsCustomHighlight()
    {
        return true;
    }

}
