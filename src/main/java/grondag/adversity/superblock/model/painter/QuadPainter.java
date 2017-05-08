package grondag.adversity.superblock.model.painter;

import java.util.Collection;
import java.util.List;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

@FunctionalInterface
public interface QuadPainter
{
    public void addPaintedQuadsToList(ModelState modelState, int painterIndex, Collection<RawQuad> shapeQuads, List<RawQuad> outputQuads);

    public static void nullQuadPainter(ModelState modelState, int painterIndex, Collection<RawQuad> shapeQuads, List<RawQuad> outputQuads)
    {
        // NOOP
    }
}
