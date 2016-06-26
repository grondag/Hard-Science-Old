package grondag.adversity.feature.volcano;

import java.util.Random;

import grondag.adversity.Adversity;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockVolcanicLava extends BlockFluidClassic {
    
    private IBlockTest testIsThisBlock = new IBlockTest() {
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
            return ibs.getBlock() == BlockVolcanicLava.this;
        }
    };
    
    private IBlockTest testCanFlowInto = new IBlockTest() {
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState state, BlockPos pos) {
            
            Material material = state.getMaterial();

            if (material == Material.air) return true;

            if (state.getBlock() == BlockVolcanicLava.this)
            {
                return false;
            }

            if (displacements.containsKey(state.getBlock()))
            {
                return displacements.get(state.getBlock());
            }

            if (material.blocksMovement())
            {
                return false;
            }

            // Volcanic lava don't give no shits.
            return true;        
        }
    };
    
	public BlockVolcanicLava(Fluid fluid, Material material) {
		super(fluid, material);
		this.setCreativeTab(Adversity.tabAdversity);
		this.setQuantaPerBlock(5);
		defaultDisplacements.put(Blocks.reeds, true);
	}


    /**
     * Returns true if the block at (pos) is displaceable. Does not displace the block.
     */
//    public boolean canDisplaceEfficiently(IBlockState state)
//    {
//        Material material = state.getMaterial();
//
//        if (material == Material.air) return true;
//
//        if (state.getBlock() == this)
//        {
//            return false;
//        }
//
//        if (displacements.containsKey(state.getBlock()))
//        {
//            return displacements.get(state.getBlock());
//        }
//
//        if (material.blocksMovement())
//        {
//            return false;
//        }
//
//        // Volcanic lava don't give no shits.
//        return true;
//    }

//    /**
//     * Attempt to displace the block at (pos), return true if it was displaced.
//     */
//    public boolean displaceIfPossibleEfficiently(World world, BlockPos pos, IBlockState state)
//    {
//        if (canDisplaceEfficiently(state))
//        {
//            if(!(state.getBlock() instanceof BlockFluidBase))
//            {
//                state.getBlock().dropBlockAsItem(world, pos, state, 0);
//            }
//            return true;
//        }
//        return false;
//    }



//	@Override
//    protected boolean canFlowInto(IBlockAccess world, BlockPos pos)
//    {
//		if (world.isAirBlock(pos))
//			return true;
//
//		final IBlockState state = world.getBlockState(pos);
//		final Block block = state.getBlock();
//		if (block == this)
//			return true;
//
//		if (this.displacements.containsKey(block))
//			return this.displacements.get(block);
//
//		final Material material = block.getMaterial(state);
//		if (material.blocksMovement() || material == Material.portal)
//			return false;
//
//		final int density = getDensity(world, pos);
//		if (density == Integer.MAX_VALUE)
//			return true;
//
//		if (this.density > density)
//			return true;
//		else
//			return false;
//	}

//	private boolean isBasalt(Block b) {
//		return b == NiceBlockRegistrar.BLOCK_HOT_BASALT || b == NiceBlockRegistrar.BLOCK_COOL_BASALT;
//	}

	@Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
	    NeighborBlocks neighbors = new NeighborBlocks(world, pos);
	    NeighborTestResults resultsIsThisBlock = neighbors.getNeighborTestResults(testIsThisBlock);
	    NeighborTestResults resultsCanFlowInto = neighbors.getNeighborTestResults(testCanFlowInto);
	    
        FlowType myFlowType = getFlowTypeEfficiently(state);
        
     //   int expectedQuanta = -101;
        //boolean isSourceBlock = quantaRemaining >= quantaPerBlock;

        //float coolChance = COOL_CHANCE;
        
        //boolean isReadyToCool = false;
        
//        //can't cool if flowing down
//        if(!resultsCanFlowInto.result(EnumFacing.DOWN) && !resultsIsThisBlock.result(EnumFacing.DOWN))
//        {
//            // stopped if no more quanta
//            if(quantaRemaining == 1)
//            {
//                isReadyToCool = true;
//            }
//            // if have quanta but can't flow, then may be ready to cool
//            else if (!( resultsCanFlowInto.result(EnumFacing.NORTH)  ||
//                    resultsCanFlowInto.result(EnumFacing.SOUTH)  ||
//                    resultsCanFlowInto.result(EnumFacing.EAST)   ||
//                    resultsCanFlowInto.result(EnumFacing.WEST)))
//            {
//                
//                 int northQuanta = getFlowTypeEfficiently(neighbors.getBlockState(EnumFacing.NORTH));
//                int southQuanta = getFlowTypeEfficiently(neighbors.getBlockState(EnumFacing.SOUTH));
//                int eastQuanta = getFlowTypeEfficiently(neighbors.getBlockState(EnumFacing.EAST));
//                int westQuanta = getFlowTypeEfficiently(neighbors.getBlockState(EnumFacing.WEST));
//                int minQuanta = quantaRemaining;
//                
//                // if covered, then must be no uncovered neighbors and be at minimum of covered neighbors
//                if(resultsIsThisBlock.result(EnumFacing.UP))
//                {
//                    if(!((resultsIsThisBlock.result(EnumFacing.NORTH) && !resultsIsThisBlock.result(BlockCorner.UP_NORTH))
//                            || (resultsIsThisBlock.result(EnumFacing.SOUTH) && !resultsIsThisBlock.result(BlockCorner.UP_SOUTH))
//                            || (resultsIsThisBlock.result(EnumFacing.EAST) && !resultsIsThisBlock.result(BlockCorner.UP_EAST))
//                            || (resultsIsThisBlock.result(EnumFacing.WEST) && !resultsIsThisBlock.result(BlockCorner.UP_WEST))))
//                    {
//                        if(resultsIsThisBlock.result(EnumFacing.NORTH) && resultsIsThisBlock.result(BlockCorner.UP_NORTH)) minQuanta = Math.min(minQuanta, northQuanta);
//                        if(resultsIsThisBlock.result(EnumFacing.SOUTH) && resultsIsThisBlock.result(BlockCorner.UP_SOUTH)) minQuanta = Math.min(minQuanta, southQuanta);
//                        if(resultsIsThisBlock.result(EnumFacing.EAST) && resultsIsThisBlock.result(BlockCorner.UP_EAST)) minQuanta = Math.min(minQuanta, eastQuanta);
//                        if(resultsIsThisBlock.result(EnumFacing.WEST) && resultsIsThisBlock.result(BlockCorner.UP_WEST)) minQuanta = Math.min(minQuanta, westQuanta);
//                        
//                        isReadyToCool = quantaRemaining == minQuanta;
//                    }
//                }
//                // if not covered, then must be at minimum of uncovered neighbors
//                else
//                {
//                    if(resultsIsThisBlock.result(EnumFacing.NORTH) && !resultsIsThisBlock.result(BlockCorner.UP_NORTH)) minQuanta = Math.min(minQuanta, northQuanta);
//                    if(resultsIsThisBlock.result(EnumFacing.SOUTH) && !resultsIsThisBlock.result(BlockCorner.UP_SOUTH)) minQuanta = Math.min(minQuanta, southQuanta);
//                    if(resultsIsThisBlock.result(EnumFacing.EAST) && !resultsIsThisBlock.result(BlockCorner.UP_EAST)) minQuanta = Math.min(minQuanta, eastQuanta);
//                    if(resultsIsThisBlock.result(EnumFacing.WEST) && !resultsIsThisBlock.result(BlockCorner.UP_WEST)) minQuanta = Math.min(minQuanta, westQuanta);
//        
//                    isReadyToCool = quantaRemaining == minQuanta;
//
//                }
//            }
//        }
//    
//        if(isReadyToCool)
//        {
////            if(Useful.SALT_SHAKER.nextFloat() > coolChance) return;
//            world.setBlockState(pos, NiceBlockRegistrar.BLOCK_HOT_BASALT.getDefaultState().withProperty(NiceBlock.META, 3));
//            return;
//        }



        // If already flowing vertically then done
        if (resultsIsThisBlock.result(EnumFacing.DOWN)) return;

        // Flow vertically if possible
        if (resultsCanFlowInto.result(EnumFacing.DOWN))
        {
            flowIntoBlockEfficiently(world, pos.down(), state, myFlowType.canFlow ? FlowType.DROP_SOURCE : FlowType.STATIC_6);
            return;
        }
        
        // flow 1 can only flow down
        if(!myFlowType.canFlow || myFlowType.outputFlowType == null)
        {
           world.setBlockState(pos, NiceBlockRegistrar.BLOCK_COOL_BASALT.getDefaultState().withProperty(NiceBlock.META, (int) (this.getFluidHeightForRender(world, pos) * 16) - 1));
           return;
        }

        boolean didFlow = false;
        
        if (resultsCanFlowInto.result(EnumFacing.NORTH))
        {
            didFlow = flowIntoBlockEfficiently(world, pos.north(), state, myFlowType.outputFlowType) || didFlow;
        }
        if (resultsCanFlowInto.result(EnumFacing.SOUTH))
        {
            didFlow = flowIntoBlockEfficiently(world, pos.south(), state, myFlowType.outputFlowType) || didFlow;
        }
        if (resultsCanFlowInto.result(EnumFacing.EAST))
        {
            didFlow = flowIntoBlockEfficiently(world, pos.east(), state, myFlowType.outputFlowType) || didFlow;
        }
        if (resultsCanFlowInto.result(EnumFacing.WEST))
        {
            didFlow = flowIntoBlockEfficiently(world, pos.west(), state, myFlowType.outputFlowType) || didFlow;
        }


        // special case for source blocks to make flow circular
        if(myFlowType.isSource)
        {
            if ((resultsCanFlowInto.result(EnumFacing.NORTH) || resultsCanFlowInto.result(EnumFacing.EAST)) && resultsCanFlowInto.result(BlockCorner.NORTH_EAST)) flowIntoBlockEfficiently(world, pos.north().east(), state, FlowType.FLOW_4);
            if ((resultsCanFlowInto.result(EnumFacing.NORTH) || resultsCanFlowInto.result(EnumFacing.WEST)) && resultsCanFlowInto.result(BlockCorner.NORTH_WEST)) flowIntoBlockEfficiently(world, pos.north().west(), state, FlowType.FLOW_4);
            if ((resultsCanFlowInto.result(EnumFacing.SOUTH) || resultsCanFlowInto.result(EnumFacing.EAST)) && resultsCanFlowInto.result(BlockCorner.SOUTH_EAST)) flowIntoBlockEfficiently(world, pos.south().east(), state, FlowType.FLOW_4);
            if ((resultsCanFlowInto.result(EnumFacing.SOUTH) || resultsCanFlowInto.result(EnumFacing.WEST)) && resultsCanFlowInto.result(BlockCorner.SOUTH_WEST)) flowIntoBlockEfficiently(world, pos.south().west(), state, FlowType.FLOW_4);

        }
   
        // edge flow blocks convert to static blocks if unable to flow
        if(myFlowType.canFlow && !didFlow)
        {
            world.setBlockState(pos, state.withProperty(LEVEL, myFlowType.staticFlowType.ordinal()), 3);
        }
	}

   
    @Override
    public float getFluidHeightForRender(IBlockAccess world, BlockPos pos)
    {
        IBlockState here = world.getBlockState(pos);
        IBlockState up = world.getBlockState(pos.up());
        if (here.getBlock() == this)
        {
            if (up.getBlock() == this)
            {
                return 1;
            }

            return (float) getFlowTypeEfficiently(world.getBlockState(pos)).level / 5.0F;
        }
        return 0;
    }

//    protected int getLargerQuantaEfficiently(IBlockState state, int compare)
//    {
//        int quantaRemaining = getFlowTypeEfficiently(state);
//        if (quantaRemaining <= 0)
//        {
//            return compare;
//        }
//        return quantaRemaining >= compare ? quantaRemaining : compare;
//    }
 

    @Override
    public int getQuantaValue(IBlockAccess world, BlockPos pos)
    {
        return getFlowTypeEfficiently(world.getBlockState(pos)).level;
    }


    @Override
    public boolean isSourceBlock(IBlockAccess world, BlockPos pos)
    {
        return getFlowTypeEfficiently(world.getBlockState(pos)).isSource;
    }


    public FlowType getFlowTypeEfficiently(IBlockState state)
    {
        if (state.getBlock() != this)
        {
            return FlowType.NOT_LAVA;
        }
        return FlowType.values()[state.getValue(LEVEL)];
    }
    
    protected boolean flowIntoBlockEfficiently(World world, BlockPos pos, IBlockState state, FlowType flowType)
    {
        // For edge blocks there is a chance they won't flow unless they are a drop
        // This is make flows slightly irregular.
        // Returning false will let sender know to convert to a static block
        if(flowType == FlowType.FLOW_1 && !testCanFlowInto.testBlock(world, state, pos.down()) && Useful.SALT_SHAKER.nextFloat() >=  (0.75)) return false;
        
        world.setBlockState(pos, this.getBlockState().getBaseState().withProperty(LEVEL, flowType.ordinal()), 3);
        
        if(world.getBlockState(pos.down()).getBlock() == NiceBlockRegistrar.BLOCK_COOL_BASALT
                && world.getBlockState(pos.down()).getValue(NiceBlock.META) != 15)
        {
            world.setBlockState(pos.down(), NiceBlockRegistrar.BLOCK_COOL_BASALT.getDefaultState().withProperty(NiceBlock.META, 15));
        }
        return true;
    }
	

	@Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

	
    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return 15 << 20 | 15 << 4;
	}
    
 
    
    private enum FlowType
    {
        SOURCE(5, true, true),
        DROP_SOURCE(5, true, true),
        FLOW_1(1, true, false),
        FLOW_2(2, true, false),
        FLOW_3(3, true, false),
        FLOW_4(4, true, false),
        FLOW_5(4, true, false),
        STATIC_1(1, false, false),
        STATIC_2(2, false, false),
        STATIC_3(3, false, false),
        STATIC_4(4, false, false),
        STATIC_5(4, false, false),
        STATIC_6(5, false, false),
        NOT_LAVA(0, false, false);
        
        public final int level;
        public final boolean canFlow;
        public FlowType outputFlowType = null;
        public final boolean isSource;
        public FlowType staticFlowType = null;
        
        
        static
        {
            SOURCE.outputFlowType = FLOW_5;
            DROP_SOURCE.outputFlowType = FLOW_5;
            FLOW_2.outputFlowType = FLOW_1;
            FLOW_3.outputFlowType = FLOW_2;
            FLOW_4.outputFlowType = FLOW_3;
            FLOW_5.outputFlowType = FLOW_3;
            
            SOURCE.staticFlowType = STATIC_6;
            DROP_SOURCE.staticFlowType = STATIC_6;
            FLOW_1.staticFlowType = STATIC_1;
            FLOW_2.staticFlowType = STATIC_2;
            FLOW_3.staticFlowType = STATIC_3;
            FLOW_4.staticFlowType = STATIC_4;
            FLOW_5.staticFlowType = STATIC_5;

        }
        
        private FlowType(int level, boolean canFlow, boolean isSource)
        {
            this.level = level;
            this.canFlow = canFlow;
            this.isSource = isSource;
        }
    }

}
