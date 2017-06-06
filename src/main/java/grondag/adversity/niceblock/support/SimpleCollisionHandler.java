//package grondag.adversity.niceblock.support;
//
//import java.util.List;
//
//import grondag.adversity.library.model.quadfactory.RawQuad;
//import grondag.adversity.niceblock.base.ModelFactory;
//import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
//import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
//
//public class SimpleCollisionHandler extends AbstractCollisionHandler
//{
//    protected final ModelFactory modelFactory;
//   
//    public SimpleCollisionHandler(ModelFactory modelFactory)
//    {
//        this.modelFactory = modelFactory;
//    }
//
// 
//    @Override
//    public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelStateSetValue modelState)
//    {
//        //NB: can't use modelState key directly - it may be a superset of this model's state
//        return this.modelFactory.getStateSet().computeKey(modelState);
//    }
//    
//    @Override
//    public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelState modelState)
//    {
//        return 0;
////        return modelState.getShape().meshFactory().collisionKeyFromModelState(modelState);
//    }
//
//    @Override
//    public int getKeyBitLength()
//    {
//        return this.modelFactory.getStateSet().bitLength;
//    }
//    
//     @Override
//    public List<RawQuad> getCollisionQuads(long modelKey)
//    {
//        return modelFactory.getCollisionQuads(this.modelFactory.getStateSet().getSetValueFromKey(modelKey));
//    }
//
//    @Override
//    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
//    {
//        return modelFactory.getCollisionBoundingBox(state, worldIn, pos);
//    }
//    
//    @Override
//    public AxisAlignedBB getRenderBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
//    {
//        return modelFactory.getRenderBoundingBox(state, worldIn, pos);
//    }
//     
//}
