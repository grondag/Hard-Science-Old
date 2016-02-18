package grondag.adversity.niceblock.newmodel.color;


public interface IColorProvider
{
    public abstract int getColorCount();
    public abstract ColorVector getColor(int colorIndex);
    public abstract int[] getSubset(ColorSubset subset);
    
    public static enum ColorSubset
    {
        FLEXSTONE_RAW,
        DURASTONE_RAW,
        NORMAL_BLOCK_COLORS,
        ALL_COLORS;
    }
}
