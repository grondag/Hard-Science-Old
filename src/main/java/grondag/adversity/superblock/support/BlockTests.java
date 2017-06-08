package grondag.adversity.superblock.support;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class BlockTests
{
    
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
