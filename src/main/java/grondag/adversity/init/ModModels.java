package grondag.adversity.init;

import java.io.IOException;
import java.util.ArrayList;

import grondag.adversity.niceblock.model.texture.TextureProvider;
import grondag.adversity.niceblock.model.texture.TextureProviders;
import grondag.adversity.superblock.block.SuperDispatcher;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModModels
{

    public static final SuperDispatcher MODEL_DISPATCH = new SuperDispatcher("model_dispatcher");
    
    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        // SET UP COLOR ATLAS
        // {
        // NiceHues.INSTANCE.writeColorAtlas(event.getModConfigurationDirectory());
        // }
        
        ModModels.MODEL_DISPATCH.clear();
        
        for (int i = 0; i < ModelState.BENUMSET_RENDER_LAYER.combinationCount(); i++)
        {
            SuperDispatcher.DispatcherDelegate delegate = ModModels.MODEL_DISPATCH.getDelegateForShadedFlags(i);
            event.getModelRegistry().putObject(new ModelResourceLocation(delegate.getModelResourceString()), delegate);
        }
    }

    /**
     * Register all textures that will be needed for associated models. 
     * Happens before model bake.
     */
    @SubscribeEvent
    public static void stitcherEventPre(TextureStitchEvent.Pre event)
    {
        ArrayList<String> textureList = new ArrayList<String>();
        
        Textures.ALL_TEXTURES.addTexturesForPrestich(textureList);
        
        for(String s : textureList)
        {
            event.getMap().registerSprite(new ResourceLocation(s));
        }
    }
}
