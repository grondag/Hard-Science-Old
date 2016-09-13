package grondag.adversity.niceblock.support;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.IBlockTestFactory;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;

public class BlockTests
{
    
    public static final IBlockTestFactory BIG_BLOCK_MATCH = new BigBlockMatchFactory();
    public static final IBlockTestFactory MASONRY_MATCH = new MasonryMatchFactory();
    
    private static class BigBlockMatchFactory implements IBlockTestFactory
    {
        @Override
        public IBlockTest makeTest(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return new BigBlockMatch2(world, ibs, pos);
        }
    }
    
    public static class BigBlockMatch2 implements IBlockTest
    {
        private final ModelDispatcher matchDispather;
        private final long matchKey;
        private final ModelColorMapComponent colorComponent;
        private final int matchSpecies;
        
        /** pass in the info for the block you want to match */
        public BigBlockMatch2(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            NiceBlock block = ((NiceBlockPlus)ibs.getBlock());
            this.matchDispather = block.dispatcher;
            
            //this.matchKey = block.getModelStateKey(ibs, world, pos);
            //prevent recursion - we don't need the full model state (which depends on this logic)
            // we just need the color, which is persisted in the TE key
            this.matchKey = ((NiceTileEntity) world.getTileEntity(pos)).getModelKey();
            this.colorComponent = matchDispather.getStateSet().getFirstColorMapComponent();
            this.matchSpecies = ibs.getValue(NiceBlock.META);
        }
        
        public BigBlockMatch2(NiceBlock block, long modelStateKey, int species)
        {
            this.matchDispather = block.dispatcher;
            this.matchKey = modelStateKey;
            this.colorComponent = matchDispather.getStateSet().getFirstColorMapComponent();
            this.matchSpecies = species;
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            
            // can only match with other NiceBlocks
            if(!(ibs.getBlock() instanceof NiceBlockPlus)) return false;
 
            return matchDispather == ((NiceBlockPlus)ibs.getBlock()).dispatcher
                   && matchSpecies == ibs.getValue(NiceBlock.META)
                   && matchDispather.getStateSet().doComponentValuesMatch(colorComponent, matchKey,
                           ((NiceTileEntity) world.getTileEntity(pos)).getModelKey());
        }       
    }
    
    private static class MasonryMatchFactory implements IBlockTestFactory
    {
        @Override
        public IBlockTest makeTest(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return new MasonryMatch(world, ibs, pos);
        }
    }
    
    public static class MasonryMatch implements IBlockTest
    {
        private final ModelDispatcher matchDispather;
        private final long matchKey;
        private final ModelColorMapComponent colorComponent;
        private final int matchSpecies;
        private final BlockPos origin;
        
        /** pass in the info for the block you want to match */
        public MasonryMatch(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            this.origin = pos;
            NiceBlock block = ((NiceBlockPlus)ibs.getBlock());
            this.matchDispather = block.dispatcher;
            
            //this.matchKey = block.getModelStateKey(ibs, world, pos);
            //prevent recursion - we don't need the full model state (which depends on this logic)
            // we just need the color, which is persisted in the TE key
            this.matchKey = ((NiceTileEntity) world.getTileEntity(pos)).getModelKey();

            this.colorComponent = matchDispather.getStateSet().getFirstColorMapComponent();
            this.matchSpecies = ibs.getValue(NiceBlock.META);
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            // for masonry blocks, a join indicates that a border IS present
            
            
            boolean isSibling = ibs.getBlock() instanceof NiceBlockPlus
                   && matchDispather == ((NiceBlockPlus)ibs.getBlock()).dispatcher;
            boolean isMate = isSibling 
                   && matchSpecies == ibs.getValue(NiceBlock.META)
                   && matchDispather.getStateSet().doComponentValuesMatch(colorComponent, matchKey,
                           ((NiceTileEntity) world.getTileEntity(pos)).getModelKey());
            boolean isSolid = ibs.isOpaqueCube();
            
            // no mortar between mates or non-solid blocks
            if(isMate || !isSolid) return false;
            
            // always mortar if not a sibling
            if(!isSibling) return true;
            
            // between siblings, only mortar on three sides of cube
            // (other sibling will do the mortar on other sides)
            return(pos.getX() == origin.getX() + 1 
                    || pos.getY() == origin.getY() + 1
                    || pos.getZ() == origin.getZ() + 1);
        }       
    }
    
       /** 
     * True if same block and color but not necessarily the same species.
     * Main use is to find candidate mates for big blocks being placed.
     */
    public static class TestForBlockColorMatch2 implements IBlockTest
    {
        private final ModelDispatcher matchDispather;
        private final long matchKey;
        private final ModelColorMapComponent colorComponent;
        
        /** pass in the info for the block you want to match */
        public TestForBlockColorMatch2(NiceBlock block, long modelStateKey)
        {
            this.matchDispather = block.dispatcher;
            this.matchKey = modelStateKey;
            this.colorComponent = matchDispather.getStateSet().getFirstColorMapComponent();
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            
            // can only match with other NiceBlocks
            if(!(ibs.getBlock() instanceof NiceBlockPlus)) return false;
 
            return matchDispather == ((NiceBlockPlus)ibs.getBlock()).dispatcher
                   && matchDispather.getStateSet().doComponentValuesMatch(colorComponent, matchKey,
                           ((NiceTileEntity) world.getTileEntity(pos)).getModelKey());
        }       
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
