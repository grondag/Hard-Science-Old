package grondag.adversity.niceblock.base;

import java.util.List;

import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.model.texture.TextureProvider;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;

/**
 * Temporary adapter to hold texture state and model until texture state becomes part of model state
 */
public class ModelHolder
{
    public final ModelFactory model;
    public final TextureProvider.Texture.TextureState textureState;
    
    public ModelHolder(ModelFactory model, TextureProvider.Texture.TextureState textureState)
    {
        this.model = model;
        this.textureState = textureState;
    }
    
    public boolean canRenderInLayer(BlockRenderLayer renderLayer) 
    { 
        return this.textureState.renderLayer == renderLayer;
    }
    
    public LightingMode lightingMode()
    {
        return this.textureState.lightingMode;
    }
    
    public QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        return model.getFaceQuads(this.textureState, state, renderLayer);
    }
    
    public List<BakedQuad> getItemQuads(ModelStateSetValue state)
    {
        return model.getItemQuads(this.textureState, state);
    }

    public String getDefaultParticleTexture()
    {
        return this.textureState.getDefaultParticleTexture();
    }

    public ModelStateSet getStateSet()
    {
        return model.getStateSet();
    }
}
