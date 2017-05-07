package grondag.adversity.superblock.model.shape;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;

@SuppressWarnings("unused")
public abstract class ShapeMeshFactory
{
    /** used by ModelState to know why type of state representation is needed by this shape */
    public final StateFormat stateFormat;
    
    /** bits flags used by ModelState to know which optional state elements are needed by this shape */
    public int stateFlags;
    
    /** Surfaces that compose the model. */
    public List<Surface> surfaces;
    
    public abstract Collection<RawQuad> getShapeQuads(ModelState modelState);
    
    // only necessary to override these two if something other than standard cube
    
    /**
     * CAN BE NULL! If non-null, blocks with this shape require special collision handling, typically because it is not a standard cube shape. 
     */
    public AbstractCollisionHandler getCollisionHandler() { return null; };
    public long getCollisionKeyFromModelState(ModelState modelState) { return 0; };
    
    protected ShapeMeshFactory(StateFormat stateFormat, int stateFlags, Surface... surfaces)
    {
        this.stateFormat = stateFormat;
        this.stateFlags = stateFlags;
        this.surfaces = new ImmutableList.Builder<Surface>()
                .add(surfaces).build();
    }
}
