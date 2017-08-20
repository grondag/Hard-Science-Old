package grondag.hard_science.machines;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MachineTileEntity extends TileEntity
{
    /**
     * Saves state to the stack.<br>
     * Data should be stored in the server-side tag defined in MachineItemBlock unless will sent to client.
     * Restored if stack is placed again using {@link #restoreStateFromStackAndReconnect(ItemStack)}.<br>
     * Should only be called server side.
     */
    public abstract void saveStateInStack(ItemStack stack);
    
    
    /**
     * Disconnects TE from simulation.<br>
     * Call when block is broken.<br>
     * Should only be called server side.
     */
    public abstract void disconnect();
    
    /**
     * Restores TE to state when was broken, reconnecting it to simulation if needed.<br>
     * Should only be called server side.
     */
    public abstract void restoreStateFromStackAndReconnect(ItemStack stack);


    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return true;
    }
    
    
    
}
