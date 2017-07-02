package grondag.hard_science.superblock.model.painter;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.TextureRotationType;
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
     
        quad.useVertexUVRotation = true;

        Vec3i surfaceVec = getFacePerspective(this.pos, quad.face, this.texture.textureScale);
        
                
        
        if(this.texture.textureVersionCount == 1)
        {
            // no alternates, so do uv flip and offset and rotation based on depth & species only
            int depthAndSpeciesHash = quad.surfaceInstance.ignoreDepthForRandomization ? 0 : MathHelper.hash(surfaceVec.getZ() | (this.species << 8));
            
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
            int depthHash = quad.surfaceInstance.ignoreDepthForRandomization ? 0 : MathHelper.hash(surfaceVec.getZ());

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
}
