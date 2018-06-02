package grondag.hard_science.superblock.model.shape.machine;

import grondag.exotic_matter.model.mesh.ShapeMeshGenerator;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.state.StateFormat;

public abstract class AbstractMachineMeshGenerator extends ShapeMeshGenerator
{

    protected AbstractMachineMeshGenerator(int stateFlags, Surface... surfaces)
    {
        super(StateFormat.BLOCK, stateFlags, surfaces);
    }

}
