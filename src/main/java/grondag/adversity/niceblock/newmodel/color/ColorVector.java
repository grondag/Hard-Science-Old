package grondag.adversity.niceblock.newmodel.color;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import grondag.adversity.library.Color;

public class ColorVector
{
    public final String vectorName;
    public final int base;
    public final int highlight;
    public final int border;

    public ColorVector(String vectorName, int base, int highlight, int border)
    {
        this.vectorName = vectorName;
        this.base = base;
        this.highlight = highlight;
        this.border = border;
    }

    public ColorVector(String vectorName, int base, int highlight)
    {
        this(vectorName, base, highlight, highlight);
    }

    public ColorVector(String vectorName, int base)
    {
        this(vectorName, base, base, base);
    }
}
