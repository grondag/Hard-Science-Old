package grondag.adversity.superblock.texture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import grondag.adversity.library.BitPacker;
import grondag.adversity.library.BitPacker.BitElement;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.model.BorderModelFactory;
import grondag.adversity.niceblock.model.MasonryModelFactory;
import net.minecraft.util.BlockRenderLayer;

public class TextureProvider2
{
    
    private static final Texture[] ARRAY_TEMPLATE = new Texture[0];
    
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
        
        public int textureOrdinal() { return ordinal; }
        public String textureBaseName() { return textureBaseName; }
        public TextureScale textureScale() { return textureScale; }
        public TextureLayout textureLayout() { return textureLayout; }
        
        /** 
         * Note that texture state does NOT specify which of multiple texture version should be rendered
         * because this is expected to be driven by world state. It only lets the model know how many are available.
         */
        public int textureVersionCount() { return textureVersionCount; }
    }
}
