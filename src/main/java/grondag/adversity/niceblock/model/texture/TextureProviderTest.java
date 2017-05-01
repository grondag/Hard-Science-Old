package grondag.adversity.niceblock.model.texture;

import org.junit.Test;

import grondag.adversity.library.model.quadfactory.LightingMode;
import net.minecraft.util.BlockRenderLayer;

public class TextureProviderTest
{

    
    @Test
    public void test()
    {
        TextureProvider provider1 = new TextureProvider();
        provider1.addTexture("One1", 4, TextureScale.MEDIUM, TextureLayout.SPLIT_X_8, true, TextureProviders.LIGHTING_BOTH, TextureProviders.SOLID_AND_CUTOUT);
        provider1.addTexture("One2", 4, TextureScale.LARGE, TextureLayout.SPLIT_X_8, true, TextureProviders.LIGHTING_BOTH, TextureProviders.SOLID_AND_CUTOUT);
        provider1.addTexture("One3", 2, TextureScale.SMALL, TextureLayout.SPLIT_X_8, true, TextureProviders.LIGHTING_SHADED_ONLY, TextureProviders.SOLID_ONLY);
        
        TextureProvider provider2 = new TextureProvider();
        provider2.addTexture("Two1", 4, TextureScale.SINGLE, TextureLayout.SPLIT_X_8, true, TextureProviders.LIGHTING_FULLBRIGHT_ONLY, TextureProviders.SOLID_AND_TRANS);
        provider2.addTexture("Two2", 4, TextureScale.SMALL, TextureLayout.SPLIT_X_8, true, TextureProviders.LIGHTING_BOTH, TextureProviders.SOLID_AND_TRANS);
        provider2.addTexture("Two3", 2, TextureScale.TINY, TextureLayout.SPLIT_X_8, true, TextureProviders.LIGHTING_BOTH, TextureProviders.CUTOUT_M_ONLY);

        int locator = provider1.get(2).getTextureState(true, LightingMode.FULLBRIGHT, BlockRenderLayer.SOLID).stateLocator();
        
        TextureProvider.Texture.TextureState state = provider1.getTextureState(locator);
        
        assert(state.lightingMode == LightingMode.FULLBRIGHT);
        assert(state.renderLayer == BlockRenderLayer.SOLID);
        assert(state.rotationEnabled == true);
        assert(state.textureBaseName().equals("One3"));
        assert(state.textureScale() == TextureScale.SMALL);
        assert(state.textureVersionCount() == 2);
        
        locator = provider2.get(0).getTextureState(false, LightingMode.SHADED, BlockRenderLayer.CUTOUT_MIPPED).stateLocator();
        
        state = provider2.getTextureState(locator);
        
        assert(state.lightingMode == LightingMode.SHADED);
        assert(state.renderLayer == BlockRenderLayer.CUTOUT_MIPPED);
        assert(state.rotationEnabled == false);
        assert(state.textureBaseName().equals("Two1"));
        assert(state.textureScale() == TextureScale.SINGLE);
        assert(state.textureVersionCount() == 4);

    }

}
