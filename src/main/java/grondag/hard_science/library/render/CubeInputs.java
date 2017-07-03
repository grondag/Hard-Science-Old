package grondag.hard_science.library.render;

import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.model.state.Surface.SurfaceInstance;
import net.minecraft.util.EnumFacing;

public class CubeInputs{
    public double u0;
    public double v0;
    public double u1;
    public double v1;
    public String textureName;
    public int color = 0xFFFFFFFF;
    public Rotation textureRotation = Rotation.ROTATE_NONE;
    public boolean rotateBottom = false;
    public boolean isOverlay = false;
    public boolean isItem = false;
    public LightingMode lightingMode = LightingMode.SHADED;
    public SurfaceInstance surfaceInstance;
    
    public CubeInputs()
    {
        //NOOP
    }
    public CubeInputs(int color, Rotation textureRotation, String textureName, boolean flipU, boolean flipV, boolean isOverlay, boolean isItem)
    {
        this.color = color;
        this.textureRotation = textureRotation;
        this.textureName = textureName;
        this.isOverlay = isOverlay;
        this.isItem = isItem;
        this.u0 = flipU ? 16 : 0;
        this.v0 = flipV ? 16 : 0;
        this.u1 = flipU ? 0 : 16;
        this.v1 = flipV ? 0 : 16;
        this.rotateBottom = true;
    }

    public RawQuad makeRawFace(EnumFacing side){

        RawQuad qi = new RawQuad();
        qi.color = this.color;
        
        qi.lockUV = true;
        qi.lightingMode = this.lightingMode;
        qi.rotation = (rotateBottom && side == EnumFacing.DOWN) ? this.textureRotation.clockwise().clockwise() : this.textureRotation;
        qi.textureName = this.textureName;
        qi.surfaceInstance = this.surfaceInstance;

        double minBound = this.isOverlay ? -0.0002 : 0.0;
        double maxBound = this.isOverlay ? 1.0002 : 1.0;
        qi.setFace(side);

        switch(side)
        {
        case UP:
            qi.setVertex(0, new Vertex(minBound, maxBound, minBound, u0, v0, this.color));
            qi.setVertex(1, new Vertex(minBound, maxBound, maxBound, u0, v1, this.color));
            qi.setVertex(2, new Vertex(maxBound, maxBound, maxBound, u1, v1, this.color));
            qi.setVertex(3, new Vertex(maxBound, maxBound, minBound, u1, v0, this.color));
            break;

        case DOWN:     
            qi.setVertex(0, new Vertex(maxBound, minBound, maxBound, u0, v1, this.color));
            qi.setVertex(1, new Vertex(minBound, minBound, maxBound, u1, v1, this.color)); 
            qi.setVertex(2, new Vertex(minBound, minBound, minBound, u1, v0, this.color)); 
            qi.setVertex(3, new Vertex(maxBound, minBound, minBound, u0, v0, this.color));
            break;

        case WEST:
            qi.setVertex(0, new Vertex(minBound, minBound, minBound, u0, v1, this.color));
            qi.setVertex(1, new Vertex(minBound, minBound, maxBound, u1, v1, this.color));
            qi.setVertex(2, new Vertex(minBound, maxBound, maxBound, u1, v0, this.color));
            qi.setVertex(3, new Vertex(minBound, maxBound, minBound, u0, v0, this.color));
            break;

        case EAST:
            qi.setVertex(0, new Vertex(maxBound, minBound, minBound, u1, v1, this.color));
            qi.setVertex(1, new Vertex(maxBound, maxBound, minBound, u1, v0, this.color));
            qi.setVertex(2, new Vertex(maxBound, maxBound, maxBound, u0, v0, this.color));
            qi.setVertex(3, new Vertex(maxBound, minBound, maxBound, u0, v1, this.color));
            break;

        case NORTH:
            qi.setVertex(0, new Vertex(minBound, minBound, minBound, u1, v1, this.color));
            qi.setVertex(1, new Vertex(minBound, maxBound, minBound, u1, v0, this.color));
            qi.setVertex(2, new Vertex(maxBound, maxBound, minBound, u0, v0, this.color));
            qi.setVertex(3, new Vertex(maxBound, minBound, minBound, u0, v1, this.color));
            break;

        case SOUTH:
            qi.setVertex(0, new Vertex(minBound, minBound, maxBound, u0, v1, this.color));
            qi.setVertex(1, new Vertex(maxBound, minBound, maxBound, u1, v1, this.color));
            qi.setVertex(2, new Vertex(maxBound, maxBound, maxBound, u1, v0, this.color));
            qi.setVertex(3, new Vertex(minBound, maxBound, maxBound, u0, v0, this.color));
            break;
        }
        
        return qi;
    }
}