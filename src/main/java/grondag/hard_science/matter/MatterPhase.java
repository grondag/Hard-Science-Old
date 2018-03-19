package grondag.hard_science.matter;

import grondag.hard_science.init.ModTextures;
import net.minecraft.util.ResourceLocation;

public enum MatterPhase
{
    GAS(new ResourceLocation(ModTextures.BIGTEX_CLOUDS.getSampleTextureName())),
    SOLID(new ResourceLocation(ModTextures.BLOCK_NOISE_STRONG.getSampleTextureName())),
    LIQUID(new ResourceLocation(ModTextures.BIGTEX_FLUID_VORTEX.getSampleTextureName()));
    
    public final ResourceLocation iconResource;
    
    private MatterPhase(ResourceLocation iconResource)
    {
        this.iconResource = iconResource;
    }
}
