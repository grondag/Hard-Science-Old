package grondag.adversity.superblock.model.painter;

import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.SurfaceType;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureScale;
import grondag.adversity.superblock.texture.TexturePalletteRegistry.TexturePallette;

public class QuadPainterFactory
{
    public static QuadPainter getPainterForSurface(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        TexturePallette texture = modelState.getTexture(paintLayer);
        
        switch(surface.topology)
        {
        case CYLINDRICAL:
            switch(texture.textureLayout)
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return new SurfaceQuadPainterCylinder(modelState, surface, paintLayer);
                
            case BORDER_13:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
                
            case MASONRY_5:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
                
            default:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
            }
            
        case TILED:
            switch(texture.textureLayout)
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return new SurfaceQuadPainterTiles(modelState, surface, paintLayer);
                
            case BORDER_13:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
                
            case MASONRY_5:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
                
            default:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
            }
            
        case TOROIDAL:
            switch(texture.textureLayout)
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return new SurfaceQuadPainterTorus(modelState, surface, paintLayer);
                
            case BORDER_13:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
                
            case MASONRY_5:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
                
            default:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
            }

        case CUBIC:
            switch(texture.textureLayout)
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return(texture.textureScale == TextureScale.SINGLE)
                        ? new CubicQuadPainterTiles(modelState, surface, paintLayer)
                        : new CubicQuadPainterBigTex(modelState, surface, paintLayer);
                
            // Borders only apply to main surface
            case BORDER_13:
                 return surface.surfaceType == SurfaceType.MAIN
                 ? new CubicQuadPainterBorders(modelState, surface, paintLayer)
                 : QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);       
                
            // Masonry only applies to main surface 
            case MASONRY_5:
                return surface.surfaceType == SurfaceType.MAIN
                ? new CubicQuadPainterMasonry(modelState, surface, paintLayer)
                : QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);  
                
            default:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
            
            }

        default:
            return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
        }
    }
}
