package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.feature.volcano.lava.EntityLavaBlob;
import grondag.hard_science.feature.volcano.lava.RenderLavaBlob;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class ModEntities
{

    public static void preInit(FMLPreInitializationEvent event) 
    {
        EntityRegistry.registerModEntity(new ResourceLocation("hard_science:lava_blob"), EntityLavaBlob.class, "hard_science:lava_blob", 1, HardScience.INSTANCE, 64, 10, true);

        if(event.getSide() == Side.CLIENT)
        {
            RenderingRegistry.registerEntityRenderingHandler(EntityLavaBlob.class, RenderLavaBlob.factory());
        }
    }
}
