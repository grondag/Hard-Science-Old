package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.world.Rotation;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureRotationType;

public abstract class CubicQuadPainter extends QuadPainter
{

    protected final Rotation rotation;
    /** true if textures can be rotated at all - not fixed */
    protected final boolean allowTexRotation;
    protected final int blockVersion;

    protected CubicQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
        
        this.allowTexRotation = this.texture.rotation.rotationType() != TextureRotationType.FIXED;
        
        if(this.texture.rotation.rotationType() == TextureRotationType.RANDOM && modelState.hasTextureRotation())
        {
            int rotationOrdinal = this.texture.rotation.rotation.ordinal();
            rotationOrdinal += modelState.getTextureRotation(this.texture.textureScale).ordinal();
            this.rotation = Rotation.values()[rotationOrdinal & 3];
        }
        else
        {
            this.rotation = this.texture.rotation.rotation;
        }
       
        this.blockVersion = modelState.hasBlockVersions() ? modelState.getBlockVersion(this.texture.textureScale) : 0;
    }
}
