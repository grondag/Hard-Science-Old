package grondag.adversity.superblock.model.shape;

import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.gui.shape.GuiShape;
import grondag.adversity.gui.shape.GuiSimpleShape;
import grondag.adversity.gui.shape.GuiSquareColumn;
import grondag.adversity.superblock.placement.AdditivePlacementHandler;
import grondag.adversity.superblock.placement.CubicPlacementHandler;
import grondag.adversity.superblock.placement.IPlacementHandler;
import grondag.adversity.superblock.placement.SimplePlacementHandler;
import net.minecraft.client.Minecraft;


public enum ModelShape
{
    CUBE() 
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return CubicPlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return CubeMeshFactory.getShapeMeshFactory(); }
    },

    COLUMN_SQUARE()
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return CubicPlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return SquareColumnMeshFactory.getShapeMeshFactory(); }
        @Override
        public GuiShape guiSettingsControl(Minecraft mc) { return new GuiSquareColumn(mc); }
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
    
    TERRAIN_HEIGHT(false)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return TerrainMeshFactory.getMeshFactory(); }
    },
    
    TERRAIN_FILLER(false)
    {
        @Override
        public IPlacementHandler getPlacementHandler() { return SimplePlacementHandler.INSTANCE; }
        @Override
        public ShapeMeshGenerator meshFactory() { return TerrainMeshFactory.getMeshFactory(); }
    };

    public final boolean isAvailableInGui;
    
    private ModelShape(boolean isAvailableInGui)
    {
        this.isAvailableInGui = isAvailableInGui;
    }
    
    private ModelShape()
    {
        this(true);
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
    public GuiShape guiSettingsControl(Minecraft mc)
    {
        return new GuiSimpleShape(false);
    }
}
