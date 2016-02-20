package grondag.adversity.niceblock.newmodel.color;

public class FixedColors implements IColorProvider
{
    private final ColorVector[] COLORS;

    public FixedColors(ColorVector... colors)
    {
        COLORS = colors;
    }
    
    @Override
    public int getColorCount()
    {
        return COLORS.length;
    }

    @Override
    public ColorVector getColor(int colorIndex)
    {
        return COLORS[Math.max(0, Math.min(COLORS.length-1, colorIndex))];
    }
    
}