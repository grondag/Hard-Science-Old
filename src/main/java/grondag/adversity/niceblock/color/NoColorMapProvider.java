package grondag.adversity.niceblock.color;

import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

public class NoColorMapProvider implements IColorMapProvider
{
    
    private static final ColorMap WHITE;
    
    static 
    {
        WHITE = new ColorMap("", 0);
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
