package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.Output;
import grondag.adversity.library.Useful;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureScale;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class CubicQuadPainterBigTex extends CubicQuadPainter
{

    private final Vec3i pos;
    private final int species;
    
    protected CubicQuadPainterBigTex(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
        this.pos = new Vec3i(modelState.getPosX(), modelState.getPosY(), modelState.getPosZ());
        this.species = modelState.getSpecies();
    }

    @Override
    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        RawQuad result = inputQuad.clone();
        
        result.recolor(this.colorMap.getColor(this.lightingMode == LightingMode.FULLBRIGHT ? EnumColorMap.LAMP : EnumColorMap.BASE));
        result.textureSprite = this.texture.getTextureSprite(this.blockVersion);
        result.lightingMode = this.lightingMode;
        
        EnumFacing face = inputQuad.getFace() == null ? EnumFacing.UP : inputQuad.getFace();
        
        // represents position within a 32x32x32 volume, with x, y representing position on the surface
        // qnd z representing depth into the surface
        Vec3i surfaceVec = getFacePerspective(this.pos, face, this.texture.textureScale);
       
        Output.getLog().info("scale=" + this.texture.textureScale + "  face=" + face + "  Input vec =" + this.pos.toString() + "  surfaceVec=" + surfaceVec.toString());
        
        float sliceIncrement = this.texture.textureScale.sliceIncrement;
        
        result.minU = surfaceVec.getX() * sliceIncrement;
        result.maxU = result.minU + sliceIncrement;

        result.minV = surfaceVec.getY() * sliceIncrement;
        result.maxV = result.minV + sliceIncrement;

//        if(this.isRotationEnabled)
//        {
//            result.rotation = this.rotation;
//        }
        outputList.add(result);    
    }

    /** 
     * Transform input vector so that x & y correspond with u / v on the given face, with u,v origin at upper left
     * and z is depth, where positive values represent distance into the face (away from viewer).
     * 
     * Coordinates are first masked to the scale of the texture being used and when we reverse an axis, 
     * we use the texture's sliceMask as the basis so that we remain within the frame of the
     * texture scale we are using.  
     */

    private static Vec3i getFacePerspective(Vec3i vec, EnumFacing face, TextureScale scale)
    {
        int sliceCountMask = scale.sliceCountMask;
        int x = vec.getX() & sliceCountMask;
        int y = vec.getY() & sliceCountMask;
        int z = vec.getZ() & sliceCountMask;
        
        switch(face)
        {
        case EAST:
            return new Vec3i(sliceCountMask - z, sliceCountMask - y, sliceCountMask - x);
        
        case WEST:
            return new Vec3i(z, sliceCountMask - y, x);
        
        case NORTH:
            return new Vec3i(sliceCountMask - x, sliceCountMask - y, z);
        
        case SOUTH:
            return new Vec3i(x, sliceCountMask - y, sliceCountMask - z);
        
        case DOWN:
            return new Vec3i(sliceCountMask - x, z, y);

        case UP:
        default:
            return new Vec3i(x, z, sliceCountMask - y);
        }
    }
    public static QuadPainter makeQuadPainter(ModelState modelState, int painterIndex)
    {
        return new CubicQuadPainterBigTex(modelState, painterIndex);
    }
}
