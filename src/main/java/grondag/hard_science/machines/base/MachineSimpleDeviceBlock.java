package grondag.hard_science.machines.base;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;

public abstract class MachineSimpleDeviceBlock extends MachineSimpleBlock
{
    protected MachineSimpleDeviceBlock(String blockName, ModelState defaultModelState)
    {
        super(blockName, defaultModelState);
    }
}
