package grondag.hard_science.machines.base;

import grondag.hard_science.Configurator;
import grondag.hard_science.library.varia.SimpleUnorderedArraySet;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.varia.KeyedTuple;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;

public abstract class MachineTileEntity extends SuperTileEntity
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
    
    
    private class PlayerListener extends KeyedTuple<EntityPlayerMP>
    {
        /** Listener is valid until this time*/
        private long goodUntilMillis;
        
        /** if true, player is focused on this tile and needs frequent updates */
        private boolean isRequired;
        
        private PlayerListener(EntityPlayerMP key, boolean isRequired)
        {
            super(key);
            this.goodUntilMillis = FMLServerHandler.instance().getServer().getCurrentTime() + Configurator.Machines.machineKeepAlivePlusLatency;
            this.isRequired = isRequired;
        }
        
        /**
         * Downgrades status from focused if appropriate and returns true
         * if this listener should be removed because it has timed out or player has disconnected.
         */
        private boolean checkForRemoval(long currentTime)
        {
            if(this.key.hasDisconnected())
            {
                if(this.isRequired) MachineTileEntity.this.requiredListenerCount--;
                return true;
            }
            
            if(currentTime > this.goodUntilMillis)
            {
                // timed out
                if(this.isRequired)
                {
                    // will give a courtesy bump for required listeners
                    // but will downgrade unless player open container is this tile entity
                    
                    this.goodUntilMillis = currentTime + Configurator.MACHINES.machineKeepaliveIntervalMilliseconds;
                    if(this.key.openContainer == null 
                            || !( this.key.openContainer instanceof MachineContainer
                                    && ((MachineContainer)this.key.openContainer).tileEntity() == MachineTileEntity.this))
                    {
                        this.isRequired = false;
                        MachineTileEntity.this.requiredListenerCount--;
                    }
                    return false;
                }
                else
                {
                    // if was not required, can be removed
                    return true;
                }
            }
            return false;
        }
    }
    
    /** players who are looking at this machine and need updates sent to client */
    private SimpleUnorderedArraySet<PlayerListener> listeningPlayers;
    
    /**
     * Set to false to disable outbound status updates (and keepalive packets) except for urgent/focused updates.
     */
    protected boolean isOptionalPlayerUpdateEnabled = true;
    
    /** if > 0 then need to send client packets more frequently */
    private int requiredListenerCount = 0;
    
    /**
     * Next time update should be sent to players.
     */
    private int nextPlayerUpdateMilliseconds = 0;
    
    /**
     * True if information has changed and players should receive an update.
     * Difference from {@link #playerUpdateTicks} is that {@link #playerUpdateTicks} controls
     * <i>when</i> update occurs. {@link #isPlayerUpdateNeeded} contols <i>if</i> update occurs.
     */
    private boolean isPlayerUpdateNeeded = false;

    
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

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        // machines always have both solid and translucent passes, even 
        // though the model itself doesn't require it
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
    
    /**
     * If isRequired == true, will send client updates more frequently.
     * Does not downgrade player to non-focused if previously added as focused.
     * Will cause an immediate player refresh if the listener is new or
     * if existing listener goes from non-urgent to urgent.
     */
    public void addPlayerListener(EntityPlayerMP player, boolean isFocused)
    {
        if(world.isRemote) return;
        
        PlayerListener listener = new PlayerListener(player, isFocused);
        
        if(this.listeningPlayers == null)
        {
            this.listeningPlayers = new SimpleUnorderedArraySet<PlayerListener>();
            this.listeningPlayers.put(listener);
            if(listener.isRequired)
            {
                // force immediate refresh for any new listener
                this.markPlayerUpdateDirty(true);
                this.requiredListenerCount++;
            }
        }
        else
        {
            PlayerListener previous = this.listeningPlayers.put(listener);
            
            // increase focus count or prevent downgrade, whichever applies
            boolean wasFocused = previous != null && previous.isRequired;
            if(listener.isRequired)
            {
                if(!wasFocused) this.requiredListenerCount++;
            }
            else if(wasFocused) listener.isRequired = true;
            
            // force immediate refresh for any new listener or for an upgrade to urgent
            if(previous == null || (listener.isRequired && !wasFocused)) this.markPlayerUpdateDirty(true);
        }
    }
    
    public void removePlayerListener(EntityPlayerMP player)
    {
        if(world.isRemote || this.listeningPlayers == null) return;

        PlayerListener removed = this.listeningPlayers.removeIfPresent(new PlayerListener(player, false));
        if( removed != null && removed.isRequired) this.requiredListenerCount--;
    }
    
    /**
     * Called to notify observing players of an update to machine status.
     * If isRequired == true will send update on next tick, even if no listeners are urgent.
     * isRequired should be used for big visible changes, like power on/off<br><br>
     * 
     * If {@link #playerUpdateMilliseconds} has been set to a negative value, will have 
     * no effect unless isRequired == true. <br><br>
     * 
     * Seldom called directly except with required updates because is 
     * called automatically by {@link #markDirty()}
     */
    public void markPlayerUpdateDirty(boolean isRequired)
    {
        if(world == null || world.isRemote || this.listeningPlayers == null || this.listeningPlayers.isEmpty()) return;
        if(isRequired)
        {
            this.isPlayerUpdateNeeded = true;
            this.nextPlayerUpdateMilliseconds = 0;
        }
        else if(!this.isPlayerUpdateNeeded)
        {
            this.isPlayerUpdateNeeded = this.requiredListenerCount > 0 || this.isOptionalPlayerUpdateEnabled;
        }
    }
    
    @Override
    public void markDirty()
    {
        super.markDirty();
        this.markPlayerUpdateDirty(false);
    }
    
    public void update()
    {
        if(world.isRemote || !this.isPlayerUpdateNeeded
                || this.listeningPlayers == null || this.listeningPlayers.isEmpty()) return;

        long time = FMLServerHandler.instance().getServer().getCurrentTime();
        
        
        if(time >= this.nextPlayerUpdateMilliseconds)
        {
            PacketMachineStatusUpdateListener packet = new PacketMachineStatusUpdateListener();

            //FIXME: add stuff to packet, probably in subclasses via abstract method
            
            int i = 0;
            while(i < listeningPlayers.size())
            {
                PlayerListener listener = listeningPlayers.get(i);
                
                if(listener.checkForRemoval(time))
                {
                    this.listeningPlayers.remove(i);
                    if(listener.isRequired) this.requiredListenerCount--;
                }
                else
                {
                    ModMessages.INSTANCE.sendTo(packet, listener.key);
                    i++;
                }
            }
            
            // if we have focused listeners, updates are always 4X per second
            this.nextPlayerUpdateMilliseconds = time + this.requiredListenerCount > 0 ? 250 : Configurator.MACHINES.machineUpdateIntervalMilliseconds;

        }
    }
    
    /**
     * Handles client status updates received from server.
     */
    public void handleMachineStatusUpdate(PacketMachineStatusUpdateListener packet)
    {
        // should never be called on server
        if(!this.world.isRemote) return;
        
        //FIXEM: handle the packet!
    }
}
