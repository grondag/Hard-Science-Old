package grondag.adversity.niceblock.base;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CompositeCollisionHandler implements ICollisionHandler
{
    private final ICollisionHandler[] handlers;
    
    private final int keyBitLength;
    private final int shiftBits[];
    private final long bitMasks[];
    
    public CompositeCollisionHandler(List<ICollisionHandler> handlers)
    {
        this.handlers = handlers.toArray(new ICollisionHandler[1]);
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
            Adversity.log.warn("Composite collision handler bit length exceeded.  Collision boxes may be borked.");
        }
    }
    
    @Override
    public long getCollisionKey(IBlockState state, World worldIn, BlockPos pos)
    {
        long key = 0;
        for(int i = 0; i < handlers.length; i++)
        {
            key |= (handlers[i].getCollisionKey(state, worldIn, pos) << shiftBits[i]);
        }
        return key;
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(IBlockState state, World worldIn, BlockPos pos)
    {
        if(handlers.length == 1) return handlers[0].getModelBounds(state, worldIn, pos);

        if(handlers.length == 0) return java.util.Collections.emptyList();
        
        ImmutableList.Builder<AxisAlignedBB> builder = new ImmutableList.Builder<>();
        for(int i = 0; i < handlers.length; i++)
        {
            builder.addAll(handlers[i].getModelBounds(state, worldIn, pos));
        }
        return builder.build();
    }

    @Override
    public int getKeyBitLength()
    {
        return this.keyBitLength;
    }

}
