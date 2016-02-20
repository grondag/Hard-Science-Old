package grondag.adversity.niceblock.newmodel.color;

public class NoColor implements IColorProvider
{
    
    public static NoColor INSTANCE = new NoColor();
    
    private final ColorVector WHITE = new ColorVector("", 0xFFFFFFFF);

    @Override
    public int getColorCount()
    {
        return 1;
    }

    @Override
    public ColorVector getColor(int colorIndex)
    {
        return WHITE;
    }

}
