package grondag.adversity.superblock.texture;


public enum TextureGroup
{
    STATIC_TILES,
    STATIC_BORDERS,
    STATIC_DETAILS,
    STATIC_SPECIAL,
    DYNAMIC_TILES,
    DYNAMIC_BORDERS,
    DYNAMIC_DETAILS,
    DYNAMIC_SPECIAL;
    
    /** used as a fast way to filter textures from a list */
    public final int bitFlag;
    
    private TextureGroup()
    {
        this.bitFlag = (1 << this.ordinal());
    }
    
    public static int makeTextureGroupFlags(TextureGroup... groups)
    {
        int flags = 0;
        for(TextureGroup group : groups)
        {
            flags |= group.bitFlag;
        }
        return flags;
    }
}
