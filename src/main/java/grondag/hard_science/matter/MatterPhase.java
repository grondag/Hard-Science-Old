package grondag.hard_science.matter;

import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.util.ResourceLocation;

public enum MatterPhase
{
    GAS(new ResourceLocation(Textures.BIGTEX_CLOUDS.getSampleTextureName())),
    SOLID(new ResourceLocation(Textures.BLOCK_NOISE_STRONG.getSampleTextureName())),
    LIQUID(new ResourceLocation(Textures.BIGTEX_FLUID_VORTEX.getSampleTextureName()));
    
    public final ResourceLocation iconResource;
    
    private MatterPhase(ResourceLocation iconResource)
    {
        this.iconResource = iconResource;
    }
}
