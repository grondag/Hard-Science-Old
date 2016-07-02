package grondag.adversity.niceblock;

import java.util.Random;

import grondag.adversity.feature.volcano.BlockVolcanicLava;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FlowingLavaBlock extends FlowBlock
{

    public FlowingLavaBlock(FlowBlockHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        world.scheduleUpdate(pos, this, tickRate);
    }
    
    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        world.scheduleUpdate(pos, this, tickRate);
    }
    
    @Override
    public boolean requiresUpdates()
    {
        return false;
    }
    
    private IBlockTest testIsThisBlock = new IBlockTest() {
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
            return ibs.getBlock() == FlowingLavaBlock.this;
        }
    };
    
    private IBlockTest testCanFlowInto = new IBlockTest() {
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState state, BlockPos pos) {

            if (state.getBlock() == FlowingLavaBlock.this)
            {
                return false;
            }

            Material material = state.getMaterial();
            if (material == Material.clay ) return false;
            if (material == Material.dragonEgg ) return false;
            if (material == Material.ground ) return false;
            if (material == Material.iron ) return false;
            if (material == Material.sand ) return false;
            if (material == Material.portal ) return false;
            if (material == Material.rock ) return false;
            if (material == Material.anvil ) return false;


//            if (material.blocksMovement())
//            {
//                return false;
//            }

            // Volcanic lava don't give no shits.
            return true;        
        }
    };
    
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
//        NeighborBlocks neighbors = new NeighborBlocks(world, pos);
//        NeighborTestResults resultsIsThisBlock = neighbors.getNeighborTestResults(testIsThisBlock);
//        NeighborTestResults resultsCanFlowInto = neighbors.getNeighborTestResults(testCanFlowInto);
//        
//        FlowType myFlowType = getFlowTypeEfficiently(state);
//        
//
//        // If already flowing vertically then done
//        if (resultsIsThisBlock.result(EnumFacing.DOWN)) return;
//
//        // Flow vertically if possible
//        if (resultsCanFlowInto.result(EnumFacing.DOWN))
//        {
//            flowIntoBlockEfficiently(world, pos.down(), state, myFlowType.canFlow ? FlowType.DROP_SOURCE : FlowType.STATIC_6);
//            return;
//        }
//        
//        // flow 1 can only flow down
//        if(!myFlowType.canFlow || myFlowType.outputFlowType == null)
//        {
//           world.setBlockState(pos, NiceBlockRegistrar.BLOCK_COOL_BASALT.getDefaultState().withProperty(NiceBlock.META, (int) (this.getFluidHeightForRender(world, pos) * 16) - 1));
//           return;
//        }
//
//        boolean didFlow = false;
//        
//        if (resultsCanFlowInto.result(EnumFacing.NORTH))
//        {
//            didFlow = flowIntoBlockEfficiently(world, pos.north(), state, myFlowType.outputFlowType) || didFlow;
//        }
//        if (resultsCanFlowInto.result(EnumFacing.SOUTH))
//        {
//            didFlow = flowIntoBlockEfficiently(world, pos.south(), state, myFlowType.outputFlowType) || didFlow;
//        }
//        if (resultsCanFlowInto.result(EnumFacing.EAST))
//        {
//            didFlow = flowIntoBlockEfficiently(world, pos.east(), state, myFlowType.outputFlowType) || didFlow;
//        }
//        if (resultsCanFlowInto.result(EnumFacing.WEST))
//        {
//            didFlow = flowIntoBlockEfficiently(world, pos.west(), state, myFlowType.outputFlowType) || didFlow;
//        }
//
//
//        // special case for source blocks to make flow circular
//        if(myFlowType.isSource)
//        {
//            if ((resultsCanFlowInto.result(EnumFacing.NORTH) || resultsCanFlowInto.result(EnumFacing.EAST)) && resultsCanFlowInto.result(BlockCorner.NORTH_EAST)) flowIntoBlockEfficiently(world, pos.north().east(), state, FlowType.FLOW_4);
//            if ((resultsCanFlowInto.result(EnumFacing.NORTH) || resultsCanFlowInto.result(EnumFacing.WEST)) && resultsCanFlowInto.result(BlockCorner.NORTH_WEST)) flowIntoBlockEfficiently(world, pos.north().west(), state, FlowType.FLOW_4);
//            if ((resultsCanFlowInto.result(EnumFacing.SOUTH) || resultsCanFlowInto.result(EnumFacing.EAST)) && resultsCanFlowInto.result(BlockCorner.SOUTH_EAST)) flowIntoBlockEfficiently(world, pos.south().east(), state, FlowType.FLOW_4);
//            if ((resultsCanFlowInto.result(EnumFacing.SOUTH) || resultsCanFlowInto.result(EnumFacing.WEST)) && resultsCanFlowInto.result(BlockCorner.SOUTH_WEST)) flowIntoBlockEfficiently(world, pos.south().west(), state, FlowType.FLOW_4);
//
//        }
//   
//        // edge flow blocks convert to static blocks if unable to flow
//        if(myFlowType.canFlow && !didFlow)
//        {
//            world.setBlockState(pos, state.withProperty(LEVEL, myFlowType.staticFlowType.ordinal()), 3);
//        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }
    
    /**
     * How many world ticks before ticking
     */
    @Override
    public int tickRate(World world)
    {
        return tickRate;
    }
}
