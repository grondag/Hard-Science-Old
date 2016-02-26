package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class BigTexController extends ModelControllerNew
{
    protected final BakedModelFactory bakedModelFactory;

    protected BigTexController(String textureName, EnumWorldBlockLayer renderLayer, boolean isShaded)
    {
        super(textureName, 1, renderLayer, isShaded, false);
        this.bakedModelFactory = new BigTexModelFactory(this);
        this.textureCount = 1;
    }

    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
    }

    @Override
    public int getShapeCount()
    {
        return 4096;
    }

    @Override
    public BakedModelFactory getBakedModelFactory()
    {
        return this.bakedModelFactory;
    }

    @Override
    public String getTextureName(int offset)
    {
        // Only one texture, with no suffixes
        return "adversity:blocks/" + textureName;
    }
}
