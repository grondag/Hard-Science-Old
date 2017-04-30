package grondag.adversity.niceblock.modelstate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.model.CSGModelFactory;
import grondag.adversity.niceblock.model.FlowModelFactory;
import grondag.adversity.niceblock.model.HeightModelFactory;
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
        FLOWING_TERRAIN.collisionHandler = FlowModelFactory.makeCollisionHandler();
        ICOSAHEDRON.collisionHandler = CSGModelFactory.makeCollisionHandler();
        HEIGHT.collisionHandler = HeightModelFactory.makeCollisionHandler();
        
        COLUMN_SQUARE.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.AXIS).add(ModelStateComponents.CORNER_JOIN).build();
        
        FLOWING_TERRAIN.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.FLOW_JOIN).build();
        
        HEIGHT.stateComponents = new ImmutableList.Builder<ModelStateComponent<?, ?>>()
                .add(ModelStateComponents.SPECIES_16).build();
    }
    
    private AbstractCollisionHandler collisionHandler = null;
    
    private List<ModelFactory<?>> models = new ArrayList<ModelFactory<?>>();
    
    /** Components that MUST be included for models with this shape because they determine topology */
    private List<ModelStateComponent<?, ?>> stateComponents = Collections.emptyList();
    
    /**
     * CAN BE NULL! If non-null, blocks with this shape require special collision handling, typically because it is not a standard cube shape. 
     */
    public AbstractCollisionHandler collisionHandler()
    {
        return collisionHandler;
    }
    
    public void addModel(ModelFactory<?> model)
    {
        this.models.add(model);
    }
    
    public List<ModelFactory<?>> models()
    {
        return Collections.unmodifiableList(this.models);
    }
    
    public List<ModelStateComponent<?, ?>> components()
    {
        return this.stateComponents;
    }
}
