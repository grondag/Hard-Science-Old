package grondag.hard_science.superblock.terrain;

import java.util.List;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
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
    
    protected List<ItemStack> createSubItems()
    {
        return this.defaultSubItems();
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
