package grondag.adversity.superblock.model.state;

public enum Translucency
{
    CLEAR(0.2F),
    TINTED(0.4F),
    SHADED(0.6F),
    STAINED(0.8F);
    
    public final float alpha;
    public final int alphaARGB;
    /** used by block method */
    public final int blockLightOpacity;
    
    private Translucency(float alpha)
    {
        this.alpha = alpha;
        this.blockLightOpacity = (int) Math.round((alpha - 0.2F) * 15);
        this.alphaARGB = ((int)Math.round(alpha * 255) << 24);
    }
}
