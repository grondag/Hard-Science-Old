package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.Rotation;
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
        result.lightingMode = this.lightingMode;
        
        EnumFacing face = inputQuad.getFace() == null ? EnumFacing.UP : inputQuad.getFace();
        
        // represents position within a 32x32x32 volume, with x, y representing position on the surface
        // qnd z representing depth into the surface
        Vec3i surfaceVec = getFacePerspective(this.pos, face, this.texture.textureScale);

        int key = Math.abs(MathHelper.hash((surfaceVec.getZ() << 4) | this.species));

        
        // Determine what type of randomizations to apply so that we have a different
        // appearance based on depth and species.
        // If we are applying a single texture, then we alternate by translating, flipping and rotating the texture.
        //
        // If the texture has alternates, then won't don't translate because then we'd need a way to know the 
        // texture version of adjacent volumes.  This would be possible if we got a reference to the 
        // alternator array instead of the alternator result, but it would needlessly complex.
        // 
        // And if the texture has alternates, we also vary the texture selection and, if supported, 
        // the rotation within the plane, to provide more variation within the same surface.
        
        
        if(this.texture.textureVersionCount == 1)
        {
            // single texture, so do rotation, uv flip and offset
            result.textureSprite = this.texture.getTextureSprite(this.blockVersion);
            
            result.useSimpleRotation = true;
            result.rotation = Rotation.values()[key & 3];

            surfaceVec = rotateFacePerspective(surfaceVec, result.rotation, this.texture.textureScale);
            
            int xOffset = (key * 11) & this.texture.textureScale.sliceCountMask; 
            int yOffset = (key * 7) & this.texture.textureScale.sliceCountMask; 
            
            int newX = (surfaceVec.getX() + xOffset) & this.texture.textureScale.sliceCountMask;
            int newY = (surfaceVec.getY() + yOffset) & this.texture.textureScale.sliceCountMask;
            surfaceVec = new Vec3i(newX, newY, surfaceVec.getZ());
            
            boolean flipU = (key & 4) == 0;
            boolean flipV = (key & 8) == 0;

            float sliceIncrement = this.texture.textureScale.sliceIncrement;
            
            int x = flipU ? this.texture.textureScale.sliceCount - surfaceVec.getX() : surfaceVec.getX();
            int y = flipV ? this.texture.textureScale.sliceCount - surfaceVec.getY() : surfaceVec.getY();
            
            result.minU = x * sliceIncrement;
            result.maxU = result.minU + (flipU ? -sliceIncrement : sliceIncrement);

            
            result.minV = y * sliceIncrement;
            result.maxV = result.minV + (flipV ? -sliceIncrement : sliceIncrement);
            
        }
        else
        {
            // multiple texture versions, so do rotation and alternation
            result.textureSprite = this.texture.getTextureSprite((this.blockVersion + (key >> 2)) & this.texture.textureVersionMask);
            
            result.useSimpleRotation = true;
            result.rotation = Rotation.values()[(this.texture.allowRotation ? key + this.rotation.ordinal() : key) & 3];

            surfaceVec = rotateFacePerspective(surfaceVec, result.rotation, this.texture.textureScale);
            
            float sliceIncrement = this.texture.textureScale.sliceIncrement;
            
            result.minU = surfaceVec.getX() * sliceIncrement;
            result.maxU = result.minU + sliceIncrement;

            
            result.minV = surfaceVec.getY() * sliceIncrement;
            result.maxV = result.minV + sliceIncrement;
        }

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
            return new Vec3i(sliceCountMask - z, sliceCountMask - y, -vec.getX());
        
        case WEST:
            return new Vec3i(z, sliceCountMask - y, vec.getX());
        
        case NORTH:
            return new Vec3i(sliceCountMask - x, sliceCountMask - y, vec.getZ());
        
        case SOUTH:
            return new Vec3i(x, sliceCountMask - y, -vec.getZ());
        
        case DOWN:
            return new Vec3i(sliceCountMask - x, z, vec.getY());

        case UP:
        default:
            return new Vec3i(x, z, -vec.getY());
        }
    }
    
    /** 
     * Rotates given surface vector around the center of the texture by the given degree.
     * 
     */
    private static Vec3i rotateFacePerspective(Vec3i vec, Rotation rotation, TextureScale scale)
    {
        switch(rotation)
        {
        case ROTATE_90:
            return new Vec3i(vec.getY(), scale.sliceCountMask - vec.getX(), vec.getZ());

        case ROTATE_180:
            return new Vec3i(scale.sliceCountMask - vec.getX(), scale.sliceCountMask - vec.getY(), vec.getZ());
            
        case ROTATE_270:
            return new Vec3i(scale.sliceCountMask - vec.getY(), vec.getX(), vec.getZ());

        case ROTATE_NONE:
        default:
            return vec;
        
        }
    }
    public static QuadPainter makeQuadPainter(ModelState modelState, int painterIndex)
    {
        return new CubicQuadPainterBigTex(modelState, painterIndex);
    }
}
