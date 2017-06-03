package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.Color;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.library.model.quadfactory.Vertex;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.Translucency;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.pipeline.LightUtil;

public abstract class QuadPainter
{
    /** color map for this surface */
    protected final ColorMap myColorMap;
    /** color map for lamp surface - used to render lamp gradients */
    protected final ColorMap lampColorMap;
    protected final BlockRenderLayer renderLayer;
    protected final LightingMode lightingMode;
    protected final TexturePallette texture;
    public final Surface surface;
    public final PaintLayer paintLayer;
    protected final Translucency translucency;
    
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
        this.myColorMap = modelState.getColorMap(paintLayer);
        this.lampColorMap = modelState.getColorMap(PaintLayer.LAMP);
        this.renderLayer = modelState.getRenderLayer(paintLayer);
        this.lightingMode = modelState.getLightingMode(paintLayer);
        this.texture = modelState.getTexture(paintLayer);
        this.translucency = modelState.getTranslucency();
    }
    
    /** for null painter only */
    private QuadPainter()
    {
        this.myColorMap = null;
        this.lampColorMap = null;
        this.renderLayer = null;
        this.lightingMode = null;
        this.surface = null;
        this.paintLayer = null;
        this.texture = null;
        this.translucency = null;
    }

    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        if(inputQuad.surface == this.surface)
        {
            RawQuad result = inputQuad.clone();
            recolorQuad(result);
            result.lightingMode = this.lightingMode;
            result.renderLayer = this.renderLayer;
            
            //TODO: not working - maybe move to quad bake?
            // bump texture slightly above surface to avoid z-fighting
//            if(this.paintLayer == PaintLayer.OVERLAY)
//            {
//                Vec3d bump = result.computeFaceNormal().scale(0.0005);
//                for(int i = result.getVertexCount() - 1; i >= 0; i--)
//                {
//                    Vertex v = result.getVertex(i);
//                    result.setVertex(i, (Vertex) v.add(bump));
//                }
//            }
            
            result = this.paintQuad(result);
            if(result != null) outputList.add(result);
        }
    }
    
    
    private void recolorQuad(RawQuad result)
    {
        int color = this.myColorMap.getColor(this.lightingMode == LightingMode.FULLBRIGHT ? EnumColorMap.LAMP : EnumColorMap.BASE);
        if(this.renderLayer == BlockRenderLayer.TRANSLUCENT && this.texture.renderLayer == BlockRenderLayer.SOLID)
        {
            color = this.translucency.alphaARGB | (color & 0x00FFFFFF);
        }
        
        if(this.lightingMode == LightingMode.FULLBRIGHT)
        {
            // If the surface has a lamp gradient or is otherwise pre-shaded 
            // we don't want to see a gradient when rendering at full brightness
            // so make all vertices white before we recolor.
            result.replaceColor(color);
        }
        else if(this.surface.isLampGradient)
        {
            // if surface has a lamp gradient and rendered with shading, need
            // to replace the colors to form the gradient.
            int shadedColor = QuadFactory.shadeColor(color, (LightUtil.diffuseLight(result.getNormalFace()) + 2) / 3, false);
            int lampColor = this.lampColorMap.getColor(EnumColorMap.LAMP);
            for(int i = 0; i < result.getVertexCount(); i++)
            {
                Vertex v = result.getVertex(i);
                if(v != null)
                {
                    int vColor = v.color == Color.WHITE ? lampColor : shadedColor;
                    result.setVertex(i, v.withColor(vColor));
                }
            }
        }
        else
        {
            // normal shaded surface - tint existing colors, usually WHITE to start with
            result.multiplyColor(color);
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
