package grondag.adversity.niceblock;

import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelState;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

public abstract class FlowBlock extends NiceBlock implements IFlowBlock
{
    
    protected int tickRate = 20;
    
    public FlowBlock(FlowHeightHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }


    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        if(blockAccess.getBlockState(pos.offset(side)).getBlock() instanceof IFlowBlock)
        {
            if(side == EnumFacing.UP || side == EnumFacing.DOWN)
            {
                return false;
            }
            else if(blockState instanceof IExtendedBlockState)
            {
                ModelState modelState = ((IExtendedBlockState)blockState).getValue(NiceBlock.MODEL_STATE);
                long shapeIndex = modelState.getShapeIndex(MinecraftForgeClient.getRenderLayer());
                FlowHeightState flowState = new FlowHeightState(shapeIndex);
                
                return flowState.getSideHeight(HorizontalFace.find(side)) < 0;

            }
            else
            {
                return true;
            }
        }
        else
        {
            return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
    }

//    @Override
//    public boolean isFullBlock(IBlockState state)
//    {
//        // TODO Auto-generated method stub
//        return super.isFullBlock(state);
//    }
//
//    @Override
//    public boolean isNormalCube(IBlockState state)
//    {
//        // TODO Auto-generated method stub
//        return super.isNormalCube(state);
//    }
//
    
     // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
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

    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int lightThis     = world.getCombinedLight(pos, 0);
        int lightUp       = world.getCombinedLight(pos.up(), 0);
        int lightThisBase = lightThis & 255;
        int lightUpBase   = lightUp & 255;
        int lightThisExt  = lightThis >> 16 & 255;
        int lightUpExt    = lightUp >> 16 & 255;
        return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) |
               ((lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16);
    }
 
    
}
