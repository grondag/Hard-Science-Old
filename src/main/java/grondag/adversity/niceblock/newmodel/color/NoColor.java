package grondag.adversity.niceblock.newmodel.color;

public class NoColor implements IColorProvider
{
    
    private static final ColorMap WHITE = new ColorMap("", 0xFFFFFFFF);
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
