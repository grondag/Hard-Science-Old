package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.volcano.lava.EntityLavaBlob;
import grondag.hard_science.volcano.lava.RenderLavaBlob;
import mcjty.theoneprobe.TheOneProbe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
public class ModEntities
{

    public static void preInit(FMLPreInitializationEvent event) 
    {
        if(Configurator.VOLCANO.enableVolcano)
        {
            EntityRegistry.registerModEntity(new ResourceLocation("hard_science:lava_blob"), EntityLavaBlob.class, "hard_science:lava_blob", 1, HardScience.INSTANCE, 64, 10, true);
        }

        if(event.getSide() == Side.CLIENT)
        {
            if(Configurator.VOLCANO.enableVolcano)
            {
                RenderingRegistry.registerEntityRenderingHandler(EntityLavaBlob.class, RenderLavaBlob.factory());
            }
        }
        
        CapabilityManager.INSTANCE.register(ModPlayerCaps.class,new Capability.IStorage<ModPlayerCaps>() {

            @Override
            public NBTBase writeNBT(Capability<ModPlayerCaps> capability, ModPlayerCaps instance, EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<ModPlayerCaps> capability, ModPlayerCaps instance, EnumFacing side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });
    }
    
    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event)
    {
        if(event.getObject() instanceof EntityPlayer)
        {
            if (!event.getObject().hasCapability(ModPlayerCaps.CAP_INSTANCE, null)) {
                event.addCapability(new ResourceLocation(TheOneProbe.MODID, "PlayerCaps"), new ModPlayerCaps());
            }
        }
    }
}
