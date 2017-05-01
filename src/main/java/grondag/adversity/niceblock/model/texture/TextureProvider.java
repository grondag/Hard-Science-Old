package grondag.adversity.niceblock.model.texture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import grondag.adversity.library.BitPacker;
import grondag.adversity.library.BitPacker.BitElement;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.model.shape.painter.BorderModelFactory;
import grondag.adversity.niceblock.model.shape.painter.MasonryModelFactory;
import net.minecraft.util.BlockRenderLayer;

public class TextureProvider
{
    private static final Texture[] ARRAY_TEMPLATE = new Texture[0];
    
    public static final int MAX_TEXTURES_PER_PROVIDER = 1024;
    
    private static final BitPacker BIT_PACKER = new BitPacker();
    private static final BitElement.IntElement TEXTURE_ORDINAL_BITS = BIT_PACKER.createIntElement(MAX_TEXTURES_PER_PROVIDER);
    private static final BitElement.BooleanElement ROTATION_ENABLED_BITS = BIT_PACKER.createBooleanElement();
    private static final BitElement.EnumElement<LightingMode> LIGHTING_MODE_BITS = BIT_PACKER.createEnumElement(LightingMode.class);
    private static final BitElement.EnumElement<BlockRenderLayer> RENDER_LAYER_BITS = BIT_PACKER.createEnumElement(BlockRenderLayer.class);
    
    public final static int TEXTURE_STATE_BIT_LENGTH = BIT_PACKER.bitLength();
    
    private final ArrayList<Texture> textures = new ArrayList<Texture>();
    
    private int nextOrdinal = 0;
    
    public Texture addTexture(String textureBaseName, int textureVersionCount, TextureScale textureScale, TextureLayout layout, boolean allowRotation, LightingMode[] lightingModes, BlockRenderLayer[] renderLayers)
    {
        Texture result = new Texture(nextOrdinal++, textureBaseName, textureVersionCount, textureScale, layout, allowRotation, lightingModes, renderLayers);
        textures.add(result);
        return result;
    }

    public int size() { return textures.size(); }

    public boolean isEmpty() { return textures.isEmpty(); }

    public boolean contains(Object o) { return textures.contains(o); }
   
    public Iterator<Texture> iterator() { return textures.iterator(); }
   
    public Texture[] toArray() { return textures.toArray(ARRAY_TEMPLATE); }
   
    public Texture get(int index) { return textures.get(index); }
    
    /** 
     * Use this to find the texture state (which also identifies texture) when you have a locator value
     * obtained earlier from TextureState.stateLocator().
     */
    public Texture.TextureState getTextureState(int stateLocator)
    {
        return textures.get(TEXTURE_ORDINAL_BITS.getValue(stateLocator))
                .getTextureState(ROTATION_ENABLED_BITS.getValue(stateLocator), LIGHTING_MODE_BITS.getValue(stateLocator), RENDER_LAYER_BITS.getValue(stateLocator));
    }
    
    /**
     * Identifies all textures needed for texture stitch.
     * Assumes a single texture per model.
     * Override if have something more complicated.
     */
    public void addTexturesForPrestich(List<String> textureList)
    {
        for(Texture t : this.textures)
        {
            t.addTexturesForPrestich(textureList);
        }
    }
    
    public class Texture
    {
        public final String textureBaseName;
        public final int textureVersionCount;
        public final TextureScale textureScale;
        public final TextureLayout textureLayout;
        
        /** Used to limit user selection, is not enforced. */
        public final boolean allowRotation;
        
        /** 
         * Bit at ordinal position indicates if mode is supported.
         * Used to limit user selection, is not enforced.
         */
        
        public final int lightingModeFlags;
        /** 
         * Bit at ordinal position indicates if layer is supported.
         * Used to limit user selection, is not enforced.
         */
        public final int renderLayerFlags;
        
        public final int ordinal;

        private Texture(int ordinal, String textureBaseName, int textureVersionCount, TextureScale textureScale, TextureLayout layout, boolean allowRotation, LightingMode[] lightingModes, BlockRenderLayer[] renderLayers)
        {
            this.ordinal = ordinal;
            this.textureBaseName = textureBaseName;
            this.textureVersionCount = textureVersionCount;
            this.textureScale = textureScale;
            this.textureLayout = layout;
           
            this.allowRotation = allowRotation;
            
            int lightFlags = 0;
            for(LightingMode mode : lightingModes)
            {
                lightFlags |= 1 << mode.ordinal();
            }
            this.lightingModeFlags = lightFlags;
            
            int layerFlags = 0;
            for(BlockRenderLayer layer : renderLayers)
            {
                layerFlags |= 1 << layer.ordinal();
            }
            this.renderLayerFlags = layerFlags;
            
        }
        
        /**
         * Identifies all textures needed for texture stitch.
         * Assumes a single texture per model.
         * Override if have something more complicated.
         */
        private void addTexturesForPrestich(List<String> textureList)
        {
            if(this.textureBaseName == null) return;
            switch(this.textureLayout)
            {
            case BIGTEX:
                for (int i = 0; i < this.textureVersionCount; i++)
                {
                    textureList.add(buildTextureNameBigTex());
                }
                break;
                
            case BORDER_13:
                for(int i = 0; i < this.textureVersionCount; i++)
                {
                    for(int j = 0; j < BorderModelFactory.TEXTURE_COUNT; j++)
                    {
                        textureList.add(buildTextureName_X_8(i * BorderModelFactory.TEXTURE_BLOCK_SIZE + j));
                    }
                }
                break;
                
            case MASONRY_5:
                for(int i = 0; i < this.textureVersionCount; i++)
                {
                    for(int j = 0; j < MasonryModelFactory.TEXTURE_COUNT; j++)
                    {
                        textureList.add(buildTextureName_X_8(i * MasonryModelFactory.TEXTURE_BLOCK_SIZE + j));
                    }
                }
                
            case SPLIT_X_8:
            default:
                for (int i = 0; i < this.textureVersionCount; i++)
                {
                    textureList.add(buildTextureName_X_8(i));
                }
                break;
            }
        }
        
        public String buildTextureName_X_8(int offset)
        {
            return "adversity:blocks/" + textureBaseName + "_" + (offset >> 3) + "_" + (offset & 7);
        }

        public String buildTextureNameBigTex()
        {
            return "adversity:blocks/" + textureBaseName;
        }
        
        /** 
         * Use this version for create a new texture state from UI or from code when you already have the selected texture. 
         * State encapsulates the texture selection also. 
         */
        public TextureState getTextureState(boolean rotationEnabled, LightingMode lightingMode, BlockRenderLayer renderLayer)
        {
            return new TextureState(rotationEnabled, lightingMode, renderLayer);
        }
        
        public class TextureState
        {
            public final boolean rotationEnabled;
            public final LightingMode lightingMode;
            public final BlockRenderLayer renderLayer;
          
            // parent properties
            public int textureOrdinal() { return ordinal; }
            public String textureBaseName() { return textureBaseName; }
            public TextureScale textureScale() { return textureScale; }
            public TextureLayout textureLayout() { return textureLayout; }
            
            /** 
             * Note that texture state does NOT specify which of multiple texture version should be rendered
             * because this is expected to be driven by world state. It only lets the model know how many are available.
             */
            public int textureVersionCount() { return textureVersionCount; }

            private TextureState(boolean rotationEnabled, LightingMode lightingMode, BlockRenderLayer renderLayer)
            {
                this.rotationEnabled = rotationEnabled;
                this.lightingMode = lightingMode;
                this.renderLayer = renderLayer;
            }
            
            /** used by dispatched as default particle texture */
            public String getDefaultParticleTexture() 
            { 
                if(textureBaseName == null) return "";
                
                switch(textureLayout)
                {
                case BIGTEX:
                    return buildTextureNameBigTex();
                case SPLIT_X_8:
                case MASONRY_5:    
                case BORDER_13:
                default:
                    return buildTextureName_X_8(0);
                }
            }
            
            public String buildTextureName()
            {
                if(textureBaseName == null) return "";
                return buildTextureNameBigTex();
            }
            
            public String buildTextureName(int index)
            {
                if(textureBaseName == null) return "";
                return buildTextureName_X_8(index);
                
            }
            
            public String buildTextureName(int version, int index)
            {
                if(textureBaseName == null) return "";
                switch(textureLayout)
                {
                case MASONRY_5:
                    return buildTextureName_X_8(version * MasonryModelFactory.TEXTURE_BLOCK_SIZE + index);
                    
                case BORDER_13:
                    return buildTextureName_X_8(version * BorderModelFactory.TEXTURE_BLOCK_SIZE + index);
                    
                default:
                    return buildTextureName_X_8(index);
                }
            }
            
            /** 
             * Can be used to identify, serialize and reinstantiate this state from the owning provider.
             * Captures the texture (via ordinal) but not the provider because provider should always be known
             * because it is an attribute of the model that will be consuming this texture state.
             * And model is captured in state elsewhere...
             */
            public int stateLocator()
            {
                return (int) (TEXTURE_ORDINAL_BITS.getBits(ordinal)
                        | ROTATION_ENABLED_BITS.getBits(rotationEnabled)
                        | LIGHTING_MODE_BITS.getBits(lightingMode)
                        | RENDER_LAYER_BITS.getBits(renderLayer));
            } 
        }
    }

}
