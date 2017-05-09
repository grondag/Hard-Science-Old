package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class CubicQuadPainterBigTex extends CubicQuadPainter
{

    protected CubicQuadPainterBigTex(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
    }

    @Override
    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        // TODO Auto-generated method stub
    }

    public static QuadPainter makeQuadPainter(ModelState modelState, int painterIndex)
    {
        return new CubicQuadPainterBigTex(modelState, painterIndex);
    }
}
