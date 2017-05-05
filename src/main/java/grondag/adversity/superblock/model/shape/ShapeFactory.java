package grondag.adversity.superblock.model.shape;

import java.util.Collection;

import grondag.adversity.library.model.quadfactory.RawQuad;

public abstract class ShapeFactory
{
    public abstract Collection<RawQuad> getShapeQuads();
}
