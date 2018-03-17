package grondag.hard_science.superblock.varia;

import javax.annotation.Nullable;

import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Convenience methods for SuperBlock and subclasses
 */
public class SuperBlockHelper
{
    /**
     * returns null if not a superblock at the position
     */
    public static ModelState getModelStateIfAvailable(IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof SuperBlock)
        {
            return ((SuperBlock)state.getBlock()).getModelStateAssumeStateIsCurrent(state, world, pos, refreshFromWorldIfNeeded);
        }
        return null;
    }
    
    /**
     * Returns species at position if it could join with the given block/modelState
     * Returns -1 if no superblock at position or if join not possible.
     */
    public static int getJoinableSpecies(IBlockAccess world, BlockPos pos, @Nullable IBlockState withBlockState, @Nullable ModelState withModelState)
    {
        if(withBlockState == null || withModelState == null) return -1;
        
        if(!withModelState.hasSpecies()) return -1;
        
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock() == withBlockState.getBlock())
        {
            ModelState mState = getModelStateIfAvailable(world, pos, false);
            if(mState == null) return -1;
            
            if(mState.doShapeAndAppearanceMatch(withModelState)) return mState.getSpecies();
        }
        return -1;
    }
}
