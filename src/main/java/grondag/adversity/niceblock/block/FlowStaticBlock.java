package grondag.adversity.niceblock.block;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FlowStaticBlock extends NiceBlockPlus implements IFlowBlock
{    
    private final boolean isFiller;
    
    public FlowStaticBlock (ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller) {
        super(dispatcher, material, styleName);
        this.isFiller = isFiller;
    }

//    @Override 
//    public boolean isDynamic()
//    {
//        return false;
//    }
    
    @Override
    public boolean isFiller()
    {
        return isFiller;
    }
    
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        
        IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
        if(neighborState.getBlock() instanceof IFlowBlock)
        {
            int myOcclusionKey = this.getOcclusionKey(blockState, blockAccess, pos, side);
            int otherOcclusionKey = ((NiceBlock)neighborState.getBlock()).getOcclusionKey(neighborState, blockAccess, pos.offset(side), side.getOpposite());
            return myOcclusionKey != otherOcclusionKey;
        }
        else
        {
            return !neighborState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
        
//        
//        if(blockAccess.getBlockState(pos.offset(side)).getBlock() instanceof FlowStaticBlock)
//        {
//            if(side == EnumFacing.UP || side == EnumFacing.DOWN)
//            {
//                return false;
//            }
//            else if(blockState instanceof IExtendedBlockState)
//            {
//                return this.dispatcher.getStateSet()
//                        .getSetValueFromBits(((IExtendedBlockState)blockState)
//                        .getValue(NiceBlock2.MODEL_KEY))
//                        .getValue(ModelStateComponents.FLOW_JOIN)
//                        .getSideHeight(HorizontalFace.find(side)) < 0;
//            }
//            else
//            {
//                return true;
//            }
//        }
//        else
//        {
//            return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
//        }
    }

    @Override
    public List<ItemStack> getSubItems()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();

        ItemStack stack = new ItemStack(this, 1, 0);
        int [] quadrants = new int[] {15, 15, 15, 15};
        long flowKey = FlowHeightState.computeStateKey(15, quadrants, quadrants, 0);
        long key = dispatcher.getStateSet()
                .computeKey(ModelStateComponents.FLOW_JOIN.createValueFromBits(flowKey));
        NiceItemBlock.setModelStateKey(stack, key);
        itemBuilder.add(stack);
        
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
    public boolean needsCustomHighlight()
    {
        return true;
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
