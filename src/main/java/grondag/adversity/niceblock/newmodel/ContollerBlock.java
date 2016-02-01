package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class ContollerBlock extends ModelControllerNew
{
    protected final IAlternator alternator;
    
    protected final BakedModelFactory bakedModelFactory;
    
    protected ContollerBlock(String styleName, String textureName, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(styleName, textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.alternator = Alternator.getAlternator(
                (byte)(((useRotations ? 4 : 1) * alternateCount) & 0xFF));
        this.bakedModelFactory = new ModelFactoryBlock(this);
    }

    public int getTextureOffsetFromShapeIndex(int shapeIndex) {
        if (useRotatedTexturesAsAlternates) {
            return shapeIndex / 4;
        } else {
            return shapeIndex;
        }
    }
    
    public Rotation getTextureRotationFromShapeIndex(int shapeIndex){
        if (useRotatedTexturesAsAlternates) {
            return Rotation.values()[shapeIndex & 3];
        } else {
            return Rotation.ROTATE_NONE;
        }
    }
    
    @Override
    public int getBlockShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
    }

    @Override
    public int getItemShapeIndex(ItemStack stack)
    {
        return 0;
    }

    @Override
    public int getBakedBlockModelCount()
    {
        return NiceColor.values().length * this.alternateTextureCount * (this.useRotatedTexturesAsAlternates? 4 : 1);
    }

    @Override
    public int getBakedItemModelCount()
    {
        return NiceColor.values().length;
    }

    @Override
    public BakedModelFactory getBakedModelFactory()
    {
        return bakedModelFactory;
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return null;
    }

}
