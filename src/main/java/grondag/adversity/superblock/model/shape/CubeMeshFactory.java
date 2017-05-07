package grondag.adversity.superblock.model.shape;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.STATE_FLAG_NONE;

import java.util.Collection;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;

public class CubeMeshFactory extends ShapeMeshFactory
{

    protected CubeMeshFactory()
    {
        super(StateFormat.BLOCK, STATE_FLAG_NONE, new Surface(0, SurfaceType.MAIN, SurfaceTopology.CUBIC));
    }

    @Override
    public Collection<RawQuad> getShapeQuads(ModelState modelState)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
