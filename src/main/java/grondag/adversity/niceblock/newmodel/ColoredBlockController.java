package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.newmodel.color.BlockColors;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.color.NiceColor;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class ColoredBlockController extends ModelControllerNew
{
    protected final IAlternator alternator;
    
    public ColoredBlockController(String textureName, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.alternator = Alternator.getAlternator(
                (byte)((useRotations ? 4 : 1) * alternateCount));
        this.bakedModelFactory = new ColoredBlockModelFactory(this);
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
        return this.alternateTextureCount * (this.useRotatedTexturesAsAlternates? 4 : 1);
    }
}
