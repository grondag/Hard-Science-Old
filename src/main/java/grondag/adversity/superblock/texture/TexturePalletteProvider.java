package grondag.adversity.superblock.texture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.model.BorderModelFactory;
import grondag.adversity.niceblock.model.MasonryModelFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;

public class TexturePalletteProvider implements Iterable<TexturePalletteProvider.TexturePallette>
{
    
    private static final TexturePallette[] ARRAY_TEMPLATE = new TexturePallette[0];
    
    private final ArrayList<TexturePallette> texturePallettes = new ArrayList<TexturePallette>();
    
    private int nextOrdinal = 0;
    
    public TexturePallette addTexturePallette(String textureBaseName, int textureVersionCount, TextureScale textureScale, TextureLayout layout, boolean allowRotation, LightingMode[] lightingModes, BlockRenderLayer renderLayer, TextureGroup textureGroup)
    {
        TexturePallette result = new TexturePallette(nextOrdinal++, textureBaseName, textureVersionCount, textureScale, layout, allowRotation, lightingModes, renderLayer, textureGroup);
        texturePallettes.add(result);
        return result;
    }
    
    public TexturePallette addZoomedPallete(TexturePallette source)
    {
        TexturePallette result = new TexturePallette(nextOrdinal++, source.textureBaseName, source.textureVersionCount, source.textureScale.zoom(), source.textureLayout, source.allowRotation, 
                    source.lightingModeFlags, source.renderLayer, source.textureGroup);
        texturePallettes.add(result);
        return result;
    }

    public int size() { return texturePallettes.size(); }

    public boolean isEmpty() { return texturePallettes.isEmpty(); }

    public boolean contains(Object o) { return texturePallettes.contains(o); }
   
    public Iterator<TexturePallette> iterator() { return texturePallettes.iterator(); }
   
    public TexturePallette[] toArray() { return texturePallettes.toArray(ARRAY_TEMPLATE); }
   
    public TexturePallette get(int index) { return texturePallettes.get(index); }
    
    /**
     * Identifies all textures needed for texture stitch.
     * Assumes a single texture per model.
     * Override if have something more complicated.
     */
    public void addTexturesForPrestich(List<String> textureList)
    {
        for(TexturePallette t : this.texturePallettes)
        {
            t.addTexturesForPrestich(textureList);
        }
    }
    
    public class TexturePallette
    {
        public final String textureBaseName;
        
        /** number of texture versions must be a power of 2 */
        public final int textureVersionCount;
        
        public final TextureScale textureScale;
        public final TextureLayout textureLayout;
        
        /**
         * Masks the version number provided by consumers - alternators that
         * drive number generation may support larger number of values. 
         * Implies number of texture versions must be a power of 2 
         */
        public final int textureVersionMask;
        
        /** Used to limit user selection, is not enforced. */
        public final boolean allowRotation;
        
        /** 
         * Bit at ordinal position indicates if mode is supported.
         * Used to limit user selection, is not enforced.
         */
        
        public final int lightingModeFlags;
        /** 
         * Layer that should be used for rendering this texture.
         * SOLID textures may still be rendered as translucent for materials like glass.
         */
        public final BlockRenderLayer renderLayer;
        
        /**
         * Globally unique id
         */
        public final int ordinal;
        
        /**
         * Used by modelstate to know which world state must be retrieved to drive this texture
         * (rotation and block version)
         */
        public final int stateFlags;
        
        public final TextureGroup textureGroup;

        /** yes, this is too damn many parameters, but not like it's going to escape into the wild... */
        private TexturePallette(int ordinal, String textureBaseName, int textureVersionCount, TextureScale textureScale, TextureLayout layout, boolean allowRotation, LightingMode[] lightingModes, BlockRenderLayer renderLayer, TextureGroup textureGroup)
        {
            this(ordinal, textureBaseName, textureVersionCount, textureScale, layout, allowRotation, 
                    LightingMode.makeLightFlags(lightingModes), renderLayer, textureGroup);
        }
        
        private TexturePallette(int ordinal, String textureBaseName, int textureVersionCount, TextureScale textureScale, TextureLayout layout, boolean allowRotation, int lightingModeFlags, BlockRenderLayer renderLayer, TextureGroup textureGroup)
        {
            this.ordinal = ordinal;
            this.textureBaseName = textureBaseName;
            this.textureVersionCount = textureVersionCount;
            this.textureVersionMask = Math.max(0, textureVersionCount - 1);
            this.textureScale = textureScale;
            this.textureLayout = layout;
            this.allowRotation = allowRotation;
            this.lightingModeFlags = lightingModeFlags;
            this.renderLayer = renderLayer;
            this.textureGroup = textureGroup;
  
            this.stateFlags = this.textureScale.modelStateFlag | this.textureLayout.modelStateFlag;
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
         * Used by dispatcher as nominal particle texture.
         * More important usage is by GUI texture picker.
         */
        public String getDefaultParticleTexture() 
        { 
            if(textureBaseName == null) return "";
            
            switch(textureLayout)
            {
            case BIGTEX:
                return buildTextureNameBigTex();
            case SPLIT_X_8:
            case MASONRY_5:    
            default:
                return buildTextureName_X_8(0);
                
            case BORDER_13:
                return buildTextureName_X_8(4);
            }
        }
        
        /** used for GUI texture preview */
        public TextureAtlasSprite getPreviewSprite()
        {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getDefaultParticleTexture());
        }
        
        public TextureAtlasSprite getTextureSprite(int version)
        {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buildTextureName(version & this.textureVersionMask));
        }
        
        private String buildTextureName(int version)
        {
            if(textureBaseName == null) return "";
            
            return (this.textureLayout == TextureLayout.BIGTEX)
                    ? buildTextureNameBigTex()
                    : buildTextureName_X_8(version);
        }
        
        public TextureAtlasSprite getTextureSprite(int version, int index)
        {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buildTextureName(version & this.textureVersionMask, index));
        }
        
        private String buildTextureName(int version, int index)
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
        
    }
}
