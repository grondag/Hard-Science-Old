package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.library.varia.SimpleUnorderedArraySet;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.MachineControlState.ControlMode;
import grondag.hard_science.machines.support.MachineControlState.RenderLevel;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction.Action;
import grondag.hard_science.network.client_to_server.PacketMachineStatusAddListener;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.simulator.wip.AssignedNumber;
import grondag.hard_science.simulator.wip.IIdentified;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.varia.KeyedTuple;
import jline.internal.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class MachineTileEntity extends SuperTileEntity implements IIdentified
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
   
    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    private final MachineControlState controlState = new MachineControlState();
    
    /** Levels of materials stored in this machine.  Is persisted to NBT. 
     * Pass null to constructor to disable. 
     */
    @Nullable
    private MaterialBufferManager bufferManager;
    
    /**
     * Machine unique ID.  Is persisted if machine is picked and put back by player.
     * Most logic is in IIdentified.
     */
    private int machineID = IIdentified.NO_ID;
    
    /**
     * Note this isn't serialized - it's always derived from machine ID.
     */
    private String machineName = null;
    
    /** on client, caches last result from {@link #getDistanceSq(double, double, double)} */
    private double lastDistanceSquared;
 
    /** players who are looking at this machine and need updates sent to client */
    private SimpleUnorderedArraySet<PlayerListener> listeningPlayers;
    
    /**
     * Set to false to disable outbound status updates (and keepalive packets) except for urgent/focused updates.
     */
    protected boolean isOptionalPlayerUpdateEnabled = true;
    
    /** if > 0 then need to send client packets more frequently */
    private int requiredListenerCount = 0;
    
    /**
     * On server, next time update should be sent to players.
     * On client, next time keepalive request should be sent to server.
     */
    private long nextPlayerUpdateMilliseconds = 0;
    
    /**
     * True if information has changed and players should receive an update.
     * Difference from {@link #playerUpdateTicks} is that {@link #playerUpdateTicks} controls
     * <i>when</i> update occurs. {@link #isPlayerUpdateNeeded} contols <i>if</i> update occurs.
     */
    private boolean isPlayerUpdateNeeded = false;
    
    private class PlayerListener extends KeyedTuple<EntityPlayerMP>
    {
        /** Listener is valid until this time*/
        private long goodUntilMillis;
        
        /** if true, player is focused on this tile and needs frequent updates */
        private boolean isRequired;
        
        private PlayerListener(EntityPlayerMP key, boolean isRequired)
        {
            super(key);
            this.goodUntilMillis = CommonProxy.currentTimeMillis() + Configurator.Machines.machineKeepAlivePlusLatency;
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
                    if(!isOpenContainerForPlayer(this.key))
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

    
    /**
     * True if container currently open by player references this tile entity.
     */
    protected boolean isOpenContainerForPlayer(EntityPlayerMP player)
    {
        Container openContainer = player.openContainer;
        return openContainer != null 
                && openContainer instanceof MachineContainer
                && ((MachineContainer)openContainer).tileEntity() == this;
    }
    

    /** 
     * If this tile has a material buffer, gives access.  Null if not.
     * Used to serialize/deserialize on client.
     */
    public final @Nullable MaterialBufferManager getBufferManager()
    {
        return this.bufferManager;
    }
    
    protected final void setBufferManager(@Nullable MaterialBufferManager bufferManager)
    {
        this.bufferManager = bufferManager;
    }
    
    /**
     * Used by GUI and TESR to draw machine's symbol.
     */
    @SideOnly(Side.CLIENT)
    public abstract int getSymbolGlTextureId();
    
    /**
     * Disconnects TE from simulation.<br>
     * Called when block is broken.<br>
     * Should only be called server side.
     */
    public abstract void disconnect();
    
    /**
     * Called after TE state has been restored.  Server-side only.<br>
     * Use this to reconnect to simulation, or to reinitialize transient state. <br>
     */
    public abstract void reconnect();


    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        // machines always render in translucent pass
        return pass == 1 && this.controlState.getRenderLevel() != RenderLevel.NONE;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public double getMaxRenderDistanceSquared()
    {
        return 1024.0D;
    }
    
    @Override
    public double getDistanceSq(double x, double y, double z)
    {
        if(this.world.isRemote)
        {
            double result = super.getDistanceSq(x, y, z);
            this.lastDistanceSquared = result;
            return result;
        }
        else
        {
            return super.getDistanceSq(x, y, z);
        }
    }
    
    public double getLastDistanceSquared()
    {
        return lastDistanceSquared;
    }
    
    public boolean isOn()
    {
        switch(this.controlState.getControlMode())
        {
        case ON:
            return true;
            
        case OFF_WITH_REDSTONE:
            return !this.hasRedstonePowerSignal();
            
        case ON_WITH_REDSTOWN:
            return this.hasRedstonePowerSignal();
            
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
        return this.controlState.hasRedstonePower();
    }

    public void updateRedstonePower()
    {
        if(this.isRemote()) return;
        boolean shouldBePowered = this.world.isBlockPowered(this.pos);
        if(shouldBePowered != this.controlState.hasRedstonePower())
        {
            this.controlState.setHasRestonePower(shouldBePowered);
            this.markPlayerUpdateDirty(true);
        }
    }
    
    /** true if tile entity operating on logical client */
    public boolean isRemote()
    {
        return this.world == null 
                ? FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
                : this.world.isRemote;
    }
    
    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        this.deserializeID(compound);
        this.controlState.deserializeNBT(compound);
        if(this.bufferManager != null) this.bufferManager.deserializeNBT(compound);
    }
    
    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        this.serializeID(compound);
        this.controlState.serializeNBT(compound);
        if(this.bufferManager != null) this.bufferManager.serializeNBT(compound);
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
        
        if(Configurator.logMachineNetwork) Log.info("got keepalive packet");
        PlayerListener listener = new PlayerListener(player, isFocused);
        
        if(this.listeningPlayers == null)
        {
            this.listeningPlayers = new SimpleUnorderedArraySet<PlayerListener>();
            this.listeningPlayers.put(listener);
            
            if(Configurator.logMachineNetwork) Log.info("added new listener, required=" + isFocused);
            
            // send immediate refresh for any new listener
            ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
            
            if(listener.isRequired) this.requiredListenerCount++;
            
        }
        else
        {
            PlayerListener previous = this.listeningPlayers.put(listener);
            
            if(previous == null)
            {
                // send immediate refresh for any new listener
                ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
                if(listener.isRequired) this.requiredListenerCount++;

                if(Configurator.logMachineNetwork) Log.info("added new listener, required=" + isFocused);

            }
            else
            {
                if(previous.isRequired)
                {
                    if(!listener.isRequired)
                    {
                        if(this.isOpenContainerForPlayer(player)) 
                        {
                            // prevent downgrade if open container
                            listener.isRequired = true;
                            
                            if(Configurator.logMachineNetwork) Log.info("prevented downgrade on listener");

                        }
                        else
                        {
                            // downgrade
                            this.requiredListenerCount--;
                            
                            if(Configurator.logMachineNetwork) Log.info("downgraded required listener");
                        }
                    }
                }
                else
                {
                    if(listener.isRequired)
                    {
                        // upgrade, increment count and send immediate refresh
                        ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
                        this.requiredListenerCount++;
                        
                        if(Configurator.logMachineNetwork) Log.info("upgraded existing listener");
                    }
                }
            }
        }
    }
    
    // not used - just waits for them to time out
//    public void removePlayerListener(EntityPlayerMP player)
//    {
//        if(world.isRemote || this.listeningPlayers == null) return;
//
//        PlayerListener removed = this.listeningPlayers.removeIfPresent(new PlayerListener(player, false));
//        if( removed != null && removed.isRequired) this.requiredListenerCount--;
//    }
    
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
    
    @SideOnly(Side.CLIENT)
    public void notifyServerPlayerWatching()
    {
        long time = CommonProxy.currentTimeMillis();
        if(time >= this.nextPlayerUpdateMilliseconds)
        {
            if(Configurator.logMachineNetwork) Log.info("sending keepalive packet");

            //FIXME: use correct urgency based on render level
            ModMessages.INSTANCE.sendToServer(new PacketMachineStatusAddListener(this.pos, false));
            this.nextPlayerUpdateMilliseconds = time + Configurator.MACHINES.machineKeepaliveIntervalMilliseconds;
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

        long time = CommonProxy.currentTimeMillis();
        
        if(time >= this.nextPlayerUpdateMilliseconds)
        {
            PacketMachineStatusUpdateListener packet = this.createMachineStatusUpdate();
            
            int i = 0;
            while(i < listeningPlayers.size())
            {
                PlayerListener listener = listeningPlayers.get(i);
                
                if(listener.checkForRemoval(time))
                {
                    this.listeningPlayers.remove(i);
                    if(listener.isRequired) this.requiredListenerCount--;
                    if(Configurator.logMachineNetwork) Log.info("Removed timed out listener");
                }
                else
                {
                    if(Configurator.logMachineNetwork) Log.info("Sending update packet due to change");
                    ModMessages.INSTANCE.sendTo(packet, listener.key);
                    i++;
                }
            }
            
            // if we have focused listeners, updates are always 4X per second
            this.nextPlayerUpdateMilliseconds = time + this.requiredListenerCount > 0 ? 250 : Configurator.MACHINES.machineUpdateIntervalMilliseconds;
            this.isPlayerUpdateNeeded = false;
        }
    }
    
    public PacketMachineStatusUpdateListener createMachineStatusUpdate()
    {
        return new PacketMachineStatusUpdateListener(this.pos, this.controlState, 
                this.getBufferManager() == null ? null : this.getBufferManager().serializeToArray());
    }
    
    /**
     * Handles client status updates received from server.
     */
    public void handleMachineStatusUpdate(PacketMachineStatusUpdateListener packet)
    {
        // should never be called on server
        if(!this.world.isRemote) return;
        this.controlState.deserializeFromBits(packet.controlStateBits);
        if(this.getBufferManager() != null) this.getBufferManager().deserializeFromArray(packet.materialBufferData);
    }

    @Override
    public void setId(int id)
    {
        this.machineID = id;
        // force regeneration of name
        this.machineName = null;
        this.markDirty();
    }

    @Override
    public int getIdRaw()
    {
        return this.machineID;
    }

    @Override
    public int getId()
    {
        //disable ID generation on client
        return (this.world == null || this.world.isRemote ? this.getIdRaw() : IIdentified.super.getId());
    }
    
    public String machineName()
    {
        if(this.machineName == null)
        {
            long l = Useful.longHash(this.world.getSeed() ^ this.getId());
            this.machineName = Base32Namer.makeName(l, Configurator.MACHINES.filterOffensiveMachineNames);
        }
        return this.machineName;
    }
    
    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.MACHINE;
    }

    /**
     * Handles packet from player to toggle power on or off.
     */
    public void togglePower(EntityPlayerMP player)
    {
        if(!this.hasWorld() || this.isInvalid()) return;
        
        if(this.world.isRemote)
        {
            // send to server
            ModMessages.INSTANCE.sendToServer(new PacketMachineInteraction(Action.TOGGLE_POWER, this.pos));
        }
        else
        {
            //FIXME: check user permissions
            
            // called by packet handler on server side
            switch(this.controlState.getControlMode())
            {
            case OFF:
                this.controlState.setControlMode(ControlMode.ON);
                break;
                
            case OFF_WITH_REDSTONE:
                this.controlState.setControlMode(ControlMode.ON_WITH_REDSTOWN);
                break;
                
            case ON:
                this.controlState.setControlMode(ControlMode.OFF);
                break;
                
            case ON_WITH_REDSTOWN:
                this.controlState.setControlMode(ControlMode.OFF_WITH_REDSTONE);
                break;
                
            default:
                break;
                
            }
            
        }
        
        
    }
}
