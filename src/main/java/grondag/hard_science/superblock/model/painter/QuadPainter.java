package grondag.hard_science.superblock.model.painter;

import java.util.List;

import grondag.exotic_matter.varia.Color;
import grondag.hard_science.library.render.QuadHelper;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.render.Vertex;
import grondag.hard_science.superblock.color.ColorMap;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.RenderPass;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.pipeline.LightUtil;

public abstract class QuadPainter
{
    /** color map for this surface */
    protected final ColorMap myColorMap;
    protected final RenderPass renderPass;
    
    /** 
     * Color map for lamp surface - used to render lamp gradients
     * Only populated for BOTTOM/CUT surfaces
     */
    protected final ColorMap lampColorMap;

    /**
    * Render layer for lamp surface - used to render lamp gradients
    * Only populated for BOTTOM/CUT surfaces
    */
    protected final RenderPass lampRenderPass;
    
    /**
     * True if paint layer is supposed to be rendered at full brightness.
     */
    protected final boolean isFullBrightnessIntended;
    
    protected final TexturePallette texture;
    public final Surface surface;
    public final PaintLayer paintLayer;
    /** Do bitwise OR with color value to get correct alpha for rendering */
    protected final int translucencyArgb;
    
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
        
        this.renderPass = modelState.getRenderPass(paintLayer);
        this.isFullBrightnessIntended = modelState.isFullBrightness(paintLayer);

        if(paintLayer == PaintLayer.BASE || paintLayer == PaintLayer.CUT)
        {
            this.lampColorMap = modelState.getColorMap(PaintLayer.LAMP);
            this.lampRenderPass = modelState.getRenderPass(PaintLayer.LAMP);
        }
        else
        {
            this.lampColorMap = null;
            this.lampRenderPass = null;
        }
        
        TexturePallette tex = modelState.getTexture(paintLayer);
        this.texture = tex == Textures.NONE ? modelState.getTexture(PaintLayer.BASE) : tex;
        this.translucencyArgb = modelState.isTranslucent(paintLayer) ? modelState.getTranslucency().alphaARGB : 0xFF000000;
    }
    
    /** for null painter only */
    private QuadPainter()
    {
        this.myColorMap = null;
        this.lampColorMap = null;
        this.lampRenderPass = null;
        this.renderPass = null;
        this.isFullBrightnessIntended = false;
        this.surface = null;
        this.paintLayer = null;
        this.texture = null;
        this.translucencyArgb = 0;
    }

    /**
     * If isItem = true will bump out quads from block center to provide
     * better depth rendering of layers in tiem rendering.
     */
    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList, boolean isItem)
    {
        if(inputQuad.surfaceInstance.surface() != this.surface) return;
        
        switch(this.paintLayer)
        {
        case BASE:
            if(inputQuad.surfaceInstance.disableBase) return;
            break;
            
        case MIDDLE:
            if(inputQuad.surfaceInstance.disableMiddle) return;
            break;
            
        case OUTER:
            if(inputQuad.surfaceInstance.disableOuter) return;
            break;
            
        case CUT:
        case LAMP:
        default:
            break;
        
        }
    
        RawQuad result = inputQuad.clone();
        result.renderPass = this.renderPass;
        result.isFullBrightness = this.isFullBrightnessIntended;

        recolorQuad(result);
     
        // TODO: Vary color slightly with species, as user-selected option
        
        result = this.paintQuad(result);
        
        if(result != null) 
        {
            if(result.lockUV)
            {
                // if lockUV is on, derive UV coords by projection
                // of vertex coordinates on the plane of the quad's face
                result.assignLockedUVCoordinates();;
            }
   
            if(isItem)
            {
                switch(this.paintLayer)
                {
                case MIDDLE:
                    result.scaleFromBlockCenter(1.01);
                    break;
                    
                case OUTER:
                    result.scaleFromBlockCenter(1.02);
                    break;
                    
                default:
                    break;
                }
            }
            outputList.add(result);
        }
    }
    
    
    private void recolorQuad(RawQuad result)
    {
        int color = this.myColorMap.getColor(this.isFullBrightnessIntended ? EnumColorMap.LAMP : EnumColorMap.BASE);
        
        if(this.renderPass.blockRenderLayer == BlockRenderLayer.TRANSLUCENT)
        {
            color = this.translucencyArgb | (color & 0x00FFFFFF);
        }
        
        if(this.isFullBrightnessIntended)
        {
            // If the surface has a lamp gradient or is otherwise pre-shaded 
            // we don't want to see a gradient when rendering at full brightness
            // so make all vertices white before we recolor.
            result.replaceColor(color);
        }
        
        else if(result.surfaceInstance.isLampGradient && this.lampColorMap != null)
        {
            // if surface has a lamp gradient and rendered with shading, need
            // to replace the colors to form the gradient.
            int shadedColor = QuadHelper.shadeColor(color, (LightUtil.diffuseLight(result.getNormalFace()) + 2) / 3, false);
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
            
            // if needed, change render pass of gradient surface to flat so that it doesn't get darkened by AO
            if(!this.lampRenderPass.isShaded && this.renderPass.isShaded)
            {
                result.renderPass = this.renderPass.flipShading();
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
        public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList, boolean isItem)
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
