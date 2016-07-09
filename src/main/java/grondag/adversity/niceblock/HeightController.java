package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.Rotation;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HeightController extends ModelController implements ICollisionHandler
{
    protected final IAlternator alternator;

    protected HeightController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, boolean isShaded,
            boolean useRotatedTexturesAsAlternates)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, useRotatedTexturesAsAlternates);
        this.alternator = Alternator.getAlternator(
                (byte)((useRotatedTexturesAsAlternates ? 4 : 1) * alternateTextureCount));
        this.bakedModelFactory = new HeightModelFactory(this);    }

    @Override
    public long getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
         return (this.alternator.getAlternate(pos) << 4) + state.getValue(NiceBlock.META);
    }

    @Override
    public int getAltTextureFromModelIndex(long l) {
        if (useRotatedTexturesAsAlternates) {
            return (int) ((l >>> 4) / 4);
        } else {
            return (int) (l >>> 4);
        }
    }
    
    protected Rotation getTextureRotationFromShapeIndex(long shapeIndex){
        if (useRotatedTexturesAsAlternates) {
            return Rotation.values()[(int) ((shapeIndex >>> 4) & 3)];
        } else {
            return Rotation.ROTATE_NONE;
        }
    }
    
    protected int getRenderHeight(long shapeIndex){
        return (int) shapeIndex & 15;
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return this;
    }

    @Override
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state)
    {
        return (long) state.getValue(NiceBlock.META);
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        ImmutableList<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, (collisionKey + 1)/16.0, 1)).build();
        return retVal;
    }
  
}
