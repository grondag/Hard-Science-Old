package grondag.adversity.niceblock.support;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.IBlockTestFactory;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperModelBlock;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;

@SuppressWarnings("unused")
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
    
    public static class SuperBlockBorderMatch implements IBlockTest
    {
        private final SuperBlock block;
        private final ModelState matchModelState;
        private final boolean isSpeciesPartOfMatch;
        
        /** pass in the info for the block you want to match */
        public SuperBlockBorderMatch(SuperBlock block, ModelState modelState, boolean isSpeciesPartOfMatch)
        {
            this.block = block;
            this.matchModelState = modelState;
            this.isSpeciesPartOfMatch = isSpeciesPartOfMatch;
        }
        
        /** assumes you want to match block at given position */
        public SuperBlockBorderMatch(IBlockAccess world, IBlockState ibs, BlockPos pos, boolean isSpeciesPartOfMatch)
        {
            this.block = ((SuperBlock)ibs.getBlock());
            //last param = false prevents recursion - we don't need the full model state (which depends on this logic)
            this.matchModelState = this.block.getModelStateAssumeStateIsCurrent(ibs, world, pos, false);
            this.isSpeciesPartOfMatch = isSpeciesPartOfMatch;
        }
        
        @Override 
        public boolean wantsModelState() { return true; }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return testBlock(world, ibs, pos, this.block.getModelStateAssumeStateIsCurrent(ibs, world, pos, false));
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
        {
            return ibs.getBlock() == this.block 
                    && this.matchModelState.doesAppearanceMatch(modelState)
                    && (!this.isSpeciesPartOfMatch || !modelState.hasSpecies() || (this.matchModelState.getSpecies() == modelState.getSpecies()));
        }
    }
    
    /** returns true if NO border should be displayed */
    public static class SuperBlockMasonryMatch implements IBlockTest
    {
        private final SuperBlock block;
        private final int matchSpecies;
        private final BlockPos origin;
        
        @Override 
        public boolean wantsModelState() { return true; }
        
        /** pass in the info for the block you want to match */
        public SuperBlockMasonryMatch(SuperBlock block, int matchSpecies, BlockPos pos)
        {
            this.block = block;
            //last param = false prevents recursion - we don't need the full model state (which depends on this logic)
            this.matchSpecies = matchSpecies;
            this.origin = pos;
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return testBlock(world, ibs, pos, this.block.getModelStateAssumeStateIsCurrent(ibs, world, pos, false));
        }
        
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
        {
            // for masonry blocks, a join indicates that a border IS present
            
            
            boolean isSibling = ibs.getBlock() == this.block && modelState.hasMasonryJoin();
            boolean isMate = isSibling 
                   && matchSpecies == modelState.getSpecies();
            
            // display mortar when against solid superblocks, even if not masonry
            boolean isSolid = ibs.isOpaqueCube() && ibs.getBlock() instanceof SuperBlock;
            
            // no mortar between mates or non-solid superblocks
            if(isMate || !isSolid) return false;
            
            // always mortar if not a sibling
            if(!isSibling) return true;
            
            // between siblings, only mortar on three sides of cube
            // (other sibling will do the mortar on other sides)
            return(pos.getX() == origin.getX() + 1 
                    || pos.getY() == origin.getY() - 1
                    || pos.getZ() == origin.getZ() + 1);
        }       
    }
}
