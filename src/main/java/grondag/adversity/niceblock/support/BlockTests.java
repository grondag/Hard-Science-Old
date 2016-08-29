package grondag.adversity.niceblock.support;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.IBlockTestFactory;
import grondag.adversity.niceblock.base.ModelDispatcher2;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.base.NiceBlockPlus2;
import grondag.adversity.niceblock.base.NiceTileEntity2;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;

public class BlockTests
{
    
    public static class TestForBigBlockMatch implements IBlockTest
    {
        private final NiceBlock matchBlock;
        private final int matchColorIndex;
        private final int matchSpecies;
        
        /** pass in the info for the block you want to match */
        public TestForBigBlockMatch(NiceBlock matchBlock, int matchColorIndex, int matchSpecies){
            this.matchBlock = matchBlock;
            this.matchColorIndex = matchColorIndex;
            this.matchSpecies = matchSpecies;
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            if(matchBlock == null)
            {
                // not initialized with a NiceBlock instance
                return false;
            }
            
            if(!(ibs.getBlock() instanceof NiceBlock))
            {
                // can only match with other NiceBlocks
                return false;
            }
            
            NiceBlock candidate = (NiceBlock)ibs.getBlock();
            

            return matchBlock.blockModelHelper.dispatcher == candidate.blockModelHelper.dispatcher
                   && matchColorIndex == candidate.blockModelHelper.getModelStateForBlock(ibs, world, pos, false).getColorIndex()
                   && matchSpecies == ibs.getValue(NiceBlock.META);
        }
        
    }
    
    public static final IBlockTestFactory BIG_BLOCK_MATCH = new BigBlockMatchFactory();
    
    private static class BigBlockMatchFactory implements IBlockTestFactory
    {
        @Override
        public IBlockTest makeTest(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return new BigBlockMatch2(world, ibs, pos);
        }
    }
    
    private static class BigBlockMatch2 implements IBlockTest
    {
        private final ModelDispatcher2 matchDispather;
        private final long matchKey;
        private final ModelColorMapComponent colorComponent;
        private final int matchSpecies;
        
        /** pass in the info for the block you want to match */
        private BigBlockMatch2(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            NiceBlock2 block = ((NiceBlockPlus2)ibs.getBlock());
            this.matchDispather = block.dispatcher;
            this.matchKey = block.getModelStateKey(ibs, world, pos);
            this.colorComponent = matchDispather.getStateSet().getFirstColorMapComponent();
            this.matchSpecies = ibs.getValue(NiceBlock2.META);
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            
            // can only match with other NiceBlocks
            if(!(ibs.getBlock() instanceof NiceBlockPlus2)) return false;
 
            return matchDispather == ((NiceBlockPlus2)ibs.getBlock()).dispatcher
                   && matchSpecies == ibs.getValue(NiceBlock.META)
                   && matchDispather.getStateSet().doComponentValuesMatch(colorComponent, matchKey,
                           ((NiceTileEntity2) world.getTileEntity(pos)).getModelKey());
        }
        
    }
    public static class BigBlockMatch implements IBlockTest
    {
        private final NiceBlock matchBlock;
        private final int matchColorIndex;
        private final int matchSpecies;
        
        /** pass in the info for the block you want to match */
        public BigBlockMatch(NiceBlock matchBlock, int matchColorIndex, int matchSpecies){
            this.matchBlock = matchBlock;
            this.matchColorIndex = matchColorIndex;
            this.matchSpecies = matchSpecies;
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            if(matchBlock == null)
            {
                // not initialized with a NiceBlock instance
                return false;
            }
            
            if(!(ibs.getBlock() instanceof NiceBlock))
            {
                // can only match with other NiceBlocks
                return false;
            }
            
            NiceBlock candidate = (NiceBlock)ibs.getBlock();
            

            return matchBlock.blockModelHelper.dispatcher == candidate.blockModelHelper.dispatcher
                   && matchColorIndex == candidate.blockModelHelper.getModelStateForBlock(ibs, world, pos, false).getColorIndex()
                   && matchSpecies == ibs.getValue(NiceBlock.META);
        }
        
    }

    
    /** 
     * True if same block and color but not necessarily the same species.
     * Main use is to find candidate mates for big blocks being placed.
     */
    public static class TestForBlockColorMatch implements IBlockTest
    {
        private final NiceBlock matchBlock;
        private final int matchColorIndex;
        
        /** pass in the info for the block you want to match */
        public TestForBlockColorMatch(NiceBlock matchBlock, int matchColorIndex){
            this.matchBlock = matchBlock;
            this.matchColorIndex = matchColorIndex;
       }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            if(matchBlock == null)
            {
                // not initialized with a NiceBlock instance
                return false;
            }
            
            if(!(ibs.getBlock() instanceof NiceBlock))
            {
                // can only match with other NiceBlocks
                return false;
            }
            
            NiceBlock candidate = (NiceBlock)ibs.getBlock();
            

            return matchBlock.blockModelHelper.dispatcher == candidate.blockModelHelper.dispatcher
                   && matchColorIndex == candidate.blockModelHelper.getModelStateForBlock(ibs, world, pos, false).getColorIndex();
        }
    }
    
    public static class TestForBlockMetaMatch implements IBlockTest
    {
        private final NiceBlock matchBlock;
        private final int meta;
        
        /** pass in the info for the block you want to match */
        public TestForBlockMetaMatch(IBlockState ibs){
            this.matchBlock = (NiceBlock)ibs.getBlock();
            this.meta = this.matchBlock != null ? ibs.getValue(NiceBlock.META) : 0;
         }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            if(matchBlock == null)
            {
                // not initialized with a NiceBlock instance
                return false;
            }
            
            if(!(ibs.getBlock() instanceof NiceBlock))
            {
                // can only match with other NiceBlocks
                return false;
            }
            
            NiceBlock candidate = (NiceBlock)ibs.getBlock();
            

            return matchBlock.blockModelHelper.dispatcher == candidate.blockModelHelper.dispatcher
                   && meta == ibs.getValue(NiceBlock.META);
        }
        
    }
//    Started to port this to new system but not sure about use case.  
//    /**
//    * Blocks match if they have are the same block and same substance. Also
//    * implies the same style.
//    */
//    public static class TestForCompleteMatch implements IBlockTest
//    {
//         private final NiceBlock block;
//         private final int meta;
//         private final ModelState
//        
//         /**
//         * Blocks match if they have are the same block and same substance. Also
//         * implies the same style. Pass in the state of the block you want to
//         * match with.
//         */
//         public TestForCompleteMatch(IBlockState ibs) {
//         block = ibs.getBlock();
//         meta = ibs.getValue(META);
//         }
//        
//         @Override
//         public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
//         TileEntity te = world.getTileEntity(pos);
//         if(te != null && te instanceof NiceTileEntity && ((NiceTileEntity)te).isDeleted){
//         Adversity.log.info("caught deleted at" + pos.toString());
//         return false;
//         }
//         return ibs.getBlock() == block && ibs.getValue(META) == meta;
//         }
//         }
//        @Override
//        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
//        {
//            // TODO Auto-generated method stub
//            return false;
//        }
//        
//    }
}