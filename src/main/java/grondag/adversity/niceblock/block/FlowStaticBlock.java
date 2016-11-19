package grondag.adversity.niceblock.block;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.base.NiceTileEntity.ModelRefreshMode;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FlowStaticBlock extends NiceBlockPlus implements IFlowBlock
{    
    private final boolean isFiller;
    
    public FlowStaticBlock (ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller) {
        super(dispatcher, material, styleName);
        this.isFiller = isFiller;
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
        return isFiller;
    }

    public boolean isFlowHeight()
    {
        return !isFiller;
    }
    
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        
        IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
        if(IFlowBlock.isFlowBlock(neighborState.getBlock()))
        {
            int myOcclusionKey = this.getOcclusionKey(blockState, blockAccess, pos, side);
            int otherOcclusionKey = ((NiceBlock)neighborState.getBlock()).getOcclusionKey(neighborState, blockAccess, pos.offset(side), side.getOpposite());
            return myOcclusionKey != otherOcclusionKey;
        }
        else
        {
            return !neighborState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
    }

    @Override
    public List<ItemStack> getSubItems()
    {
        int itemCount = this.isFiller ? 2 : FlowHeightState.BLOCK_LEVELS_INT;
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < itemCount; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            int level = this.isFiller ? FlowHeightState.BLOCK_LEVELS_INT - 1 : FlowHeightState.BLOCK_LEVELS_INT - i;
            int [] quadrants = new int[] {level, level, level, level};
            long flowKey = FlowHeightState.computeStateKey(level, quadrants, quadrants, 0);
            long key = dispatcher.getStateSet()
                    .computeKey(ModelStateComponents.FLOW_JOIN.createValueFromBits(flowKey));
            NiceItemBlock.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
    
    
//    @Override
//    public boolean isFullBlock(IBlockState state)
//    {
//        return super.isFullBlock(state);
//    }
    
    
    
    @Override
    public long getModelStateKey(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        NiceTileEntity myTE = (NiceTileEntity) world.getTileEntity(pos);
       return myTE == null ? 0 : myTE.getModelKey();
    }

    @Override
    public ModelRefreshMode getModelRefreshMode()
    {
        return ModelRefreshMode.ALWAYS;
    }

    public void setModelStateKey(IBlockState state, IBlockAccess world, BlockPos pos, long modelKey)
    {
       NiceTileEntity myTE = (NiceTileEntity) world.getTileEntity(pos);
       if(myTE != null) myTE.setModelKey(modelKey);
    }

    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        //TODO: make this dependent on model state
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        //TODO: make this dependent on model state
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        double volume = 0;
        for(AxisAlignedBB box : this.getCachedModelBounds(state, world, pos))
        {
            volume += Useful.AABBVolume(box);
        }
        
        return (int) Math.min(9, volume * 9);
    }
    
    @Override
    public boolean hasAppearanceGui()
    {
        return false;
    }
    
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ItemStack stack = super.getStackFromBlock(state, world, pos);
        
        if(stack != null)
        {
            NiceItemBlock myItem = (NiceItemBlock) Item.getItemFromBlock(this);
            ModelStateSetValue modelState = this.dispatcher.getStateSet().getSetValueFromBits( this.getModelStateKey(state, world, pos));
            myItem.setFlowState(stack, modelState.getWrappedValue(ModelStateComponents.FLOW_JOIN));
        }

        return stack;
    }
    
    @Override
    public boolean needsCustomHighlight()
    {
        return true;
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
    
    


//    @Override
//    public int getPackedLightmapCoords(IBlockState state, IBlockAccess world, BlockPos pos)
//    {
//        // This is borrowed from BlockFluidBase. 
//        // Not sure it needs to be here.
//        
//        int lightThis     = world.getCombinedLight(pos, 0);
//        int lightUp       = world.getCombinedLight(pos.up(), 0);
//        int lightThisBase = lightThis & 255;
//        int lightUpBase   = lightUp & 255;
//        int lightThisExt  = lightThis >> 16 & 255;
//        int lightUpExt    = lightUp >> 16 & 255;
//        return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) |
//               ((lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16);
//    }
 
    
}
