package grondag.adversity.niceblock.base;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import akka.util.Collections;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

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
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state)
    {
        long key = 0;
        for(int i = 0; i < handlers.length; i++)
        {
            key |= (handlers[i].getCollisionKey(worldIn, pos, state) << shiftBits[i]);
        }
        return key;
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        if(handlers.length == 1) return handlers[0].getModelBounds(collisionKey);

        if(handlers.length == 0) return java.util.Collections.emptyList();
        
        ImmutableList.Builder<AxisAlignedBB> builder = new ImmutableList.Builder<>();
        for(int i = 0; i < handlers.length; i++)
        {
            builder.addAll(handlers[i].getModelBounds((collisionKey >> shiftBits[i]) & bitMasks[i]));
        }
        return builder.build();
    }

    @Override
    public int getKeyBitLength()
    {
        return this.keyBitLength;
    }

}
