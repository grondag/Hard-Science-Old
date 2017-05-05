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

public enum ModelShape
{
    CUBE,
    COLUMN_SQUARE,
    FLOWING_TERRAIN,
    ICOSAHEDRON,
    HEIGHT;
    
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
        
        ICOSAHEDRON.surfaces = new ImmutableList.Builder<Surface>()
                .add(ICOSAHEDRON.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
        ICOSAHEDRON.collisionHandler = CSGModelFactory.makeCollisionHandler();

        HEIGHT.surfaces = new ImmutableList.Builder<Surface>()
                .add(HEIGHT.makeSurface(SurfaceType.MAIN, SurfaceTopology.CUBIC)).build();
        HEIGHT.collisionHandler = HeightModelFactory.makeCollisionHandler();
        HEIGHT.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.SPECIES_16).build();
    }
    
    private AbstractCollisionHandler collisionHandler = null;
    
    private List<ModelFactory> models = new ArrayList<ModelFactory>();
    
    private int nextSurfaceOrdinal = 0;
    
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
