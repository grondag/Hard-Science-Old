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

    protected int getTextureOffsetFromShapeIndex(int shapeIndex) {
        if (useRotatedTexturesAsAlternates) {
            return shapeIndex / 4;
        } else {
            return shapeIndex;
        }
    }
    
    protected Rotation getTextureRotationFromShapeIndex(int shapeIndex){
        if (useRotatedTexturesAsAlternates) {
            return Rotation.values()[shapeIndex & 3];
        } else {
            return Rotation.ROTATE_NONE;
        }
    }
    
    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
    }

    @Override
    public int getShapeCount()
    {
        return this.getAlternateTextureCount() * (this.useRotatedTexturesAsAlternates? 4 : 1);
    }
}