package grondag.adversity.superblock.model.shape;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.model.CSGModelFactory;
import grondag.adversity.niceblock.model.FlowModelFactory;
import grondag.adversity.niceblock.model.HeightModelFactory;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

public enum ModelShape
{
    CUBE(StateFormat.BLOCK, STATE_FLAG_NONE),
    COLUMN_SQUARE(StateFormat.BLOCK, STATE_FLAG_NEEDS_CORNER_JOIN),
    HEIGHT(StateFormat.BLOCK, STATE_FLAG_NONE),
    
    BOX(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    SPHERE(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    DOME(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    CYLINDER(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    TUBE(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    CONE(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    PYRAMID(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    TORUS(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    ICOSAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    TETRAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    OCTAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    DODECAHEDRON(StateFormat.MULTIBLOCK, STATE_FLAG_NONE),
    
    FLOWING_TERRAIN(StateFormat.FLOW, STATE_FLAG_NEEDS_POS);
    
    
    private ModelShape(StateFormat stateFormat, int stateFlags)
    {
        this.stateFormat = stateFormat;
        this.stateFlags = stateFlags;
    }
    
    static
    {
        CUBE.surfaces = new ImmutableList.Builder<Surface>()
                .add(CUBE.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
        
        COLUMN_SQUARE.surfaces = new ImmutableList.Builder<Surface>()
                .add(COLUMN_SQUARE.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC))
                .add(COLUMN_SQUARE.makeSurface(SurfaceType.CUT, SurfaceTopology.CUBIC))
                .add(COLUMN_SQUARE.makeSurface(SurfaceType.LAMP, SurfaceTopology.CUBIC)).build();
        COLUMN_SQUARE.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.AXIS)
                .add(ModelStateComponents.CORNER_JOIN).build();
        
        
        FLOWING_TERRAIN.surfaces = new ImmutableList.Builder<Surface>()
                .add(FLOWING_TERRAIN.makeSurface(SurfaceType.MAIN, SurfaceTopology.TILED))
                .add(FLOWING_TERRAIN.makeSurface(SurfaceType.BLOCKFACE, SurfaceTopology.CUBIC)).build();
        FLOWING_TERRAIN.collisionHandler = FlowModelFactory.makeCollisionHandler();
        FLOWING_TERRAIN.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.FLOW_JOIN).build();
        
        SPHERE.surfaces = new ImmutableList.Builder<Surface>()
                .add(SPHERE.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
        SPHERE.collisionHandler = CSGModelFactory.makeCollisionHandler();

        HEIGHT.surfaces = new ImmutableList.Builder<Surface>()
                .add(HEIGHT.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
        HEIGHT.collisionHandler = HeightModelFactory.makeCollisionHandler();
        HEIGHT.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.SPECIES_16).build();
    }
    
    private AbstractCollisionHandler collisionHandler = null;
    
    private List<ModelFactory> models = new ArrayList<ModelFactory>();
    
    private int nextSurfaceOrdinal = 0;
    
    public final StateFormat stateFormat;
    public final int stateFlags;
    
    /** Surfaces that compose the model. */
    private List<Surface> surfaces = Collections.emptyList();

    /** Components that MUST be included for models with this shape because they determine topology */
    private List<ModelStateComponent<?, ?>> stateComponents = Collections.emptyList();
    
    /**
     * CAN BE NULL! If non-null, blocks with this shape require special collision handling, typically because it is not a standard cube shape. 
     */
    public AbstractCollisionHandler collisionHandler()
    {
        return collisionHandler;
    }
    
    public void addModel(ModelFactory model)
    {
        this.models.add(model);
    }
    
    public List<ModelFactory> models()
    {
        return Collections.unmodifiableList(this.models);
    }
    
    public List<Surface> surfaces()
    {
        return this.surfaces;
    }
    
    public List<ModelStateComponent<?, ?>> components()
    {
        return this.stateComponents;
    }
        
    private Surface makeSurface(SurfaceType paintType, SurfaceTopology topology)
    {
        return new Surface(this.nextSurfaceOrdinal++, paintType, topology);
    }
    
    public class Surface
    {
        public final int ordinal;
        public final SurfaceType paintType;
        public final SurfaceTopology topology;
        
        private Surface(int ordinal, SurfaceType paintType, SurfaceTopology topology)
        {
            this.ordinal = ordinal;
            this.paintType = paintType;
            this.topology = topology;
        }
        
        public ModelShape shape()
        {
            return ModelShape.this;
        }
    }
}
