package grondag.hard_science.superblock.texture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.model.painter.CubicQuadPainterBorders;
import grondag.hard_science.superblock.model.painter.CubicQuadPainterMasonry;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.TextureRotationType.TextureRotationSetting;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.text.translation.I18n;

public class TexturePalletteRegistry implements Iterable<TexturePalletteRegistry.TexturePallette>
{
    
    private static final TexturePallette[] ARRAY_TEMPLATE = new TexturePallette[0];
    
    private final ArrayList<TexturePallette> texturePallettes = new ArrayList<TexturePallette>();
    
    private int nextOrdinal = 0;
    
    public TexturePallette addTexturePallette(String textureBaseName, TexturePalletteInfo info)
    {
        TexturePallette result = new TexturePallette(nextOrdinal++, textureBaseName, info);
        texturePallettes.add(result);
        return result;
    }
    
    public TexturePallette addZoomedPallete(TexturePallette source)
    {
        TexturePallette result = new TexturePallette(nextOrdinal++, source.textureBaseName, 
                new TexturePalletteInfo(source)
                    .withZoomLevel(source.zoomLevel + 1)
                    .withScale(source.textureScale.zoom()));
        texturePallettes.add(result);
        return result;
    }

    public int size() { return texturePallettes.size(); }

    public boolean isEmpty() { return texturePallettes.isEmpty(); }

    public boolean contains(Object o) { return texturePallettes.contains(o); }
   
    public Iterator<TexturePallette> iterator() { return texturePallettes.iterator(); }
   
    public TexturePallette[] toArray() { return texturePallettes.toArray(ARRAY_TEMPLATE); }
   
    public TexturePallette get(int index) { return texturePallettes.get(index); }
    
    public static class TexturePalletteInfo
    {
        private int textureVersionCount = 1;
        private TextureScale textureScale = TextureScale.SINGLE; 
        private TextureLayout layout = TextureLayout.BIGTEX; 
        private TextureRotationSetting rotation = TextureRotationType.CONSISTENT.with(Rotation.ROTATE_NONE);
        private BlockRenderLayer renderLayer = BlockRenderLayer.SOLID; 
        private TextureGroup textureGroup = TextureGroup.ALWAYS_HIDDEN;
        private int zoomLevel = 0;
        /** number of ticks to display each frame */
        private int ticksPerFrame = 2;
        /** for border-layout textures, controls if "no border" texture is rendered */
        private boolean renderNoBorderAsTile = false;
        
        public TexturePalletteInfo()
        {
            
        }
        
        public TexturePalletteInfo(TexturePallette source)
        {
            this.textureVersionCount = source.textureVersionCount;
            this.textureScale = source.textureScale;
            this.layout = source.textureLayout;
            this.rotation = source.rotation;
            this.renderLayer = source.renderLayer;
            this.textureGroup = source.textureGroup;
            this.zoomLevel = source.zoomLevel;
            this.ticksPerFrame = source.ticksPerFrame;
            this.renderNoBorderAsTile = source.renderNoBorderAsTile;
        }

        /**
         * @see TexturePallette#textureVersionCount
         */
        public TexturePalletteInfo withVersionCount(int textureVersionCount)
        {
            this.textureVersionCount = textureVersionCount;
            return this;
        }
        
        /**
         * @see TexturePallette#textureScale
         */
        public TexturePalletteInfo withScale(TextureScale textureScale)
        {
            this.textureScale = textureScale;
            return this;
        }
        
        /**
         * @see TexturePallette#layout
         */
        public TexturePalletteInfo withLayout(TextureLayout layout)
        {
            this.layout = layout;
            return this;
        }
        
        /**
         * @see TexturePallette#rotation
         */
        public TexturePalletteInfo withRotation(TextureRotationSetting rotation)
        {
            this.rotation = rotation;
            return this;
        }
        
        /**
         * @see TexturePallette#renderLayer
         */
        public TexturePalletteInfo withRenderLayer(BlockRenderLayer renderLayer)
        {
            this.renderLayer = renderLayer;
            return this;
        }
        
        /**
         * @see TexturePallette#textureGroup
         */
        public TexturePalletteInfo withGroup(TextureGroup textureGroup)
        {
            this.textureGroup = textureGroup;
            return this;
        }
        
        /**
         * @see TexturePallette#zoomLevel
         */
        public TexturePalletteInfo withZoomLevel(int zoomLevel)
        {
            this.zoomLevel = zoomLevel;
            return this;
        }
        
        /**
         * @see TexturePallette#ticksPerFrame
         */
        public TexturePalletteInfo withTicksPerFrame(int ticksPerFrame)
        {
            this.ticksPerFrame = ticksPerFrame;
            return this;
        }
        
        public TexturePalletteInfo withRenderNoBorderAsTile(boolean renderAsTile)
        {
            this.renderNoBorderAsTile = renderAsTile;
            return this;
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
         * Used to display appropriate label for texture.
         * 0 = no zoom, 1 = 2x zoom, 2 = 4x zoom
         */
        public final int zoomLevel;
        
        /**
         * Masks the version number provided by consumers - alternators that
         * drive number generation may support larger number of values. 
         * Implies number of texture versions must be a power of 2 
         */
        public final int textureVersionMask;
        
        /** Governs default rendering rotation for texture and what rotations are allowed. */
        public final TextureRotationSetting rotation;
        
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
        
        /**
         * Number of ticks each frame should be rendered on the screen
         * before progressing to the next frame.
         */
        public final int ticksPerFrame;

        /** for border-layout textures, controls if "no border" texture is rendered */
        public final boolean renderNoBorderAsTile;

        protected TexturePallette(int ordinal, String textureBaseName, TexturePalletteInfo info)
        {
            this.ordinal = ordinal;
            this.textureBaseName = textureBaseName;
            this.textureVersionCount = info.textureVersionCount;
            this.textureVersionMask = Math.max(0, info.textureVersionCount - 1);
            this.textureScale = info.textureScale;
            this.textureLayout = info.layout;
            this.rotation = info.rotation;
            this.renderLayer = info.renderLayer;
            this.textureGroup = info.textureGroup;
            this.zoomLevel = info.zoomLevel;
            this.ticksPerFrame = info.ticksPerFrame;
            this.renderNoBorderAsTile = info.renderNoBorderAsTile;
  
            int flags = this.textureScale.modelStateFlag | this.textureLayout.modelStateFlag;
            
            // textures with randomization options also require position information
            
            if(info.rotation.rotationType() == TextureRotationType.RANDOM)
            {
                flags |= (ModelState.STATE_FLAG_NEEDS_TEXTURE_ROTATION | ModelState.STATE_FLAG_NEEDS_POS);
            }
            
            if(info.textureVersionCount > 1)
            {
                flags |= ModelState.STATE_FLAG_NEEDS_POS;
            }
            this.stateFlags =  flags;
                    
        }
        
        /**
         * Identifies all textures needed for texture stitch.
         */
        public List<String> getTexturesForPrestich()
        {
            if(this.textureBaseName == null) return Collections.emptyList();
            
            ArrayList<String> textureList = new ArrayList<String>();
            
            switch(this.textureLayout)
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
                for (int i = 0; i < this.textureVersionCount; i++)
                {
                    textureList.add(buildTextureNameBigTex());
                }
                break;
                
            case BORDER_13:
            {
                // last texture (no border) only needed if rendering in solid layer
                int texCount = this.renderLayer == BlockRenderLayer.SOLID 
                        ? CubicQuadPainterBorders.TEXTURE_COUNT 
                        : CubicQuadPainterBorders.TEXTURE_COUNT -1;
                
                for(int i = 0; i < this.textureVersionCount; i++)
                {
                    for(int j = 0; j < texCount; j++)
                    {
                        textureList.add(buildTextureName_X_8(i * CubicQuadPainterBorders.TEXTURE_BLOCK_SIZE + j));
                    }
                }
                break;
            }
            case MASONRY_5:
                for(int i = 0; i < this.textureVersionCount; i++)
                {
                    for(int j = 0; j < CubicQuadPainterMasonry.TEXTURE_COUNT; j++)
                    {
                        textureList.add(buildTextureName_X_8(i * CubicQuadPainterMasonry.TEXTURE_BLOCK_SIZE + j));
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
            
            return textureList;
        }
        
        private String buildTextureName_X_8(int offset)
        {
            return "hard_science:blocks/" + textureBaseName + "_" + (offset >> 3) + "_" + (offset & 7);
        }

        private String buildTextureNameBigTex()
        {
            return "hard_science:blocks/" + textureBaseName;
        }
        
        /** 
         * Used by dispatcher as nominal particle texture.
         * More important usage is by GUI texture picker.
         */
        public String getSampleTextureName() 
        { 
            if(textureBaseName == null) return "";
            
            switch(textureLayout)
            {
            case BIGTEX:
            case BIGTEX_ANIMATED:
                return buildTextureNameBigTex();
            case SPLIT_X_8:
            case MASONRY_5:    
            default:
                return buildTextureName_X_8(0);
                
            case BORDER_13:
                return buildTextureName_X_8(4);
            }
        }
        
        public String getTextureName(int version)
        {
            return buildTextureName(version & this.textureVersionMask);
        }
        
        private String buildTextureName(int version)
        {
            if(textureBaseName == null) return "";
            
            return (this.textureLayout == TextureLayout.BIGTEX || this.textureLayout == TextureLayout.BIGTEX_ANIMATED)
                    ? buildTextureNameBigTex()
                    : buildTextureName_X_8(version);
        }
        
        public String getTextureName(int version, int index)
        {
            return buildTextureName(version & this.textureVersionMask, index);
        }
        
        private String buildTextureName(int version, int index)
        {
            if(textureBaseName == null) return "";
            switch(textureLayout)
            {
            case MASONRY_5:
                return buildTextureName_X_8(version * CubicQuadPainterMasonry.TEXTURE_BLOCK_SIZE + index);
                
            case BORDER_13:
                return buildTextureName_X_8(version * CubicQuadPainterBorders.TEXTURE_BLOCK_SIZE + index);
                
            default:
                return buildTextureName_X_8(index);
            }
        }
        
        @SuppressWarnings("deprecation")
        public String localizedName()
        {
            String texName = I18n.translateToLocal("texture." + this.textureBaseName.toLowerCase());
            switch(this.zoomLevel)
            {
                case 1:
                    return I18n.translateToLocalFormatted("texture.zoom2x_format", texName);
                case 2:
                    return I18n.translateToLocalFormatted("texture.zoom4x_format", texName);
                default:
                    return texName;
            }
        }
    }
}
