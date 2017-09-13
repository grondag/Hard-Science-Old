package grondag.hard_science.gui.control.machine;

public class BinaryGlTexture 
{
    public final int trueTextureID;
    public final int falseTextureID;

    public BinaryGlTexture(int trueTextureID, int falseTextureID)
    {
        this.trueTextureID = trueTextureID;
        this.falseTextureID = falseTextureID;
    }

    public int apply(boolean selector)
    {
        return selector ? trueTextureID : falseTextureID;
    }
}