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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FlowSimpleBlock extends NiceBlock implements IFlowBlock
{

    public FlowSimpleBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }

    /** 
     * This is an egregious hack to avoids performance hit of instanceof.
     * (Based on performance profile results.)
     * Returns true if this is a type of IFlowBlock
     */
    @Override
    public boolean isAssociatedBlock(Block other)
    {
        return other == IFlowBlock.FLOW_BLOCK_INDICATOR || super.isAssociatedBlock(other);
    }
    public boolean isFlowFiller()
    {
        return false;
    }

    public boolean isFlowHeight()
    {
        return true;
    }
    
    //allow mined blocks to stack - consistent with appearance of a full-height block
    @Override
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    //allow mined blocks to stack - don't put an NBT on them
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.damageDropped(state));
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
    
    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them static.
     */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        IFlowBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them static.
     */
    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState result = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        IFlowBlock.freezeNeighbors(worldIn, pos, result);
        return result;
    }
}
