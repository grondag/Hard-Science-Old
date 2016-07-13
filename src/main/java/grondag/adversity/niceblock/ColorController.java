package grondag.adversity.niceblock;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.Rotation;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;

import net.minecraft.world.IBlockAccess;

public class ColorController extends ModelController
{
    protected final IAlternator alternator;
    
    public ColorController(String textureName, int alternateCount, BlockRenderLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.alternator = Alternator.getAlternator(
                (byte)((useRotations ? 4 : 1) * alternateCount));
        this.bakedModelFactory = new ColorModelFactory(this);
    }

    @Override
    public int getAltTextureFromModelIndex(long l) {
        if (useRotatedTexturesAsAlternates) {
            return (int) (l / 4);
        } else {
            return (int) l;
        }
    }
    
    protected Rotation getTextureRotationFromShapeIndex(long shapeIndex){
        if (useRotatedTexturesAsAlternates) {
            return Rotation.values()[(int) (shapeIndex & 3)];
        } else {
            return Rotation.ROTATE_NONE;
        }
    }
    
    @Override
    public long getDynamicShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
    }

//    @Override
//    public int getShapeCount()
//    {
//        return this.getAlternateTextureCount() * (this.useRotatedTexturesAsAlternates? 4 : 1);
//    }
}
