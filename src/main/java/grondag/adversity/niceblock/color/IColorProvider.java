package grondag.adversity.niceblock.color;


public interface IColorProvider
{
    public abstract int getColorCount();
    public abstract ColorMap getColor(int colorIndex);
}
