package grondag.adversity.niceblock.newmodel.color;

public class FixedColors implements IColorProvider
{
    private final ColorMap[] COLORS;

    public FixedColors(ColorMap... colors)
    {
        COLORS = colors;
    }
    
    @Override
    public int getColorCount()
    {
        return COLORS.length;
    }

    @Override
    public ColorMap getColor(int colorIndex)
    {
        return COLORS[Math.max(0, Math.min(COLORS.length-1, colorIndex))];
    }
    
}