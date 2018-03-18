package grondag.hard_science.moving;

import static grondag.exotic_matter.model.MetaUsage.SHAPE;
import static grondag.exotic_matter.model.MetaUsage.SPECIES;

import grondag.hard_science.superblock.model.shape.CubeMeshFactory;
import grondag.hard_science.superblock.model.shape.MachineMeshFactory;
import grondag.hard_science.superblock.model.shape.SquareColumnMeshFactory;
import grondag.hard_science.superblock.model.shape.StackedPlatesMeshFactory;
import grondag.hard_science.superblock.model.shape.StairMeshFactory;
import grondag.hard_science.superblock.model.shape.TerrainMeshFactory;
import grondag.hard_science.superblock.model.shape.WedgeMeshFactory;
import grondag.hard_science.superblock.model.state.ModelShape;

public class ModShapes
{

    public static final ModelShape<?> CUBE = ModelShape.create("cube", CubeMeshFactory.class, SPECIES);
    public static final ModelShape<?> COLUMN_SQUARE = ModelShape.create("column_square", SquareColumnMeshFactory.class, SPECIES);
    public static final ModelShape<?> STACKED_PLATES = ModelShape.create("stacked_plats", StackedPlatesMeshFactory.class, SHAPE);
    //TODO: why need both?
    public static final ModelShape<?> TERRAIN_HEIGHT = ModelShape.create("terrain_height", TerrainMeshFactory.class, SHAPE, true);
    public static final ModelShape<?> TERRAIN_FILLER = ModelShape.create("terrain_filler", TerrainMeshFactory.class, SHAPE, false);
    public static final ModelShape<?> WEDGE = ModelShape.create("wedge", WedgeMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> STAIR = ModelShape.create("stair", StairMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> MACHINE = ModelShape.create("machine", MachineMeshFactory.class, SPECIES, false);

}
