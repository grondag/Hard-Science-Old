package grondag.adversity.niceblock.support;

import java.util.List;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SimpleCollisionHandler extends AbstractCollisionHandler
{
    protected final ModelFactory<?> modelFactory;
    protected final ModelStateSet modelStateSet;
   
    public SimpleCollisionHandler(ModelFactory<?> modelFactory)
    {
        this.modelFactory = modelFactory;
        this.modelStateSet = ModelStateSet.find(modelFactory.getStateGroup());
    }

 
    @Override
    public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelStateSetValue modelState)
    {
        return modelState.getKey();
    }

    @Override
    public int getKeyBitLength()
    {
        return this.modelStateSet.bitLength;
    }
    
     @Override
    public List<RawQuad> getCollisionQuads(long modelKey)
    {
        return modelFactory.getCollisionQuads(modelStateSet.getSetValueFromKey(modelKey));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return modelFactory.getCollisionBoundingBox(state, worldIn, pos);
    }
     
     
}
