package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.library.varia.SimpleUnorderedArraySet;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.support.IMachinePowerProvider;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.MachineControlState.ControlMode;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.MachineControlState.RenderLevel;
import grondag.hard_science.machines.support.MachineStatusState;
import grondag.hard_science.machines.support.MaterialBuffer;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction.Action;
import grondag.hard_science.network.client_to_server.PacketMachineStatusAddListener;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.simulator.wip.AssignedNumber;
import grondag.hard_science.simulator.wip.IIdentified;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.KeyedTuple;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
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

    private MachineControlState controlState = new MachineControlState();
    protected MachineStatusState statusState = new MachineStatusState();
    
    /** Levels of materials stored in this machine.  Is persisted to NBT. 
     * Pass null to constructor to disable. 
     */
    @Nullable
    private MaterialBufferManager bufferManager;
    
    
    /**
     * Power provider for this machine, if it has one.
     */
    @Nullable
    private IMachinePowerProvider powerProvider;
    
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

    
    /**
     * For use by TESR - cached items stack based on status info.
     */
    protected ItemStack statusStack;
    
    /**
     * For use by TESR - last time player looked at this machine within the machine rendering distance
     */
    @SideOnly(Side.CLIENT)
    public long lastInViewMillis;
    
    /**
     * Set to PolyEthylene buffer in subclass constructor if this machine uses PE.  
     * Sent to power provider during update if this machine has one.
     */
    protected MaterialBuffer peBuffer;
    
    private class PlayerListener extends KeyedTuple<EntityPlayerMP>
    {
        /** Listener is valid until this time*/
        private long goodUntilMillis;
        
        private PlayerListener(EntityPlayerMP key)
        {
            super(key);
            this.goodUntilMillis = CommonProxy.currentTimeMillis() + Configurator.Machines.machineKeepAlivePlusLatency;
        }
        
        /**
         * Returns true if this listener should be removed because it has timed out or player has disconnected.
         */
        private boolean checkForRemoval(long currentTime)
        {
            return this.key.hasDisconnected()
                    || (currentTime > this.goodUntilMillis && !isOpenContainerForPlayer(this.key));
        }
    }

    /**
     * Make false to disable on/off switch.
     */
    public boolean hasOnOff() { return true;}
    
    /**
     * Make false to disable redstone control.
     */
    public boolean hasRedstoneControl() { return true; }
    
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
        this.controlState.hasMaterialBuffer(bufferManager != null);
    }
    
    /** 
     * If this tile has a power provider, gives access.  Null if not.
     * Used to serialize/deserialize on client.
     */
    public final @Nullable IMachinePowerProvider getPowerProvider()
    {
        return this.powerProvider;
    }
    
    protected final void setPowerProvider(@Nullable IMachinePowerProvider powerProvider)
    {
        this.powerProvider = powerProvider;
        this.controlState.hasPowerProvider(powerProvider != null);
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
        return pass == 1 && this.getControlState().getRenderLevel() != RenderLevel.NONE;
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
        if(!this.hasOnOff()) return false;
        
            switch(this.getControlState().getControlMode())
            {
            case ON:
                return true;
                
            case OFF_WITH_REDSTONE:
                return !this.hasRedstonePowerSignal();
                
            case ON_WITH_REDSTONE:
                return this.hasRedstonePowerSignal();
                
            case OFF:
            default:
                return false;
            
            }
    }
    
    public boolean isRedstoneControlEnabled()
    {
        if(!this.hasRedstoneControl()) return false;
        
        switch(this.getControlState().getControlMode())
        {
        case OFF_WITH_REDSTONE:
        case ON_WITH_REDSTONE:
            return true;
            
        case ON:
        case OFF:
        default:
            return false;
        }
        
    }

    public boolean hasBacklog()
    {
        return this.statusState.hasBacklog();
    }
    
    /**
     * Max backlog depth since machine was last idle or power cycled.
     * Automatically maintained y {@link #setCurrentBacklog(int)}
     */
    public int getMaxBacklog()
    {
        return this.hasBacklog() ? this.statusState.getMaxBacklog() : 0;
    }
    
    public int getCurrentBacklog()
    {
        return this.hasBacklog() ? this.statusState.getCurrentBacklog() : 0;
    }
    
    public void setCurrentBacklog(int value)
    {
        if(!this.hasBacklog()) return;
        
        if(value != this.statusState.getCurrentBacklog())
        {
            this.statusState.setCurrentBacklog(value);
            this.markPlayerUpdateDirty(false);
        }
        
        int maxVal = Math.max(value, this.getMaxBacklog());
        if(value == 0) maxVal = 0;
        
        if(maxVal != this.getMaxBacklog())
        {
            this.statusState.setMaxBacklog(maxVal);
            this.markPlayerUpdateDirty(false);
        }        
    }
    
    public boolean hasJobTicks()
    {
        return this.getControlState().hasJobTicks();
    }
    
    public int getJobDurationTicks()
    {
        return this.getControlState().getJobDurationTicks();
    }
    
    public int getJobRemainingTicks()
    {
        return this.getControlState().getJobRemainingTicks();
    }
    
    public MachineState getMachineState()
    {
        return this.getControlState().getMachineState();
    }
        
    /**
     * For use by TESR - cached items stack based on status info.
     * Assumes that the target block is a superModel block.
     */
    public ItemStack getStatusStack()
    {
        ItemStack result = this.statusStack;
        if(result == null && this.getControlState().hasModelState())
        {
            ModelState modelState = this.getControlState().getModelState();
            if(modelState == null) return null;
            
            SuperModelBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(this.getControlState().getSubstance(), this.getControlState().getModelState());
            result = newBlock.getSubItems().get(0);
            SuperItemBlock.setStackLightValue(result, this.getControlState().getLightValue());
            SuperItemBlock.setStackSubstance(result, this.getControlState().getSubstance());
            SuperItemBlock.setStackModelState(result, this.getControlState().getModelState());
            this.statusStack = result;
        }
        return result;
    }
    
    @Override
    public void onLoad()
    {
        super.onLoad();
        this.updateRedstonePower();
    }


    public boolean hasRedstonePowerSignal()
    {
        return this.statusState.hasRedstonePower();
    }

    public void updateRedstonePower()
    {
        if(this.isRemote()) return;
        boolean shouldBePowered = this.world.isBlockPowered(this.pos);
        if(shouldBePowered != this.statusState.hasRedstonePower())
        {
            this.statusState.setHasRestonePower(shouldBePowered);
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
        this.getControlState().deserializeNBT(compound);
        if(this.powerProvider != null) this.powerProvider.deserializeNBT(compound);
        if(this.bufferManager != null) this.bufferManager.deserializeNBT(compound);
    }
    
    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        this.serializeID(compound);
        this.getControlState().serializeNBT(compound);
        if(this.powerProvider != null) this.powerProvider.serializeNBT(compound);
        if(this.bufferManager != null) this.bufferManager.serializeNBT(compound);
    }
    
    /**
     * If isRequired == true, will send client updates more frequently.
     * Does not downgrade player to non-focused if previously added as focused.
     * Will cause an immediate player refresh if the listener is new or
     * if existing listener goes from non-urgent to urgent.
     */
    public void addPlayerListener(EntityPlayerMP player)
    {
        if(world.isRemote) return;
        
        if(Configurator.logMachineNetwork) Log.info("got keepalive packet");
        PlayerListener listener = new PlayerListener(player);
        
        if(this.listeningPlayers == null)
        {
            this.listeningPlayers = new SimpleUnorderedArraySet<PlayerListener>();
            this.listeningPlayers.put(listener);
            
            if(Configurator.logMachineNetwork) Log.info("added new listener");
            
            // send immediate refresh for any new listener
            ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
            
        }
        else
        {
            PlayerListener previous = this.listeningPlayers.put(listener);
            
            if(previous == null)
            {
                // send immediate refresh for any new listener
                ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);

                if(Configurator.logMachineNetwork) Log.info("added new listener");

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
     * If isUrgent == true will send update on next tick.
     * isUrgent should be used for big visible changes, like power on/off<br><br>
     * 
     * If {@link #playerUpdateMilliseconds} has been set to a negative value, will have 
     * no effect unless isUrgent == true. <br><br>
     * 
     * Seldom called directly except with required updates because is 
     * called automatically by {@link #markDirty()}
     */
    public void markPlayerUpdateDirty(boolean isUrgent)
    {
        if(this.listeningPlayers == null || this.listeningPlayers.isEmpty()) return;
        
        this.isPlayerUpdateNeeded = true;
        if(isUrgent)
        {
            this.nextPlayerUpdateMilliseconds = 0;
        }
    }
    
    
    @SideOnly(Side.CLIENT)
    public void notifyServerPlayerWatching()
    {
        long time = CommonProxy.currentTimeMillis();
        
        // don't send more frequently than needed, but send immediately if upgrading to urgent
        if(time >= this.nextPlayerUpdateMilliseconds)
        {
            if(Configurator.logMachineNetwork) Log.info("sending keepalive packet");

            ModMessages.INSTANCE.sendToServer(new PacketMachineStatusAddListener(this.pos));
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
        if(world.isRemote) return;
        
        if(this.controlState.hasPowerProvider())
            this.getPowerProvider().tick(this.peBuffer);
        
        if(this.isPlayerUpdateNeeded && this.listeningPlayers != null && !this.listeningPlayers.isEmpty())
        {
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
                        if(Configurator.logMachineNetwork) Log.info("Removed timed out listener");
                    }
                    else
                    {
                        if(Configurator.logMachineNetwork) Log.info("Sending update packet due to change");
                        ModMessages.INSTANCE.sendTo(packet, listener.key);
                        i++;
                    }
                }
                
                this.nextPlayerUpdateMilliseconds = time + Configurator.MACHINES.machineUpdateIntervalMilliseconds;
                this.isPlayerUpdateNeeded = false;
            }
        }
    }
    
    public PacketMachineStatusUpdateListener createMachineStatusUpdate()
    {
        return new PacketMachineStatusUpdateListener(this);
    }
    
    /**
     * Handles client status updates received from server.
     */
    public void handleMachineStatusUpdate(PacketMachineStatusUpdateListener packet)
    {
        // should never be called on server
        if(!this.world.isRemote) return;
        
        this.setControlState(packet.controlState);
        this.statusState = packet.statusState;
        this.statusStack = null;

        if(this.controlState.hasMaterialBuffer())
            this.getBufferManager().deserializeFromArray(packet.materialBufferData);

        if(this.controlState.hasPowerProvider())
            this.getPowerProvider().deserializeFromArray(packet.powerProviderData);
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
     * Returns false if denied.
     */
    public boolean togglePower(EntityPlayerMP player)
    {
        if(!this.hasWorld() || this.isInvalid() || !this.hasOnOff()) return false;
        
        if(this.world.isRemote)
        {
            // send to server
            ModMessages.INSTANCE.sendToServer(new PacketMachineInteraction(Action.TOGGLE_POWER, this.pos));
            return true;
        }
        else
        {
            // clear backlog on power cycle if we have one
            this.setCurrentBacklog(0);
            
            //FIXME: check user permissions
            
            // called by packet handler on server side
            switch(this.getControlState().getControlMode())
            {
            case OFF:
                this.getControlState().setControlMode(ControlMode.ON);
                break;
                
            case OFF_WITH_REDSTONE:
                this.getControlState().setControlMode(ControlMode.ON_WITH_REDSTONE);
                break;
                
            case ON:
                this.getControlState().setControlMode(ControlMode.OFF);
                break;
                
            case ON_WITH_REDSTONE:
                this.getControlState().setControlMode(ControlMode.OFF_WITH_REDSTONE);
                break;
                
            default:
                break;
                
            }
            this.markDirty();
            this.markPlayerUpdateDirty(true);
            return true;
        }
    }
    
    /**
     * Handles packet from player to toggle redstone control on or off.
     * Returns false if denied.
     */
    public boolean toggleRedstoneControl(EntityPlayerMP player)
    {
        if(!this.hasWorld() || this.isInvalid() || !this.hasRedstoneControl()) return false;
        
        if(this.world.isRemote)
        {
            // send to server
            ModMessages.INSTANCE.sendToServer(new PacketMachineInteraction(Action.TOGGLE_REDSTONE_CONTROL, this.pos));
            return true;
        }
        else
        {
            //FIXME: check user permissions
            
            // called by packet handler on server side
            switch(this.getControlState().getControlMode())
            {
            case OFF:
                this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.OFF_WITH_REDSTONE : ControlMode.ON_WITH_REDSTONE);
                break;
                
            case OFF_WITH_REDSTONE:
                this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.OFF : ControlMode.ON);
                break;
                
            case ON:
                this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.ON_WITH_REDSTONE : ControlMode.OFF_WITH_REDSTONE);
                break;
                
            case ON_WITH_REDSTONE:
                this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.ON : ControlMode.OFF);
                break;
                
            default:
                break;
                
            }
            this.markDirty();
            this.markPlayerUpdateDirty(true);
            return true;
        }
    }

    /**
     * Called when player is looking directly at this machine, within machine rendering distance.
     */
    @SideOnly(Side.CLIENT)
    public void notifyInView()
    {
        this.lastInViewMillis = CommonProxy.currentTimeMillis();
    }

    public MachineControlState getControlState()
    {
        return this.controlState;
    }
    
    private void setControlState(MachineControlState controlState)
    {
        this.controlState = controlState;
    }
    
    public MachineStatusState getStatusState()
    {
        return this.statusState;
    }
}
