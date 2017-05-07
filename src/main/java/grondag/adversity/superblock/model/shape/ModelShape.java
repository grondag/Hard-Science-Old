package grondag.adversity.superblock.model.shape;

import java.util.List;

import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;

public enum ModelShape
{
    CUBE(new CubeMeshFactory()),
    COLUMN_SQUARE(new CubeMeshFactory()),
    HEIGHT(new CubeMeshFactory()),
    BOX(new CubeMeshFactory()),
    SPHERE(new CubeMeshFactory()),
    DOME(new CubeMeshFactory()),
    CYLINDER(new CubeMeshFactory()),
    TUBE(new CubeMeshFactory()),
    CONE(new CubeMeshFactory()),
    PYRAMID(new CubeMeshFactory()),
    TORUS(new CubeMeshFactory()),
    ICOSAHEDRON(new CubeMeshFactory()),
    TETRAHEDRON(new CubeMeshFactory()),
    OCTAHEDRON(new CubeMeshFactory()),
    DODECAHEDRON(new CubeMeshFactory()),
    FLOWING_TERRAIN(new CubeMeshFactory());

    //    COLUMN_SQUARE(StateFormat.BLOCK, STATE_FLAG_NEEDS_CORNER_JOIN, new CubeMeshFactory()),
    //    HEIGHT(StateFormat.BLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    
    //    BOX(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    SPHERE(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    DOME(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    CYLINDER(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    TUBE(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    CONE(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    PYRAMID(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    TORUS(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    ICOSAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    TETRAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    OCTAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    DODECAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE, new CubeMeshFactory()),
    //    
    //    FLOWING_TERRAIN(StateFormat.FLOW, STATE_FLAG_NEEDS_POS, new CubeMeshFactory());


    private ModelShape(ShapeMeshFactory meshFactory)
    {
        this.meshFactory = meshFactory;
        this.stateFlags = meshFactory.stateFlags;
        this.stateFormat = meshFactory.stateFormat;
        this.surfaces = meshFactory.surfaces;
        this.collisionHandler = meshFactory.getCollisionHandler();
    }

    //    static
    //    {
    //        CUBE.surfaces = new ImmutableList.Builder<Surface>()
    //                .add(CUBE.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
    //        
    //        COLUMN_SQUARE.surfaces = new ImmutableList.Builder<Surface>()
    //                .add(COLUMN_SQUARE.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC))
    //                .add(COLUMN_SQUARE.makeSurface(SurfaceType.CUT, SurfaceTopology.CUBIC))
    //                .add(COLUMN_SQUARE.makeSurface(SurfaceType.LAMP, SurfaceTopology.CUBIC)).build();
    //        
    //        
    //        FLOWING_TERRAIN.surfaces = new ImmutableList.Builder<Surface>()
    //                .add(FLOWING_TERRAIN.makeSurface(SurfaceType.MAIN, SurfaceTopology.TILED))
    //                .add(FLOWING_TERRAIN.makeSurface(SurfaceType.BLOCKFACE, SurfaceTopology.CUBIC)).build();
    //        
    //        SPHERE.surfaces = new ImmutableList.Builder<Surface>()
    //                .add(SPHERE.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
    //
    //        HEIGHT.surfaces = new ImmutableList.Builder<Surface>()
    //                .add(HEIGHT.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
    //    }

    public final ShapeMeshFactory meshFactory;

    // these are reproduced here to avoid an extra hop because will be called from hot inner loops

    /** used by ModelState to know why type of state representation is needed by this shape */
    public final StateFormat stateFormat;

    /** bits flags used by ModelState to know which optional state elements are needed by this shape */
    public final int stateFlags;

    /** Surfaces that compose the model. */
    public final List<Surface> surfaces;
    
    /** may be null! */
    public final AbstractCollisionHandler collisionHandler;
    
    //TODO: remove - is for NiceBlock support, and doesn't fully work
    public AbstractCollisionHandler collisionHandler() { return this.collisionHandler; }
}
