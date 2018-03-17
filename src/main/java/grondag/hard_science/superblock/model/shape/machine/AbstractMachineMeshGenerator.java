package grondag.hard_science.superblock.model.shape.machine;

import grondag.exotic_matter.render.Surface;
import grondag.hard_science.superblock.model.shape.ShapeMeshGenerator;
import grondag.hard_science.superblock.model.state.StateFormat;

public abstract class AbstractMachineMeshGenerator extends ShapeMeshGenerator
{

    protected AbstractMachineMeshGenerator(int stateFlags, Surface... surfaces)
    {
        super(StateFormat.BLOCK, stateFlags, surfaces);
    }

}
