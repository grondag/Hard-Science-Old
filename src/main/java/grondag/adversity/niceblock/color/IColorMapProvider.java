package grondag.adversity.niceblock.color;


public interface IColorMapProvider
{
    public abstract int getColorMapCount();
    public abstract ColorMap getColorMap(int colorIndex);
}
