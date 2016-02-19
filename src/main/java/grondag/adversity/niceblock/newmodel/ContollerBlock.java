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

public class ContollerBlock extends ModelControllerNew
{
    protected final IAlternator alternator;
    
    protected final BakedModelFactory bakedModelFactory;
    
    protected final IColorProvider colorProvider;
        
    public ContollerBlock(String styleName, String textureName, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        this(styleName, textureName, BlockColors.INSTANCE, alternateCount, renderLayer, isShaded, useRotations);
    }

    public ContollerBlock(String styleName, String textureName, IColorProvider colorProvider, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(styleName, textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.alternator = Alternator.getAlternator(
                (byte)(((useRotations ? 4 : 1) * alternateCount) & 0xFF));
        this.bakedModelFactory = new ModelFactoryBlock(this);
        this.colorProvider = colorProvider;
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

    // can't see any case in which this would be useful
    // The stack already has an index when created, and if not, no way to figure it out.
//    @Override
//    public int getItemShapeIndex(ItemStack stack)
//    {
//        return 0;
//    }

    @Override
    public int getBakedBlockModelCount()
    {
        return getColorProvider().getColorCount() * this.alternateTextureCount * (this.useRotatedTexturesAsAlternates? 4 : 1);
    }

    @Override
    public int getBakedItemModelCount()
    {
        return getColorProvider().getColorCount();
    }

    @Override
    public BakedModelFactory getBakedModelFactory()
    {
        return bakedModelFactory;
    }

    @Override
    public IColorProvider getColorProvider()
    {
        return colorProvider;
    }
    
    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return null;
    }

    @Override
    public int getBlockModelIndex(ModelState state)
    {
        return (getColorProvider().getColorCount() * state.getShapeIndex()) + state.getColorIndex();
    }

    @Override
    public int getItemModelIndex(ModelState state)
    {
        return state.getColorIndex();
    }

}
