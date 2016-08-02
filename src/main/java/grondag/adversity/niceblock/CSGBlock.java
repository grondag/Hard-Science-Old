package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;


public class CSGBlock extends NiceBlockPlus
{

    public CSGBlock(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }

    //Necessary for correct AO lighting
    @Override
    public boolean isFullCube(IBlockState state)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean needsCustomHighlight()
    {
        return true;
    }

}
