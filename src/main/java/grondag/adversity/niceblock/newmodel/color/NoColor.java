package grondag.adversity.niceblock.newmodel.color;

public class NoColor implements IColorProvider
{
    
    private static final ColorVector WHITE = new ColorVector("", 0xFFFFFFFF);
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
    public ColorVector getColor(int colorIndex)
    {
        return WHITE;
    }

}
