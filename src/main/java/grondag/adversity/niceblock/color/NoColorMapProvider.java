package grondag.adversity.niceblock.color;

import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

public class NoColorMapProvider implements IColorMapProvider
{
    public static final NoColorMapProvider INSTANCE = new NoColorMapProvider(1);
    
    private static final ColorMap WHITE;
    
    static 
    {
        WHITE = new ColorMap("", null, null, null,  0);
        for(EnumColorMap whichColor : EnumColorMap.values())
        {
            WHITE.setColor(whichColor, 0xFFFFFFFF);
        }
    }
    
    private final int virtualColorCount;

    public NoColorMapProvider(int virtualColorCount)
    {
        this.virtualColorCount = virtualColorCount;
    }

    @Override
    public int getColorMapCount()
    {
        return virtualColorCount;
    }

    @Override
    public ColorMap getColorMap(int colorIndex)
    {
        return WHITE;
    }

}
