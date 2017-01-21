package grondag.adversity.niceblock.support;

import java.util.List;

import grondag.adversity.library.cache.ManagedLoadingCache;
import grondag.adversity.library.cache.SimpleCacheLoader;
import grondag.adversity.library.model.quadfactory.RawQuad;
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
    protected final ManagedLoadingCache<List<AxisAlignedBB>> modelBounds = new ManagedLoadingCache<List<AxisAlignedBB>>(new ComplexBoundsLoader(), 0xF, 0xFFF);
    
//    protected final ManagedLoadingCache<AxisAlignedBB> collisionBounds = new ManagedLoadingCache<AxisAlignedBB>(new CollisionBoundsLoader(), 0xF, 0xFFF);

    public List<AxisAlignedBB> getCollisionBoxes(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return modelBounds.get(getCollisionKey(state, worldIn, pos));
    }
    
    public  abstract AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos);
  

    public abstract long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos);
    
    public abstract List<RawQuad> getCollisionQuads(long modelKey);
    
    /** for composite keys */
    public abstract int getKeyBitLength();

    
    protected class ComplexBoundsLoader implements SimpleCacheLoader<List<AxisAlignedBB>>
    {
        @Override
        public List<AxisAlignedBB> load(long key)
        {
            return CollisionBoxGenerator.makeCollisionBoxList(getCollisionQuads(key));
        }
    }
//
//    protected class CollisionBoundsLoader implements SimpleCacheLoader<AxisAlignedBB>
//    {
//        @Override
//        public AxisAlignedBB load(long key)
//        {
//            return CollisionBoxGenerator.makeBoxSimpleMethod(getCollisionQuads(key));
//        }
//    }
}
