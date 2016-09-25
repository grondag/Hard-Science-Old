package grondag.adversity.library.model.quadfactory;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
/**
 * Used in QuadFactory primarily to enable full brightness rendering.
 * Quads will be packed with the format associated with this enum.
 * For full brightness, the BLOCK format is used, which has an extra UV
 * element that accepts a pre-baked lightmap.  VertexLighterFlat uses
 * this lightmap instead of the ambient lightmap if it is present.
 */
public enum LightingMode
{
    PRESHADED,
    SHADED,
    FULLBRIGHT;

    public VertexFormat vertexFormat = DefaultVertexFormats.ITEM;

    static
    {
        FULLBRIGHT.vertexFormat = DefaultVertexFormats.BLOCK;
    }
}