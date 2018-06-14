package grondag.hard_science.init;

import static grondag.exotic_matter.model.state.MetaUsage.SPECIES;

import grondag.exotic_matter.model.mesh.ModelShape;
import grondag.exotic_matter.model.mesh.ModelShapes;
import grondag.hard_science.superblock.model.shape.machine.MachineMeshFactory;

public class ModShapes
{

    public static final ModelShape<?> MACHINE = ModelShapes.create("machine", MachineMeshFactory.class, SPECIES, false);

}
