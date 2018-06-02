package grondag.hard_science.init;

import static grondag.exotic_matter.model.state.MetaUsage.SPECIES;

import grondag.exotic_matter.model.mesh.ModelShape;
import grondag.hard_science.superblock.model.shape.machine.MachineMeshFactory;

public class ModShapes
{

    public static final ModelShape<?> MACHINE = ModelShape.create("machine", MachineMeshFactory.class, SPECIES, false);

}
