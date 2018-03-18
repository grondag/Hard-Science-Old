package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.ITexturePalette;
import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.model.TextureScale;
import grondag.exotic_matter.render.Surface;

public class QuadPainterFactory
{
    public static QuadPainter getPainterForSurface(ISuperModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        ITexturePalette texture = modelState.getTexture(paintLayer);
        
        switch(surface.topology)
        {
        case CYLINDRICAL:
            switch(texture.textureLayout())
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
            switch(texture.textureLayout())
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
            switch(texture.textureLayout())
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
            switch(texture.textureLayout())
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return(texture.textureScale() == TextureScale.SINGLE)
                        ? new CubicQuadPainterTiles(modelState, surface, paintLayer)
                        : new CubicQuadPainterBigTex(modelState, surface, paintLayer);
                
            case BORDER_13:
                 return new CubicQuadPainterBorders(modelState, surface, paintLayer);
                
            case MASONRY_5:
                return new CubicQuadPainterMasonry(modelState, surface, paintLayer);  
                
            default:
                return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
            
            }

        default:
            return QuadPainter.makeNullQuadPainter(modelState, surface, paintLayer);
        }
    }
}
