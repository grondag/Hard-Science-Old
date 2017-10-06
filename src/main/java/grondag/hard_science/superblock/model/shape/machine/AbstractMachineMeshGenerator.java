package grondag.hard_science.superblock.model.shape.machine;

import grondag.hard_science.superblock.model.shape.ShapeMeshGenerator;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;

public abstract class AbstractMachineMeshGenerator extends ShapeMeshGenerator
{

    protected AbstractMachineMeshGenerator(int stateFlags, Surface... surfaces)
    {
        super(StateFormat.BLOCK, stateFlags, surfaces);
    }

}
