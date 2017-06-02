package grondag.adversity.superblock.model.shape;

import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.superblock.placement.AdditivePlacementHandler;
import grondag.adversity.superblock.placement.IPlacementHandler;
import grondag.adversity.superblock.placement.SimplePlacementHandler;
import grondag.adversity.superblock.placement.SpeciesMultiBlockPlacement;


public enum ModelShape
{
    CUBE() 
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SpeciesMultiBlockPlacement.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    COLUMN_SQUARE()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },
    
    STACKED_PLATES()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return AdditivePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return StackedPlatesMeshFactory.getShapeMeshFactory(); }
    },
    
    BOX()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    SPHERE()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    DOME()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    CYLINDER()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    TUBE()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    CONE()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    PYRAMID()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    TORUS()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    ICOSAHEDRON()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    TETRAHEDRON()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    OCTAHEDRON()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    DODECAHEDRON()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },
    
    FLOWING_TERRAIN()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    };

    public static final List<ModelShape> AS_LIST = ImmutableList.copyOf(values());
    
    public abstract ShapeMeshGenerator meshFactory();
    public abstract IPlacementHandler getPlacementHandler();
}
