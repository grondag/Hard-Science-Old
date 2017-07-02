package grondag.adversity.superblock.model.shape;

import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.superblock.model.state.MetaUsage;
import grondag.adversity.superblock.placement.AdditivePlacementHandler;
import grondag.adversity.superblock.placement.CubicPlacementHandler;
import grondag.adversity.superblock.placement.IPlacementHandler;
import grondag.adversity.superblock.placement.SimplePlacementHandler;
import net.minecraft.util.text.translation.I18n;

import static grondag.adversity.superblock.model.state.MetaUsage.*;

public enum ModelShape
{
    CUBE(SPECIES) 
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return CubicPlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    COLUMN_SQUARE(SPECIES)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return CubicPlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return SquareColumnMeshFactory.getShapeMeshFactory(); }
    },
    
    STACKED_PLATES(SHAPE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return AdditivePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return StackedPlatesMeshFactory.getShapeMeshFactory(); }
    },
    
    //TODO: ROUND COLUMN
    //TODO: STAIRS
    //TODO: PIPES
    //TODO: WALL/BARRIER/PANE
    
    // TODO: BOX
    BOX(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: SPHERE/DOME
    SPHERE(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: ROUNDED BOX
    ROUNDED_BOX(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: CYLINDER/TUBE
    CYLINDER(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: ROCK
    TUBE(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: CONE
    CONE(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: PYRAMID
    PYRAMID(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: TORUS
   TORUS(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: ICOSAHEDRON
    ICOSAHEDRON(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },
 
    // TODO: TETRAHEDRON
    TETRAHEDRON(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: OCTAHEDRON
    OCTAHEDRON(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    // TODO: DODECAHEDRON
    DODECAHEDRON(NONE)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },
    
    TERRAIN_HEIGHT(SHAPE, false)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return TerrainMeshFactory.getMeshFactory(); }
    },
    
    TERRAIN_FILLER(SHAPE, false)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return TerrainMeshFactory.getMeshFactory(); }
    },
    
    WEDGE(SPECIES, true)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return CubicPlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return WedgeMeshFactory.getShapeMeshFactory(); }
    };

    public final boolean isAvailableInGui;
    public final MetaUsage metaUsage;
    
    private ModelShape(MetaUsage metaUsage, boolean isAvailableInGui)
    {
        this.metaUsage = metaUsage;
        this.isAvailableInGui = isAvailableInGui;
    }
    
    private ModelShape(MetaUsage metaUsage)
    {
        this(metaUsage, true);
    }
    public static final List<ModelShape> GUI_AVAILABLE_SHAPES;
    
    static
    {
        ImmutableList.Builder<ModelShape> builder = ImmutableList.builder();
        for(ModelShape shape : values())
        {
            if(shape.isAvailableInGui) builder.add(shape);
        }
        GUI_AVAILABLE_SHAPES = builder.build();
    }
    public abstract ShapeMeshGenerator meshFactory();
    public abstract IPlacementHandler getPlacementHandler();
    
    @SuppressWarnings("deprecation")
    public String localizedName()
    {
        return I18n.translateToLocal("shape." + this.name().toLowerCase());
    }
}
