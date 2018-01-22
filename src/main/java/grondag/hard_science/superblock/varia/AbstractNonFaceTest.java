package grondag.hard_science.superblock.varia;

import grondag.hard_science.library.world.BlockCorner;
import grondag.hard_science.library.world.FarCorner;
import grondag.hard_science.library.world.IBlockTest;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Base class for block tests that don't care about facing.
 *
 */
public abstract class AbstractNonFaceTest implements IBlockTest
{
    abstract protected boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState);

    abstract protected boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos);
    
    @Override
    public boolean testBlock(EnumFacing face, IBlockAccess world, IBlockState ibs, BlockPos pos)
    {
        return this.testBlock(world, ibs, pos);
    }
    
    @Override
    public boolean testBlock(EnumFacing face, IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
    {
        return this.testBlock(world, ibs, pos, modelState);
    }

    @Override
    public boolean testBlock(BlockCorner corner, IBlockAccess world, IBlockState ibs, BlockPos pos)
    {
        return this.testBlock(world, ibs, pos);
    }

    @Override
    public boolean testBlock(BlockCorner face, IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
    {
        return this.testBlock(world, ibs, pos, modelState);
    }
    
    @Override
    public boolean testBlock(FarCorner corner, IBlockAccess world, IBlockState ibs, BlockPos pos)
    {
        return this.testBlock(world, ibs, pos);
    }
    
    @Override
    public boolean testBlock(FarCorner face, IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
    {
        return this.testBlock(world, ibs, pos, modelState);
    }
}
