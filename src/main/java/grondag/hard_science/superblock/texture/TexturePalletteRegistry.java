package grondag.hard_science.superblock.texture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import grondag.exotic_matter.render.EnhancedSprite;
import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.movetogether.ITexturePalette;
import grondag.hard_science.movetogether.ModelStateData;
import grondag.hard_science.movetogether.TextureLayout;
import grondag.hard_science.movetogether.TextureRenderIntent;
import grondag.hard_science.movetogether.TextureRotationType;
import grondag.hard_science.movetogether.TextureScale;
import grondag.hard_science.movetogether.TextureRotationType.TextureRotationSetting;
import grondag.hard_science.superblock.model.painter.CubicQuadPainterBorders;
import grondag.hard_science.superblock.model.painter.CubicQuadPainterMasonry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TexturePalletteRegistry implements Iterable<ITexturePalette>
{
    
    private static final ITexturePalette[] ARRAY_TEMPLATE = new TexturePallette[0];
    
    private final ArrayList<ITexturePalette> texturePallettes = new ArrayList<ITexturePalette>();
    
    private int nextOrdinal = 0;
    
    public ITexturePalette addTexturePallette(String textureBaseName, TexturePalletteInfo info)
    {
        ITexturePalette result = new TexturePallette(nextOrdinal++, textureBaseName, info);
        texturePallettes.add(result);
        return result;
    }
    
    public ITexturePalette addZoomedPallete(ITexturePalette source)
    {
        ITexturePalette result = new TexturePallette(nextOrdinal++, source.textureBaseName(), 
                new TexturePalletteInfo(source)
                    .withZoomLevel(source.zoomLevel() + 1)
                    .withScale(source.textureScale().zoom()));
        texturePallettes.add(result);
        return result;
    }

    public int size() { return texturePallettes.size(); }

    public boolean isEmpty() { return texturePallettes.isEmpty(); }

    public boolean contains(Object o) { return texturePallettes.contains(o); }
   
    public Iterator<ITexturePalette> iterator() { return texturePallettes.iterator(); }
   
    public ITexturePalette[] toArray() { return texturePallettes.toArray(ARRAY_TEMPLATE); }
   
    public ITexturePalette get(int index) { return texturePallettes.get(index); }
    
    public static class TexturePalletteInfo
    {
        private int textureVersionCount = 1;
        private TextureScale textureScale = TextureScale.SINGLE; 
        private TextureLayout layout = TextureLayout.BIGTEX; 
        private TextureRotationSetting rotation = TextureRotationType.CONSISTENT.with(Rotation.ROTATE_NONE);
        private TextureRenderIntent renderIntent = TextureRenderIntent.BASE_ONLY; 
        private int textureGroupFlags = TextureGroup.ALWAYS_HIDDEN.bitFlag;
        private int zoomLevel = 0;
        /** number of ticks to display each frame */
        private int ticksPerFrame = 2;
        /** for border-layout textures, controls if "no border" texture is rendered */
        private boolean renderNoBorderAsTile = false;
        
        public TexturePalletteInfo()
        {
            
        }
        
        public TexturePalletteInfo(ITexturePalette source)
        {
            this.textureVersionCount = source.textureVersionCount();
            this.textureScale = source.textureScale();
            this.layout = source.textureLayout();
            this.rotation = source.rotation();
            this.renderIntent = source.renderIntent();
            this.textureGroupFlags = source.textureGroupFlags();
            this.zoomLevel = source.zoomLevel();
            this.ticksPerFrame = source.ticksPerFrame();
            this.renderNoBorderAsTile = source.renderNoBorderAsTile();
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
         * @see TexturePallette#renderIntent
         */
        public TexturePalletteInfo withRenderIntent(TextureRenderIntent renderIntent)
        {
            this.renderIntent = renderIntent;
            return this;
        }
        
        /**
         * @see TexturePallette#textureGroupFlags
         */
        public TexturePalletteInfo withGroups(TextureGroup... groups)
        {
            this.textureGroupFlags = TextureGroup.makeTextureGroupFlags(groups);
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
    
    public class TexturePallette implements ITexturePalette
    {
        private final String textureBaseName;
        
        /** number of texture versions must be a power of 2 */
        private final int textureVersionCount;
        
        private final TextureScale textureScale;
        private final TextureLayout textureLayout;
        
        /** 
         * Used to display appropriate label for texture.
         * 0 = no zoom, 1 = 2x zoom, 2 = 4x zoom
         */
        private final int zoomLevel;
        
        /**
         * Masks the version number provided by consumers - alternators that
         * drive number generation may support larger number of values. 
         * Implies number of texture versions must be a power of 2 
         */
        private final int textureVersionMask;
        
        /** Governs default rendering rotation for texture and what rotations are allowed. */
        private final TextureRotationSetting rotation;
        
        /** 
         * Determines layer that should be used for rendering this texture.
         */
        private final TextureRenderIntent renderIntent;
        
        /**
         * Globally unique id
         */
        private final int ordinal;
        
        /**
         * Used by modelstate to know which world state must be retrieved to drive this texture
         * (rotation and block version)
         */
        private final int stateFlags;
        
        private final int textureGroupFlags;
        
        /**
         * Number of ticks each frame should be rendered on the screen
         * before progressing to the next frame.
         */
        private final int ticksPerFrame;

        /** for border-layout textures, controls if "no border" texture is rendered */
        private final boolean renderNoBorderAsTile;

        protected TexturePallette(int ordinal, String textureBaseName, TexturePalletteInfo info)
        {
            this.ordinal = ordinal;
            this.textureBaseName = textureBaseName;
            this.textureVersionCount = info.textureVersionCount;
            this.textureVersionMask = Math.max(0, info.textureVersionCount - 1);
            this.textureScale = info.textureScale;
            this.textureLayout = info.layout;
            this.rotation = info.rotation;
            this.renderIntent = info.renderIntent;
            this.textureGroupFlags = info.textureGroupFlags;
            this.zoomLevel = info.zoomLevel;
            this.ticksPerFrame = info.ticksPerFrame;
            this.renderNoBorderAsTile = info.renderNoBorderAsTile;
  
            int flags = this.textureScale.modelStateFlag | this.textureLayout.modelStateFlag;
            
            // textures with randomization options also require position information
            
            if(info.rotation.rotationType() == TextureRotationType.RANDOM)
            {
                flags |= (ModelStateData.STATE_FLAG_NEEDS_TEXTURE_ROTATION | ModelStateData.STATE_FLAG_NEEDS_POS);
            }
            
            if(info.textureVersionCount > 1)
            {
                flags |= ModelStateData.STATE_FLAG_NEEDS_POS;
            }
            this.stateFlags =  flags;
                    
        }
        
        /* (non-Javadoc)
         * @see grondag.hard_science.superblock.texture.ITexturePallette#getTexturesForPrestich()
         */
        @Override
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
                // last texture (no border) only needed if indicated
                int texCount = this.renderNoBorderAsTile 
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
        
        /* (non-Javadoc)
         * @see grondag.hard_science.superblock.texture.ITexturePallette#getSampleTextureName()
         */
        @Override
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
        
        /**
         * See {@link #getSampleSprite()}
         */
        @SideOnly(Side.CLIENT)
        private EnhancedSprite sampleSprite;
        
        /* (non-Javadoc)
         * @see grondag.hard_science.superblock.texture.ITexturePallette#getSampleSprite()
         */
        @Override
        @SideOnly(Side.CLIENT)
        public EnhancedSprite getSampleSprite()
        {
            EnhancedSprite result = sampleSprite;
            if(result == null)
            {
                result = (EnhancedSprite)Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(this.getSampleTextureName());
                sampleSprite = result;
            }
            return result;
        }
        
        /* (non-Javadoc)
         * @see grondag.hard_science.superblock.texture.ITexturePallette#getTextureName(int)
         */
        @Override
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
        
        /* (non-Javadoc)
         * @see grondag.hard_science.superblock.texture.ITexturePallette#getTextureName(int, int)
         */
        @Override
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
        
        /* (non-Javadoc)
         * @see grondag.hard_science.superblock.texture.ITexturePallette#localizedName()
         */
        @Override
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
        
        public String textureBaseName() { return this.textureBaseName; }
        
        /** number of texture versions must be a power of 2 */
        public int textureVersionCount() { return this.textureVersionCount; }
        
        public TextureScale textureScale() { return this.textureScale; }
        public TextureLayout textureLayout() { return this.textureLayout; }
        
        /** 
         * Used to display appropriate label for texture.
         * 0 = no zoom, 1 = 2x zoom, 2 = 4x zoom
         */
        public int zoomLevel() { return this.zoomLevel; }
        
        /**
         * Masks the version number provided by consumers - alternators that
         * drive number generation may support larger number of values. 
         * Implies number of texture versions must be a power of 2 
         */
        public int textureVersionMask() { return this.textureVersionMask; }
        
        /** Governs default rendering rotation for texture and what rotations are allowed. */
        public TextureRotationSetting rotation() { return this.rotation; }
        
        /** 
         * Determines layer that should be used for rendering this texture.
         */
        public TextureRenderIntent renderIntent() { return this.renderIntent; }
        
        /**
         * Globally unique id
         */
        public int ordinal() { return this.ordinal; }
        
        /**
         * Used by modelstate to know which world state must be retrieved to drive this texture
         * (rotation and block version)
         */
        public int stateFlags() { return this.stateFlags; }
        
        public int textureGroupFlags() { return this.textureGroupFlags; }
        
        /**
         * Number of ticks each frame should be rendered on the screen
         * before progressing to the next frame.
         */
        public int ticksPerFrame() { return this.ticksPerFrame; }

        /** for border-layout textures, controls if "no border" texture is rendered */
        public boolean renderNoBorderAsTile() { return this.renderNoBorderAsTile; }
        
        
    }
}
