package grondag.adversity.niceblock.color;

public class FixedColorMapProvider implements IColorMapProvider
{
    private final ColorMap[] COLORS;

    public FixedColorMapProvider(ColorMap... colors)
    {
        COLORS = colors;
    }
    
    @Override
    public int getColorMapCount()
    {
        return COLORS.length;
    }

    @Override
    public ColorMap getColorMap(int colorIndex)
    {
        return COLORS[Math.max(0, Math.min(COLORS.length-1, colorIndex))];
    }
    
}