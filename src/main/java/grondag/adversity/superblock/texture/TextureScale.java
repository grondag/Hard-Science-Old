package grondag.adversity.superblock.texture;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

public enum TextureScale
{
    /** 1x1 */
    SINGLE(0, STATE_FLAG_NEEDS_BLOCK_RANDOMS),
    
    /** 2x2 */
    TINY(1, STATE_FLAG_NEEDS_2x2_BLOCK_RANDOMS),
    
    /** 4x4 */
    SMALL(2, STATE_FLAG_NEEDS_4x4_BLOCK_RANDOMS),
    
    /** 8x8 */
    MEDIUM(3, STATE_FLAG_NEEDS_8x8_BLOCK_RANDOMS),
    
    /** 16x16 */
    LARGE(4, STATE_FLAG_NEEDS_16x16_BLOCK_RANDOMS),
    
    /** 32x32 */
    GIANT(5, STATE_FLAG_NEEDS_32x32_BLOCK_RANDOMS);
    
    /** UV length for each subdivision of the texture */
    public final float sliceIncrement;
    
    /** number of texture subdivisions */
    public final int sliceCount;
    
    /** mask to derive a value within the number of slice counts (sliceCount - 1) */
    public final int sliceCountMask;
    
    /** number of texture subdivisions as an exponent of 2 */
    public final int power;
    
    /** for textures with this scale that can be rotated or have alternates, identifies the world state needed to drive random rotation/selection */
    public final int modelStateFlag;
    
    public TextureScale zoom()
    {
        if(this == GIANT)
        {
            return GIANT;
        }
        else
        {
            return values()[this.ordinal() + 1];
        }
    }
    
    private TextureScale(int power, int modelStateFlag)
    {
        this.power = power;
        this.sliceCount = 1 << power;
        this.sliceCountMask = sliceCount - 1;
        this.sliceIncrement = 16f / sliceCount;
        this.modelStateFlag = modelStateFlag;
    }

}