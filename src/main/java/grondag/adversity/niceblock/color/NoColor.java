package grondag.adversity.niceblock.color;

import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

public class NoColor implements IColorProvider
{
    
    private static final ColorMap WHITE;
    
    static 
    {
        WHITE = new ColorMap("");
        for(EnumColorMap whichColor : EnumColorMap.values())
        {
            WHITE.setColor(whichColor, 0xFFFFFFFF);
        }
    }
    
    private final int virtualColorCount;

    public NoColor(int virtualColorCount)
    {
        this.virtualColorCount = virtualColorCount;
    }

    @Override
    public int getColorCount()
    {
        return virtualColorCount;
    }

    @Override
    public ColorMap getColor(int colorIndex)
    {
        return WHITE;
    }

}
