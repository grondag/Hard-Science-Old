package grondag.adversity.niceblock.texture;

public enum TextureScale
{
    /** 16x16 */
    LARGE(1),
    /** 8x8 */
    MEDIUM(2),
    /** 4x4 */
    SMALL(4),
    /** 2x2 */
    TINY(8),
    /** 1x1 */
    SINGLE(16);
    
    public final int sliceIncrement;
    
    private TextureScale(int sliceIncrement)
    {
        this.sliceIncrement = sliceIncrement;
    }

}