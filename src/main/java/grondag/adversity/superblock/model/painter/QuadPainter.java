package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap;
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
    protected final boolean isRotationEnabled;
    protected final TexturePallette texture;
    public final Surface surface;
    
    protected QuadPainter(ModelState modelState, int painterIndex)
    {
        this.colorMap = modelState.getColorMap(painterIndex);
        this.renderLayer = modelState.getRenderLayer(painterIndex);
        this.lightingMode = modelState.getLightingMode(painterIndex);
        this.isRotationEnabled = modelState.getRotationEnabled(painterIndex);
        this.texture = modelState.getTexture(painterIndex);
        this.surface = modelState.getSurface(painterIndex);
    }
    
    /** for null painter only */
    private QuadPainter()
    {
        this.colorMap = null;
        this.renderLayer = null;
        this.lightingMode = null;
        this.isRotationEnabled = false;
        this.surface = null;
        this.texture = null;
    }
    
    @FunctionalInterface
    public static interface QuadPainterFactory
    {
        public QuadPainter makeQuadPainter(ModelState modelState, int painterIndex);
    }
    
    public static QuadPainter makeNullQuadPainter(ModelState modelState, int painterIndex)
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
