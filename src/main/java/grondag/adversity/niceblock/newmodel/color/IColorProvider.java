package grondag.adversity.niceblock.newmodel.color;


public interface IColorProvider
{
    public abstract int getColorCount();
    public abstract BlockColor getColor(int colorIndex);
}
