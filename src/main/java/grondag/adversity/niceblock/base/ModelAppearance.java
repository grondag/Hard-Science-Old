package grondag.adversity.niceblock.base;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.model.TextureScale;
import net.minecraft.util.BlockRenderLayer;

//TODO: WIP - for transition to more elaborate/varied block texturing
public class ModelAppearance
{
    public final String textureName;
    public final int textureVersionCount;
    public final TextureScale textureScale;
    
    //TODO: remove, for compatibility during conversion
    public final LightingMode lightingMode;
    public final BlockRenderLayer renderLayer;

    public ModelAppearance(String textureName, int textureVersionCount, TextureScale textureScale)
    {
        this.textureName = textureName;
        this.textureVersionCount = textureVersionCount;
        this.textureScale = textureScale;
        this.lightingMode = null;
        this.renderLayer = null;
    }
    
    public ModelAppearance(String textureName, int textureChoiceCount)
    {
        this(textureName, textureChoiceCount, TextureScale.SINGLE);
    }
    
  //TODO: remove, for compatibility during conversion
    public ModelAppearance(String textureName, LightingMode lightingMode, BlockRenderLayer renderLayer)
    {
        this.textureName = textureName;
        this.textureVersionCount = 1;
        this.textureScale = TextureScale.SINGLE;
        this.lightingMode = lightingMode;
        this.renderLayer = renderLayer;
    }
}