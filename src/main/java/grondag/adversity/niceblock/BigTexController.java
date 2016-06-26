package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;

public class BigTexController extends ModelController
{
    protected final boolean hasMetaVariants;

    public BigTexController(String textureName, BlockRenderLayer renderLayer, boolean isShaded, boolean hasMetaVariants)
    {
        super(textureName, 1, renderLayer, isShaded, false);
        this.bakedModelFactory = new BigTexModelFactory(this);
        this.textureCount = 1;
        this.hasMetaVariants = hasMetaVariants;
    }


    @Override
    /**
    * normally a 12 bit number that selects one of 4096 models in a repeating 16x16x16 volume
    * if meta alternates are enabled, becomes a 16-bit number, where bits in HSB order are:
    *      2 bit rotation index
    *      2 bits uv texture flip indicators
    *      12 bit facade index
    */
    public long getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(this.hasMetaVariants)
        {
             return state.getValue(NiceBlock.META) << 12 | ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
        }
        else
        {
            return ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
        }
    }

//    @Override
//    public int getShapeCount()
//    {
//        return this.hasMetaVariants ? 4096 * 16 : 4096;
//    }

    @Override
    public String getTextureName(int offset)
    {
        // Only one texture, with no suffixes
        return "adversity:blocks/" + textureName;
    }
}
