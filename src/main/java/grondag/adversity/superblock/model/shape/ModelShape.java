package grondag.adversity.superblock.model.shape;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;


public enum ModelShape
{
    CUBE(CubeMeshFactory::getShapeMeshFactory),
    COLUMN_SQUARE(CubeMeshFactory::getShapeMeshFactory),
    STACKED_PLATES(StackedPlatesMeshFactory::getShapeMeshFactory),
    BOX(CubeMeshFactory::getShapeMeshFactory),
    SPHERE(CubeMeshFactory::getShapeMeshFactory),
    DOME(CubeMeshFactory::getShapeMeshFactory),
    CYLINDER(CubeMeshFactory::getShapeMeshFactory),
    TUBE(CubeMeshFactory::getShapeMeshFactory),
    CONE(CubeMeshFactory::getShapeMeshFactory),
    PYRAMID(CubeMeshFactory::getShapeMeshFactory),
    TORUS(CubeMeshFactory::getShapeMeshFactory),
    ICOSAHEDRON(CubeMeshFactory::getShapeMeshFactory),
    TETRAHEDRON(CubeMeshFactory::getShapeMeshFactory),
    OCTAHEDRON(CubeMeshFactory::getShapeMeshFactory),
    DODECAHEDRON(CubeMeshFactory::getShapeMeshFactory),
    FLOWING_TERRAIN(CubeMeshFactory::getShapeMeshFactory);
    
    // have to do this here or get initialization errors
//    static
//    {
//        CUBE.setFactory(new CubeMeshFactory());
//        COLUMN_SQUARE.setFactory(new CubeMeshFactory());
//        HEIGHT.setFactory(new CubeMeshFactory());
//        BOX.setFactory(new CubeMeshFactory());
//        SPHERE.setFactory(new CubeMeshFactory());
//        DOME.setFactory(new CubeMeshFactory());
//        CYLINDER.setFactory(new CubeMeshFactory());
//        TUBE.setFactory(new CubeMeshFactory());
//        CONE.setFactory(new CubeMeshFactory());
//        PYRAMID.setFactory(new CubeMeshFactory());
//        TORUS.setFactory(new CubeMeshFactory());
//        ICOSAHEDRON.setFactory(new CubeMeshFactory());
//        TETRAHEDRON.setFactory(new CubeMeshFactory());
//        OCTAHEDRON.setFactory(new CubeMeshFactory());
//        DODECAHEDRON.setFactory(new CubeMeshFactory());
//        FLOWING_TERRAIN.setFactory(new CubeMeshFactory());
//    }

    private ModelShape(Supplier<ShapeMeshGenerator> meshFactoryGetter)
    {
        this.meshFactoryGetter = meshFactoryGetter;
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

    private final Supplier<ShapeMeshGenerator> meshFactoryGetter;
    private ShapeMeshGenerator meshFactory;
    
    public static final List<ModelShape> AS_LIST = ImmutableList.copyOf(values());

    
    public ShapeMeshGenerator meshFactory()
    {
        if(this.meshFactory == null) this.meshFactory = this.meshFactoryGetter.get();
        return meshFactory;
    }
}
