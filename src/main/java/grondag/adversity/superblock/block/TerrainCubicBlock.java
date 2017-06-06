package grondag.adversity.superblock.block;

import java.util.List;

import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TerrainCubicBlock extends TerrainDynamicBlock
{

    public TerrainCubicBlock(String blockName, BlockSubstance substance, ModelState defaultModelState)
    {
        super(blockName, substance, defaultModelState, false);
        
        ModelState modelState = defaultModelState.clone();
        modelState.setFlowState(new FlowHeightState(FlowHeightState.FULL_BLOCK_STATE_KEY));
        this.defaultModelStateBits = modelState.getBitsIntArray();

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
        return this.getSubItemsBasic();
    }
    
    public boolean isFlowFiller()
    {
        return false;
    }

    public boolean isFlowHeight()
    {
        return true;
    }
  
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
        return !neighborState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
    }

    public void makeStatic(IBlockState state, World world, BlockPos pos)
    {
        // already effectively static
    }
    
    @Override
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        return 1;
    }
 
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

}
