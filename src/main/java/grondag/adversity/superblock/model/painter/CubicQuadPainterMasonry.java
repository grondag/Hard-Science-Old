package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class CubicQuadPainterMasonry extends CubicQuadPainter
{

    protected CubicQuadPainterMasonry(ModelState modelState, int painterIndex)
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
        return new CubicQuadPainterMasonry(modelState, painterIndex);
    }
}
