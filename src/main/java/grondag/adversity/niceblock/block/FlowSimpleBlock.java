package grondag.adversity.niceblock.block;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlowSimpleBlock extends NiceBlock implements IFlowBlock
{

    public FlowSimpleBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }

    @Override
    public boolean isFiller()
    {
        return false;
    }

    public List<ItemStack> getSubItems()
    {
     
        long key = dispatcher.getStateSet()
                .computeKey(ModelStateComponents.FLOW_JOIN.createValueFromBits(FlowHeightState.FULL_BLOCK_STATE_KEY));
        
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < 16; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            NiceItemBlock.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
    
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        IFlowBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }


}
