package grondag.adversity.niceblock.base;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Output;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CompositeCollisionHandler extends AbstractCollisionHandler
{
    private final AbstractCollisionHandler[] handlers;
    
    private final int keyBitLength;
    private final int shiftBits[];
    private final long bitMasks[];
    
    public CompositeCollisionHandler(Collection<AbstractCollisionHandler> handlers)
    {
        this.handlers = handlers.toArray(new AbstractCollisionHandler[1]);
        this.shiftBits = new int[handlers.size()];
        this.bitMasks = new long[handlers.size()];
        
        int totalBitLength = 0;
        for(int i = 0; i < this.handlers.length; i++)
        {
            shiftBits[i] = totalBitLength;
            bitMasks[i] = (1L << (this.handlers[i].getKeyBitLength() + 1)) - 1;
            totalBitLength += this.handlers[i].getKeyBitLength();
        }
        this.keyBitLength = totalBitLength;
        
        if(this.keyBitLength > 64) 
        {
            Output.getLog().warn("Composite collision handler bit length exceeded.  Collision boxes may be borked.");
        }
    }
    
    @Override
    public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelStateSetValue modelState)
    {
        long key = 0;
        for(int i = 0; i < handlers.length; i++)
        {
            key |= (handlers[i].getCollisionKey(state, worldIn, pos, modelState) << shiftBits[i]);
        }
        return key;
    }
    
    @Override
    public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos, ModelState modelState)
    {
        //TODO 
        // Won't actually get used by superblocks.
        // When niceblocks are retired need to update this to actually handle CSG shapes
        // or more accurately, use this class as the starting point for the collision handle for the CSG shape.
        return modelState.getShape().meshFactory().getCollisionKeyFromModelState(modelState);
   }

    @Override
    public int getKeyBitLength()
    {
        return this.keyBitLength;
    }

    @Override
    public List<RawQuad> getCollisionQuads(long modelKey)
    {
        if(handlers.length == 1) return handlers[0].getCollisionQuads(modelKey);

        if(handlers.length == 0) return java.util.Collections.emptyList();
        
        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<>();
        for(int i = 0; i < handlers.length; i++)
        {
            builder.addAll(handlers[i].getCollisionQuads(modelKey >> shiftBits[i] & bitMasks[i]));
        }
        return builder.build();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        if(handlers.length == 0) return Block.NULL_AABB;

        AxisAlignedBB result = handlers[0].getCollisionBoundingBox(state, worldIn, pos);
        
        if(handlers.length > 1)
        {
            for(int i = 1; i < handlers.length; i++)
            {
                result = result.union(handlers[i].getCollisionBoundingBox(state, worldIn, pos));
            }
        }
        return result;
    }
}
