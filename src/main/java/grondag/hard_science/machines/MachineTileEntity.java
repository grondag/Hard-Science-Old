package grondag.hard_science.machines;

import grondag.hard_science.library.varia.Useful;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MachineTileEntity extends TileEntity
{
    
    public static enum ControlMode
    {
        ON,
        OFF,
        ON_WITH_REDSTOWN,
        OFF_WITH_REDSTONE;
    }
    
    private ControlMode controlMode = ControlMode.OFF_WITH_REDSTONE;
    private boolean hasRedstonePowerSignal = false;
    
    /**
     * Saves state to the stack.<br>
     * Data should be stored in the server-side tag defined in MachineItemBlock unless will sent to client.
     * Restored if stack is placed again using {@link #restoreStateFromStackAndReconnect(ItemStack)}.<br>
     * Should only be called server side.
     */
    public void saveStateInStack(ItemStack stack, NBTTagCompound serverSideTag)
    {
        if(world.isRemote) return;
        serverSideTag.setInteger("controlMode", this.controlMode.ordinal());
    }
    
    
    /**
     * Disconnects TE from simulation.<br>
     * Call when block is broken.<br>
     * Should only be called server side.
     */
    public void disconnect() {};
    
    /**
     * Restores TE to state when was broken, reconnecting it to simulation if needed.<br>
     * Should only be called server side.
     * @param serverSideTag 
     */
    public void restoreStateFromStackAndReconnect(ItemStack stack, NBTTagCompound serverSideTag)
    {
        if(this.world == null || this.world.isRemote) return;
        
        if(serverSideTag == null) return;

        int controlOrdinal = serverSideTag.getInteger("controlMode");
        
        this.setControlMode(Useful.safeEnumFromOrdinal(controlOrdinal, ControlMode.OFF_WITH_REDSTONE));
    }


    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return true;
    }


    public ControlMode getControlMode()
    {
        return controlMode;
    }

    public void setControlMode(ControlMode controlMode)
    {
        this.controlMode = controlMode == null ? ControlMode.ON : controlMode;
        this.markDirty();
    }
    
    public boolean isOn()
    {
        switch(this.controlMode)
        {
        case ON:
            return true;
            
        case OFF_WITH_REDSTONE:
            return !this.hasRedstonePowerSignal;
            
        case ON_WITH_REDSTOWN:
            return this.hasRedstonePowerSignal;
            
        case OFF:
        default:
            return false;
        
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        this.updateRedstonePower();
    }


    public boolean hasRedstonePowerSignal()
    {
        return hasRedstonePowerSignal;
    }

    public void updateRedstonePower()
    {
        if(this.isRemote()) return;
        
        this.hasRedstonePowerSignal = this.world.isBlockPowered(this.pos);
    }
    
    /** true if tile entity operating on logical client */
    public boolean isRemote()
    {
        return this.world == null 
                ? FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
                : this.world.isRemote;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(this.isRemote()) return;
        this.setControlMode(Useful.safeEnumFromOrdinal(compound.getInteger("ControlMode"), ControlMode.OFF_WITH_REDSTONE));
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if(this.isRemote()) return compound;
        compound.setInteger("ControlMode", this.controlMode.ordinal());
        return compound;
    }
    
}
