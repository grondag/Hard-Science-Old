package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.library.model.quadfactory.Vertex;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.Vec3d;

public abstract class QuadPainter
{
    
    protected final ColorMap colorMap;
    protected final BlockRenderLayer renderLayer;
    protected final LightingMode lightingMode;
    protected final TexturePallette texture;
    public final Surface surface;
    public final PaintLayer paintLayer;
    
    /**
     * Provided quad is already a clone, and should be
     * modified directly and returned.
     * Return null to exclude quad from output.
     * RenderLayer, lighting mode and color will already be set.
     * @return 
     */
    protected abstract RawQuad paintQuad(RawQuad quad);
    
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

    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        if(inputQuad.surface == this.surface)
        {
            RawQuad result = inputQuad.clone();
            int color = this.colorMap.getColor(this.lightingMode == LightingMode.FULLBRIGHT ? EnumColorMap.LAMP : EnumColorMap.BASE);
            if(this.renderLayer == BlockRenderLayer.TRANSLUCENT && this.texture.renderLayer == BlockRenderLayer.SOLID)
            {
                //TODO: make % translucency depend on substance
                color = 0x40000000 | (color & 0x00FFFFFF);
            }
            result.recolor(color);
            result.lightingMode = this.lightingMode;
            result.renderLayer = this.renderLayer;
            
            // bump texture slightly above surface to avoid z-fighting
            if(this.paintLayer == PaintLayer.OVERLAY)
            {
                Vec3d bump = result.computeFaceNormal().scale(0.00005);
                for(int i = result.getVertexCount() - 1; i >= 0; i--)
                {
                    Vertex v = result.getVertex(i);
                    result.setVertex(i, (Vertex) v.add(bump));
                }
            }
            
            result = this.paintQuad(result);
            if(result != null) outputList.add(result);
        }
    }
    
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

        @Override
        protected RawQuad paintQuad(RawQuad quad)
        {
            return null;
        }
        
    }
}
