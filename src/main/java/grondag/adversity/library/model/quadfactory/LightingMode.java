package grondag.adversity.library.model.quadfactory;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public enum LightingMode
{
    FLAT,
    SHADED,
    FULLBRIGHT;

    public VertexFormat vertexFormat = DefaultVertexFormats.ITEM;

    static
    {
        FULLBRIGHT.vertexFormat = DefaultVertexFormats.BLOCK;
    }
}