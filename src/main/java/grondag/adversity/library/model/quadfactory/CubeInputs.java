package grondag.adversity.library.model.quadfactory;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Rotation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class CubeInputs{
    public double u0;
    public double v0;
    public double u1;
    public double v1;
    public TextureAtlasSprite textureSprite;
    public int color = 0xFFFFFFFF;
    public Rotation textureRotation = Rotation.ROTATE_NONE;
    public boolean rotateBottom = false;
    public boolean isOverlay = false;
    public boolean isItem = false;
    public boolean isShaded = true;

    public CubeInputs()
    {
        //NOOP
    }
    public CubeInputs(int color, Rotation textureRotation, TextureAtlasSprite textureSprite, boolean flipU, boolean flipV, boolean isOverlay, boolean isItem)
    {
        this.color = color;
        this.textureRotation = textureRotation;
        this.textureSprite = textureSprite;
        this.isOverlay = isOverlay;
        this.isItem = isItem;
        this.u0 = flipU ? 16 : 0;
        this.v0 = flipV ? 16 : 0;
        this.u1 = flipU ? 0 : 16;
        this.v1 = flipV ? 0 : 16;
        this.rotateBottom = true;
    }

    public List<BakedQuad> makeFace(EnumFacing side){

        RawQuad qi = new RawQuad();
        qi.color = this.color;
        qi.rotation = (rotateBottom && side == EnumFacing.DOWN) ? this.textureRotation.clockwise().clockwise() : this.textureRotation;
        qi.textureSprite = this.textureSprite;

        double minBound = this.isOverlay ? -0.0002 : 0.0;
        double maxBound = this.isOverlay ? 1.0002 : 1.0;
        qi.side = side;

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();

        switch(side)
        {
        case UP:
            qi.vertices[0] = new Vertex(minBound, maxBound, minBound, u0, v0, this.color);
            qi.vertices[1] = new Vertex(minBound, maxBound, maxBound, u0, v1, this.color);
            qi.vertices[2] = new Vertex(maxBound, maxBound, maxBound, u1, v1, this.color);
            qi.vertices[3] = new Vertex(maxBound, maxBound, minBound, u1, v0, this.color);
            break;

        case DOWN:     
            qi.vertices[0] = new Vertex(maxBound, minBound, maxBound, u0, v1, this.color);
            qi.vertices[1] = new Vertex(minBound, minBound, maxBound, u1, v1, this.color); 
            qi.vertices[2] = new Vertex(minBound, minBound, minBound, u1, v0, this.color); 
            qi.vertices[3] = new Vertex(maxBound, minBound, minBound, u0, v0, this.color);
            break;

        case WEST:
            qi.vertices[0] = new Vertex(minBound, minBound, minBound, u0, v1, this.color);
            qi.vertices[1] = new Vertex(minBound, minBound, maxBound, u1, v1, this.color);
            qi.vertices[2] = new Vertex(minBound, maxBound, maxBound, u1, v0, this.color);
            qi.vertices[3] = new Vertex(minBound, maxBound, minBound, u0, v0, this.color);
            break;

        case EAST:
            qi.vertices[0] = new Vertex(maxBound, minBound, minBound, u1, v1, this.color);
            qi.vertices[1] = new Vertex(maxBound, maxBound, minBound, u1, v0, this.color);
            qi.vertices[2] = new Vertex(maxBound, maxBound, maxBound, u0, v0, this.color);
            qi.vertices[3] = new Vertex(maxBound, minBound, maxBound, u0, v1, this.color);
            break;

        case NORTH:
            qi.vertices[0] = new Vertex(minBound, minBound, minBound, u1, v1, this.color);
            qi.vertices[1] = new Vertex(minBound, maxBound, minBound, u1, v0, this.color);
            qi.vertices[2] = new Vertex(maxBound, maxBound, minBound, u0, v0, this.color);
            qi.vertices[3] = new Vertex(maxBound, minBound, minBound, u0, v1, this.color);
            break;

        case SOUTH:
            qi.vertices[0] = new Vertex(minBound, minBound, maxBound, u0, v1, this.color);
            qi.vertices[1] = new Vertex(maxBound, minBound, maxBound, u1, v1, this.color);
            qi.vertices[2] = new Vertex(maxBound, maxBound, maxBound, u1, v0, this.color);
            qi.vertices[3] = new Vertex(minBound, maxBound, maxBound, u0, v0, this.color);
            break;
        }


        builder.add(qi.createNormalQuad()).build();

        return builder.build();
    }
}