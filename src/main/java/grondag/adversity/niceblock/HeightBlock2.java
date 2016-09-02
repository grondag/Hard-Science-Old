package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.ModelDispatcher2;
import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.base.NiceBlockPlus2;
import grondag.adversity.niceblock.base.NiceItemBlock2;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HeightBlock2 extends NiceBlockPlus2
{
    
    public HeightBlock2(ModelDispatcher2 dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }

//    @Override
//    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
//    {
//        // TODO actually implement this
//        return true;
//    }

    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return state.getValue(NiceBlock2.META) == 15;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return state.getValue(NiceBlock2.META) == 15;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return state.getValue(NiceBlock2.META) == 15;
    }
    
    @Override
    public boolean isItemUsageAdditive(World worldIn, BlockPos pos, ItemStack stack)
    {
        IBlockState state = worldIn.getBlockState(pos);
        return state.getBlock() == this && state.getValue(META) < 15  
                && dispatcher.getStateSet().doComponentValuesMatch(dispatcher.getStateSet().getFirstColorMapComponent(),
                this.getModelStateKey(state, worldIn, pos), NiceItemBlock2.getModelStateKey(stack));
    }

    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        // add meta values if adding to existing block instance
        IBlockState state = worldIn.getBlockState(posPlaced);
        ModelStateSet set = dispatcher.getStateSet();
        ModelStateSetValue stateSetValue = set.getSetValueFromBits(NiceItemBlock2.getModelStateKey(stack));
        
        if(state.getBlock() == this 
                && set.doComponentValuesMatch(set.getFirstColorMapComponent(),
                        this.getModelStateKey(state, worldIn, posPlaced), NiceItemBlock2.getModelStateKey(stack)))
        {
            return (Math.min(15, state.getValue(META) + 1 + stateSetValue.getValue(set.getFirstSpeciesComponent())));
        }
        else
        {
            return (Math.min(15, stateSetValue.getValue(set.getFirstSpeciesComponent())));
        }
    }
    
    
}