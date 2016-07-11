package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.support.BaseMaterial;

public class FlowFillerBlock extends FlowBlock implements IFlowBlock.IFillerBlock
{
    public FlowFillerBlock(FlowHeightHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }

}