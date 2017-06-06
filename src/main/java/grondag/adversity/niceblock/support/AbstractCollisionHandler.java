package grondag.adversity.niceblock.support;

import java.util.List;

import grondag.adversity.library.cache.longKey.LongSimpleCacheLoader;
import grondag.adversity.library.cache.longKey.LongSimpleLoadingCache;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Implement to provide specialized collision handling to NiceBlock for blocks
 * with non-standard shapes.
 */
public abstract class AbstractCollisionHandler 
{
    protected final LongSimpleLoadingCache<List<AxisAlignedBB>> modelBounds = new LongSimpleLoadingCache<List<AxisAlignedBB>>(new ComplexBoundsLoader(),  0xFFF);
    
    public List<AxisAlignedBB> getCollisionBoxes(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelState modelState)
    {
        return modelBounds.get(getCollisionKey(state, worldIn, pos, modelState));
    }
    
    /**
     * Provides minimal enclosing AABB to be used for collision handling.
     * Will almost always be the same as render bounding box.
     */
    public  abstract AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos);
  
    /**
     * Provides minimal enclosing AABB to be used for rendering. 
     * Will almost always be the same as collision bounding box.
     */
    public  abstract AxisAlignedBB getRenderBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos);

    public abstract long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelState modelState);
    
    public abstract List<RawQuad> getCollisionQuads(long modelKey);
    
    /** for composite keys */
    public abstract int getKeyBitLength();

    
    protected class ComplexBoundsLoader implements LongSimpleCacheLoader<List<AxisAlignedBB>>
    {
        @Override
        public List<AxisAlignedBB> load(long key)
        {
            return CollisionBoxGenerator.makeCollisionBoxList(getCollisionQuads(key));
        }
    }

}
