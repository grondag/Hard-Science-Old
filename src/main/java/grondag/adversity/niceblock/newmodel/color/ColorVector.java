package grondag.adversity.niceblock.newmodel.color;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import grondag.adversity.library.Color;

public class ColorVector
{
    public final String vectorName;
    public final Color base;
    public final Color highlight;
    public final Color border;

    public ColorVector(String vectorName, Color base, Color highlight, Color border)
    {
        this.vectorName = vectorName;
        this.base = base;
        this.highlight = highlight;
        this.border = border;
    }

    public ColorVector(String vectorName, Color base, Color highlight)
    {
        this(vectorName, base, highlight, highlight);
    }

    public ColorVector(String vectorName, Color base)
    {
        this(vectorName, base, base, base);
    }
}
