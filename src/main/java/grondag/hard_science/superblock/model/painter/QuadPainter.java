package grondag.hard_science.superblock.model.painter;

import java.util.List;

import grondag.hard_science.library.render.QuadHelper;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.render.Vertex;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.superblock.color.ColorMap;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.RenderMode;
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
    protected final BlockRenderLayer renderLayer;
    
    /** 
     * Color map for lamp surface - used to render lamp gradients
     * Only populated for BASE/CUT surfaces
     */
    protected final ColorMap lampColorMap;

    /**
    * Render layer for lamp surface - used to render lamp gradients
    * Only populated for BASE/CUT surfaces
    */
    protected final BlockRenderLayer lampRenderLayer;
    
    /**
     * True if paint layer is supposed to be rendered at full brightness.
     */
    protected final boolean isFullBrightnessIntended;
    
    /**
     * True if layer is forced to going to be rendered with flat 
     * lighting even if it wasn't intended, due to limitations of MC rendering.
     */
    protected final boolean isFullBrightnessEffective;
    
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
        
        RenderMode renderMode = modelState.getRenderMode(paintLayer);
        this.renderLayer = renderMode.renderLayer;
        this.isFullBrightnessEffective = renderMode.isFullBrightness;
        this.isFullBrightnessIntended = modelState.isFullBrightness(paintLayer);

        if(paintLayer == PaintLayer.BASE || paintLayer == PaintLayer.CUT)
        {
            this.lampColorMap = modelState.getColorMap(PaintLayer.LAMP);
            this.lampRenderLayer = modelState.getRenderMode(PaintLayer.LAMP).renderLayer;
        }
        else
        {
            this.lampColorMap = null;
            this.lampRenderLayer = null;
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
        this.lampRenderLayer = null;
        this.renderLayer = null;
        this.isFullBrightnessEffective = false;
        this.isFullBrightnessIntended = false;
        this.surface = null;
        this.paintLayer = null;
        this.texture = null;
        this.translucencyArgb = 0;
    }

    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        if(inputQuad.surfaceInstance.surface() == this.surface)
        {
            RawQuad result = inputQuad.clone();
            result.renderLayer = this.renderLayer;
            result.isFullBrightness = this.isFullBrightnessEffective;

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
       
                outputList.add(result);
            }
        }
    }
    
    
    private void recolorQuad(RawQuad result)
    {
        int color = this.myColorMap.getColor(this.isFullBrightnessIntended ? EnumColorMap.LAMP : EnumColorMap.BASE);
        
        if(this.renderLayer == BlockRenderLayer.TRANSLUCENT)
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
            
            // if the quad texture is compatible with the 
            // renderlayer being using for lamp
            // then render the quad with the lamp render layer 
            // so that it doesn't get darkened by AO
            if(this.texture.renderIntent.isCompatibleWith(this.lampRenderLayer))
            {
                result.renderLayer = this.lampRenderLayer;
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
