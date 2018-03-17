package grondag.hard_science.superblock.varia;

import grondag.exotic_matter.world.BlockCorner;
import grondag.exotic_matter.world.FarCorner;
import grondag.exotic_matter.world.IBlockTest;
import grondag.hard_science.machines.base.IMachineBlock;
import grondag.hard_science.simulator.transport.endpoint.IPortLayout;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockTests
{
    
    public static class SuperBlockBorderMatch extends AbstractNonFaceTest
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
        protected boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
        {
            return ibs.getBlock() == this.block 
                    && this.matchModelState.doShapeAndAppearanceMatch(modelState)
                    && (!this.isSpeciesPartOfMatch || !modelState.hasSpecies() || (this.matchModelState.getSpecies() == modelState.getSpecies()));
        }

        @Override
        protected boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return testBlock(world, ibs, pos, this.block.getModelStateAssumeStateIsCurrent(ibs, world, pos, false));
        }
    }
    
    /** returns true if NO border should be displayed */
    public static class SuperBlockMasonryMatch extends AbstractNonFaceTest
    {
        private final SuperBlock block;
        private final int matchSpecies;
        private final BlockPos origin;
        
        /** pass in the info for the block you want to match */
        public SuperBlockMasonryMatch(SuperBlock block, int matchSpecies, BlockPos pos)
        {
            this.block = block;
            //last param = false prevents recursion - we don't need the full model state (which depends on this logic)
            this.matchSpecies = matchSpecies;
            this.origin = pos;
        }
        
        @Override 
        public boolean wantsModelState() { return true; }

        @Override
        protected boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return testBlock(world, ibs, pos, this.block.getModelStateAssumeStateIsCurrent(ibs, world, pos, false));
        }
        
        @Override
        protected boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
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
    
    public static class SuperBlockCableMatch implements IBlockTest<ModelState>
    {
        private final IPortLayout portLayout;
        private final int channel;
        
        /** pass in the info for the block you want to match */
        public SuperBlockCableMatch(IPortLayout portLayout, int channel)
        {
            this.portLayout = portLayout;
            this.channel = channel;
        }
        
        @Override
        public boolean testBlock(EnumFacing face, IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            if(ibs.getBlock() instanceof IMachineBlock)
            {
                IPortLayout otherLayout = ((IMachineBlock)ibs.getBlock())
                        .portLayout(world, pos, ibs);
                int otherChannel = ibs.getValue(SuperBlock.META);
                
                return this.portLayout.couldConnect(face, this.channel, otherLayout, otherChannel);
            }
            return false;
        }

        @Override
        public boolean testBlock(BlockCorner corner, IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return false;
        }

        @Override
        public boolean testBlock(FarCorner corner, IBlockAccess world, IBlockState ibs, BlockPos pos)
        {
            return false;
        }
    }
}
