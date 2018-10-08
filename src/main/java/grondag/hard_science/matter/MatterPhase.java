package grondag.hard_science.matter;

import grondag.artbox.ArtBoxTextures;
import net.minecraft.util.ResourceLocation;

public enum MatterPhase
{
    GAS(new ResourceLocation(ArtBoxTextures.BIGTEX_CLOUDS.getSampleTextureName())),
    SOLID(new ResourceLocation(grondag.exotic_matter.init.ModTextures.BLOCK_NOISE_STRONG.getSampleTextureName())),
    LIQUID(new ResourceLocation(ArtBoxTextures.BIGTEX_FLUID_VORTEX.getSampleTextureName()));
    
    public final ResourceLocation iconResource;
    
    private MatterPhase(ResourceLocation iconResource)
    {
        this.iconResource = iconResource;
    }
}
