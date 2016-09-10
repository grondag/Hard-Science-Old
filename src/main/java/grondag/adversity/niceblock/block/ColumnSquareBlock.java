package grondag.adversity.niceblock.block;

import net.minecraft.util.math.BlockPos;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ColumnSquareBlock extends NiceBlockPlus
{

    public ColumnSquareBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }
 
    //TODO: should this be at least a little more optimized? 
    // may not be worth it - internal faces aren't generated in model iirc.
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        return facing.getAxis().ordinal();

    }

    @Override
    public List<ItemStack> getSubItems()
    {
        ModelColorMapComponent colorMap = dispatcher.getStateSet().getFirstColorMapComponent();
        int itemCount = (int) colorMap.getValueCount();
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < itemCount; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            long key = dispatcher.getStateSet().computeKey(colorMap.createValueFromBits(i),
                    ModelStateComponents.AXIS.fromEnum(EnumFacing.Axis.Y));
            NiceItemBlock.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
}
