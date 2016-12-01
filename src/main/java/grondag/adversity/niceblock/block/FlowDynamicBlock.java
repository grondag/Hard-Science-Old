package grondag.adversity.niceblock.block;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.config.Config;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FlowDynamicBlock extends NiceBlock implements IFlowBlock
{    
    private final boolean isFiller;
    private FlowStaticBlock staticVersion;


    public FlowDynamicBlock (ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller) {
        super(dispatcher, material, styleName);
        this.isFiller = isFiller;
    }

    public void setStaticVersion(FlowStaticBlock staticVersion)
    {
    	this.staticVersion = staticVersion;
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



    //  private SparseLayerMap getModel(long key)
    //  {

    ////      SparseLayerMap result = modelCache.get(key);
    //      SparseLayerMap result = cache.getUnchecked(key);
    //  
    //      return result;        
    //  }

//    private static AtomicLong duration = new AtomicLong(0);
//    private static AtomicLong count = new AtomicLong(0);
//    private static  AtomicLong sample = new AtomicLong(0);

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        //see Config.render().enableFaceCullingOnFlowBlocks for explanation
        
//        boolean result;
//        long start = System.nanoTime();
        IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
        if(Config.render().enableFaceCullingOnFlowBlocks && IFlowBlock.isFlowBlock(neighborState.getBlock()))
        {
            int myOcclusionKey = this.getOcclusionKey(blockState, blockAccess, pos, side);
            int otherOcclusionKey = ((NiceBlock)neighborState.getBlock()).getOcclusionKey(neighborState, blockAccess, pos.offset(side), side.getOpposite());
//            result = myOcclusionKey != otherOcclusionKey;
            return myOcclusionKey != otherOcclusionKey;
        }
        else
        {
//            result = !neighborState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
            return !neighborState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
//        duration.addAndGet(System.nanoTime() - start);
//        count.incrementAndGet();
//        synchronized(sample)
//        {     
//            if(count.get() >= 0xFFFL)
//            {
//                Adversity.log.info("Config=" + Config.render().enableFaceCullingOnFlowBlocks 
//                        + " Sample=" + sample.getAndIncrement() + " Avg time= " + duration.get() / count.get() );
//                duration.set(0);
//                count.set(0);
//
//            }
//        }
//        return result;
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

    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
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
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return IFlowBlock.shouldBeFullCube(state, world, pos);
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return IFlowBlock.isEmpty(worldIn.getBlockState(pos), worldIn, pos);

        //        return this.dispatcher.isEmpty(this.getModelStateKey(worldIn.getBlockState(pos), worldIn, pos));
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return IFlowBlock.isEmpty(state, world, pos);
        //        return this.dispatcher.isEmpty(this.getModelStateKey(state, world, pos));
    }

    @Override
    public boolean needsCustomHighlight()
    {
        return true;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        IFlowBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * Convert this block to a static version of itself if a static version was given.
     */
    public void makeStatic(IBlockState state, World world, BlockPos pos)
    {
        if(this.staticVersion == null || state.getBlock() != this) return;

        long oldKey = this.getModelStateKey(state, world, pos);
        world.setBlockState(pos, this.staticVersion.getDefaultState()
                .withProperty(NiceBlock.META, state.getValue(NiceBlock.META)), 7);
        staticVersion.setModelStateKey(world.getBlockState(pos), world, pos, oldKey);
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
