package grondag.hard_science.library.render;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Used in QuadFactory primarily to enable full brightness rendering.
 * Quads will be packed with the format associated with this enum.
 * For full brightness, the BLOCK format is used, which has an extra UV
 * element that accepts a pre-baked lightmap.  VertexLighterFlat uses
 * this lightmap instead of the ambient lightmap if it is present.
 */
public enum LightingMode
{
    SHADED,
    FULLBRIGHT;

    @SideOnly(Side.CLIENT)
    public net.minecraft.client.renderer.vertex.VertexFormat getVertexFormat()
    {
        return this==SHADED 
                ? net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM
                :net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK;
    }
    
    public static int makeLightFlags(LightingMode[] lightingModes)
    {
        int lightFlags = 0;
        for(LightingMode mode : lightingModes)
        {
            lightFlags |= 1 << mode.ordinal();
        }
        return lightFlags;
    }
}