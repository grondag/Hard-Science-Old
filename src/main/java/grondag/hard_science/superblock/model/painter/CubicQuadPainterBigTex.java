package grondag.hard_science.superblock.model.painter;

import grondag.hard_science.Log;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.TextureRotationType;
import grondag.hard_science.superblock.texture.TextureScale;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class CubicQuadPainterBigTex extends CubicQuadPainter
{
    private final boolean allowTexRotation;
    
    public CubicQuadPainterBigTex(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
        
        this.allowTexRotation = this.texture.rotation.rotationType() != TextureRotationType.FIXED;
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        // Determine what type of randomizations to apply so that we have a different
        // appearance based on depth and species.
        // If we are applying a single texture, then we alternate by translating, flipping and rotating the texture.
        // In this case, the normal variation logic in superclass does not apply.
        //
        // If the texture has alternates, we simply use the normal alternation/rotation logic in super class.
        // But we won't don't translate because then we'd need a way to know the 
        // texture version of adjacent volumes.  This would be possible if we got a reference to the 
        // alternator array instead of the alternator result, but it would needlessly complex.
        // 
        // If the texture has alternates, we also vary the texture selection and, if supported, 
        // based on depth within the plane, to provide variation between adjacent layers.
        // This depth-based variation can be disabled with a setting in the surface instance.
     
        if(Log.DEBUG_MODE && !quad.lockUV) Log.warn("BigTex cubic quad painter received quad without lockUV semantics.  Not expected");

        Vec3i surfaceVec = CubicQuadPainterBigTex.getSurfaceVector(this.pos, quad.getNominalFace(), this.texture.textureScale);
        
                
        
        if(this.texture.textureVersionCount == 1)
        {
            // no alternates, so do uv flip and offset and rotation based on depth & species only
            
            // abs is necessary so that hash input components combine together properly
            // Small random numbers already have most bits set.
            int depthAndSpeciesHash = quad.surfaceInstance.ignoreDepthForRandomization
                    ? quad.surfaceInstance.textureSalt 
                    : MathHelper.hash(Math.abs(surfaceVec.getZ()) | (this.species << 8) | (quad.surfaceInstance.textureSalt << 12));
            
            // rotation 
            quad.rotation = this.allowTexRotation
                    ? Useful.offsetEnumValue(texture.rotation.rotation, depthAndSpeciesHash & 3)
                    : texture.rotation.rotation;
                    
            surfaceVec = rotateFacePerspective(surfaceVec, quad.rotation, this.texture.textureScale);

            quad.textureName = this.texture.getTextureName(0);
            
            int xOffset = (depthAndSpeciesHash >> 2) & this.texture.textureScale.sliceCountMask; 
            int yOffset = (depthAndSpeciesHash >> 8) & this.texture.textureScale.sliceCountMask; 
            
            int newX = (surfaceVec.getX() + xOffset) & this.texture.textureScale.sliceCountMask;
            int newY = (surfaceVec.getY() + yOffset) & this.texture.textureScale.sliceCountMask;
            surfaceVec = new Vec3i(newX, newY, surfaceVec.getZ());
            
            boolean flipU = this.allowTexRotation && (depthAndSpeciesHash & 256) == 0;
            boolean flipV = this.allowTexRotation && (depthAndSpeciesHash & 512) == 0;

            float sliceIncrement = this.texture.textureScale.sliceIncrement;
            
            int x = flipU ? this.texture.textureScale.sliceCount - surfaceVec.getX() : surfaceVec.getX();
            int y = flipV ? this.texture.textureScale.sliceCount - surfaceVec.getY() : surfaceVec.getY();
            
            quad.minU = x * sliceIncrement;
            quad.maxU = quad.minU + (flipU ? -sliceIncrement : sliceIncrement);

            
            quad.minV = y * sliceIncrement;
            quad.maxV = quad.minV + (flipV ? -sliceIncrement : sliceIncrement);
            
        }
        else
        {
            // multiple texture versions, so do rotation and alternation normally, except add additional variation for depth;
            
         // abs is necessary so that hash input components combine together properly
            // Small random numbers already have most bits set.
            int depthHash = quad.surfaceInstance.ignoreDepthForRandomization && quad.surfaceInstance.textureSalt == 0
                    ? 0 
                    : MathHelper.hash(Math.abs(surfaceVec.getZ()) | (quad.surfaceInstance.textureSalt << 8));

            quad.textureName = this.texture.getTextureName((this.textureVersionForFace(quad.getNominalFace()) + depthHash) & this.texture.textureVersionMask);
            
            quad.rotation = this.allowTexRotation
                    ? Useful.offsetEnumValue(this.textureRotationForFace(quad.getNominalFace()), (depthHash >> 16) & 3)
                    : this.textureRotationForFace(quad.getNominalFace());
                    
            surfaceVec = rotateFacePerspective(surfaceVec, quad.rotation, this.texture.textureScale);

            float sliceIncrement = this.texture.textureScale.sliceIncrement;
            
            quad.minU = surfaceVec.getX() * sliceIncrement;
            quad.maxU = quad.minU + sliceIncrement;

            
            quad.minV = surfaceVec.getY() * sliceIncrement;
            quad.maxV = quad.minV + sliceIncrement;
        }
        return quad;
    }

    /** 
     * Transform input vector so that x & y correspond with u / v on the given face, with u,v origin at upper left
     * and z is depth, where positive values represent distance into the face (away from viewer). <br><br>
     * 
     * Coordinates are first masked to the scale of the texture being used and when we reverse an orthogonalAxis, 
     * we use the texture's sliceMask as the basis so that we remain within the frame of the
     * texture scale we are using.  <br><br>
     * 
     * Note that the x, y components are for determining min/max UV values. 
     * They should NOT be used to set vertex UV coordinates directly.
     * All bigtex models should have lockUV = true, which means that 
     * uv coordinates will be derived at time of quad bake by projecting each
     * vertex onto the plane of the quad's nominal face. 
     * Setting UV coordinates on a quad with lockUV=true has no effect.
     */
    protected static Vec3i getSurfaceVector(Vec3i vec, EnumFacing face, TextureScale scale)
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
            return new Vec3i(x, sliceCountMask - z, vec.getY());
    
        case UP:
        default:
            return new Vec3i(x, z, -vec.getY());
        }
    }
}
