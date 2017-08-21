package grondag.hard_science.library.world;

import grondag.hard_science.Log;
import grondag.hard_science.machines.MachineTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;

public class BasicBuilderTileEntity extends MachineTileEntity implements ITickable
{

    @Override
    public void update()
    {
        Log.info("boop");
    }

    @Override
    public void saveStateInStack(ItemStack stack)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disconnect()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restoreStateFromStackAndReconnect(ItemStack stack)
    {
        // TODO Auto-generated method stub
        
    }

}
