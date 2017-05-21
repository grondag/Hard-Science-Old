package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.util.BlockRenderLayer;

public abstract class QuadPainter
{
    public abstract void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList);
    
    protected final ColorMap colorMap;
    protected final BlockRenderLayer renderLayer;
    protected final LightingMode lightingMode;
    protected final TexturePallette texture;
    public final Surface surface;
    public final PaintLayer paintLayer;
    
    public QuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        this.surface = surface;
        this.paintLayer = paintLayer;
        this.colorMap = modelState.getColorMap(paintLayer);
        this.renderLayer = modelState.getRenderLayer(paintLayer);
        this.lightingMode = modelState.getLightingMode(paintLayer);
        this.texture = modelState.getTexture(paintLayer);
    }
    
    /** for null painter only */
    private QuadPainter()
    {
        this.colorMap = null;
        this.renderLayer = null;
        this.lightingMode = null;
        this.surface = null;
        this.paintLayer = null;
        this.texture = null;
    }
    
//    @FunctionalInterface
//    public static interface QuadPainterFactory
//    {
//        public QuadPainter makeQuadPainter(Surface surface, PaintLayer paintLayer, ModelState modelState);
//    }
    
    public static QuadPainter makeNullQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        return NullQuadPainter.INSTANCE;
    }
    
    public static class NullQuadPainter extends QuadPainter
    {

        private static final NullQuadPainter INSTANCE = new NullQuadPainter();
        
        private NullQuadPainter()
        {
            super();
        };
        
        @Override
        public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
        {
            // NOOP
        }
        
    }
}
