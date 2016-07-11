package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.support.BaseMaterial;

public class FlowHeightBlock extends FlowBlock implements IFlowBlock.IHeightBlock
{
    public FlowHeightBlock(FlowHeightHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }

}
