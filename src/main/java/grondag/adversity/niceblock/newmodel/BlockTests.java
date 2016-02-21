package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.model.ModelController;
import grondag.adversity.niceblock.newmodel.color.NiceColor;

public class BlockTests
{
    public static class TestForBigBlockMatch implements IBlockTest
    {
        private final NiceBlock matchBlock;
        private final int matchColorIndex;
        private final int matchSpecies;
        
        /** pass in the info for the block you want to match */
        public TestForBigBlockMatch(IBlockAccess world, IBlockState ibs, BlockPos pos){
            if(ibs.getBlock() instanceof NiceBlock)
            {
                matchBlock = (NiceBlock)ibs.getBlock();
                matchColorIndex = matchBlock.blockModelHelper.getModelStateForBlock(ibs, world, pos).getColorIndex();
                matchSpecies = ibs.getValue(NiceBlock.META);
            }
            else
            {
                matchBlock = null;
                matchColorIndex = -1;
                matchSpecies = 0;
            }
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
                   && matchColorIndex == candidate.blockModelHelper.getModelStateForBlock(ibs, world, pos).getColorIndex()
                   && matchSpecies == ibs.getValue(NiceBlock.META);
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