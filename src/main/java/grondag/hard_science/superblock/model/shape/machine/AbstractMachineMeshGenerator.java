package grondag.hard_science.superblock.model.shape.machine;

import grondag.exotic_matter.model.StateFormat;
import grondag.exotic_matter.render.Surface;
import grondag.hard_science.movetogether.ShapeMeshGenerator;

public abstract class AbstractMachineMeshGenerator extends ShapeMeshGenerator
{

    protected AbstractMachineMeshGenerator(int stateFlags, Surface... surfaces)
    {
        super(StateFormat.BLOCK, stateFlags, surfaces);
    }

}
