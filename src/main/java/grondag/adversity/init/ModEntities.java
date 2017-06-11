package grondag.adversity.init;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.EntityLavaBlob;
import grondag.adversity.feature.volcano.lava.RenderLavaBlob;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class ModEntities
{

    public static void preInit(FMLPreInitializationEvent event) 
    {
        EntityRegistry.registerModEntity(new ResourceLocation("adversity:lava_blob"), EntityLavaBlob.class, "adversity:lava_blob", 1, Adversity.INSTANCE, 64, 10, true);

        if(event.getSide() == Side.CLIENT)
        {
            RenderingRegistry.registerEntityRenderingHandler(EntityLavaBlob.class, RenderLavaBlob.factory());
        }
    }
}
