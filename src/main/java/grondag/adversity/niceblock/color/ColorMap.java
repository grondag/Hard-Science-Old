package grondag.adversity.niceblock.color;

public class ColorMap
{
    public final String colorMapName;
    private final int[] colors = new int[EnumColorMap.values().length];

    public ColorMap(String vectorName)
    {
        this.colorMapName = vectorName;
    }

    public ColorMap setColor(EnumColorMap whichColor, int colorValue)
    {
        colors[whichColor.ordinal()] = colorValue;
        return this;
    }
    
    public int getColorMap(EnumColorMap whichColor)
    {
        return colors[whichColor.ordinal()];
    }
    
    public static enum EnumColorMap
    {
        BASE,
        HIGHLIGHT,
        BORDER,
        LAMP
    }
}
