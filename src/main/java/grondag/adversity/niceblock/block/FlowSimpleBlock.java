package grondag.adversity.niceblock.block;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;

public class FlowSimpleBlock extends NiceBlock implements IFlowBlock
{

    public FlowSimpleBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }

    @Override
    public boolean isFiller()
    {
        return false;
    }

}
