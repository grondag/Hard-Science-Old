package grondag.adversity.niceblock.base;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import akka.util.Collections;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class CompositeCollisionHandler implements ICollisionHandler
{
    private final List<ICollisionHandler> handlers;
    
    public CompositeCollisionHandler(List<ICollisionHandler> handlers)
    {
        this.handlers = handlers;
    }
    
    // TODO don't need this method
    // can just reference the modelstate key directly from caller
    @Override
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state)
    {
        return ((IExtendedBlockState) state).getValue(NiceBlock.MODEL_STATE).stateValue.getKey();
    }

    // TODO have this accept the model state key directly, vs a separate collision key
    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        if(handlers.isEmpty()) return java.util.Collections.emptyList();
        
        if(handlers.size() == 1) return handlers.get(0).getModelBounds(collisionKey);
        
        ImmutableList.Builder<AxisAlignedBB> builder = new ImmutableList.Builder<>();
        for(ICollisionHandler h : handlers)
        {
            builder.addAll(h.getModelBounds(collisionKey));
        }
        return builder.build();
    }

}
