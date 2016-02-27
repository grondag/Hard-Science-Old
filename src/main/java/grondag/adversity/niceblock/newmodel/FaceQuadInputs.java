package grondag.adversity.niceblock.newmodel;

public class FaceQuadInputs
{
    protected final int textureOffset;
    protected final Rotation rotation;
    protected final boolean flipU;
    protected final boolean flipV;
    
    protected FaceQuadInputs(int textureOffset, Rotation rotation, boolean flipU, boolean flipV)
    {
        this.textureOffset = textureOffset;
        this.rotation = rotation;
        this.flipU = flipU;
        this.flipV = flipV;
    }
}