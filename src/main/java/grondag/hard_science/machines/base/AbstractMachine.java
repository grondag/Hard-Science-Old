package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MachineControlState.ControlMode;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.machines.support.MachineStatusState;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.MaterialBufferManager.MaterialBufferDelegate;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.device.AbstractDevice;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class AbstractMachine extends AbstractDevice
{
    private MachineControlState controlState = new MachineControlState();
    protected MachineStatusState statusState = new MachineStatusState();
    
    /** Levels of materials stored in this machine.  Is persisted to NBT. 
     * Null (default) means disabled. 
     */
    @Nullable
    private MaterialBufferManager bufferManager = null;
    
    /**
     * Power provider for this machine, if it has one.
     */
    @Nullable
    private MachinePowerSupply powerSupply;
    
    /**
     * Set to PolyEthylene buffer in subclass constructor if this machine uses PE.  
     * Sent to power provider during update if this machine has one.
     */
    protected MaterialBufferDelegate bufferHDPE;
    
    /**
     * If non-null will send notifications.
     */
    public MachineTileEntity machineTE;
    
    public AbstractMachine()
    {
        this.bufferManager = this.createBufferManager();
        this.controlState.hasMaterialBuffer(bufferManager != null);
        this.powerSupply = this.createPowerSuppy();
        this.controlState.hasPowerSupply(powerSupply != null);
    }
    
    /** 
     * If this tile has a material buffer, gives access.  Null if not.
     * Used to serialize/deserialize on client.
     */
    public final @Nullable MaterialBufferManager getBufferManager()
    {
        return this.bufferManager;
    }
    
    /**
     * If this machine has a material buffer, used to create a new instance.
     * May be used on client to create client-side delegate.
     */
    @Nullable
    protected MaterialBufferManager createBufferManager()
    {
        return null;
    }
    
    /** 
     * If this machine has a power provider, gives access.  Null if not.
     * Used to serialize/deserialize on client.
     */
    public final @Nullable MachinePowerSupply getPowerSupply()
    {
        return this.powerSupply;
    }
    
    /**
     * If this machine has a power supply, used to create a new instance.
     * May be used on client to obtain a client-side version.
     */
    protected @Nullable MachinePowerSupply createPowerSuppy()
    {
        return null;
    }
    
    /**
     * Used for visual display.
     * Defaults to max of power supply, or 1 (to help prevent /0) if there isn't one.
     */
    public float maxPowerConsumptionWatts()
    {
        return this.powerSupply == null ? 1 : this.powerSupply.maxPowerOutputWatts(); 
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
            this.markTEPlayerUpdateDirty(false);
        }
        
        int maxVal = Math.max(value, this.getMaxBacklog());
        if(value == 0) maxVal = 0;
        
        if(maxVal != this.getMaxBacklog())
        {
            this.statusState.setMaxBacklog(maxVal);
            this.markTEPlayerUpdateDirty(false);
        }        
    }
    
    /**
     * Handles packet from player to toggle power on or off.
     * Returns false if denied.
     */
    public boolean togglePower(EntityPlayerMP player)
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
        this.setDirty();
        return true;
    }
    
    /**
     * Called when get packet from client.
     */
    public boolean toggleRedstoneControl(EntityPlayerMP player)
    {
        //FIXME: check user permissions
        
        if(!this.hasRedstoneControl()) return false;
        
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
        this.setDirty();
        return true;
    }
    
    public void markTEPlayerUpdateDirty(boolean isUrgent)
    {
        if(this.machineTE != null)
        {
            if(this.machineTE.isInvalid())
            {
                this.machineTE = null;
            }
            else
            {
                this.machineTE.markPlayerUpdateDirty(isUrgent);
            }
        }
    }
    
    public MachineControlState getControlState()
    {
        return this.controlState;
    }
    
    public MachineStatusState getStatusState()
    {
        return this.statusState;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.getControlState().deserializeNBT(tag);
        if(this.powerSupply != null) this.powerSupply.deserializeNBT(tag);
        if(this.bufferManager != null) this.bufferManager.deserializeNBT(tag);
        if(this.hasFront() && tag.hasKey(ModNBTTag.MACHINE_FRONT)) this.setFront(Useful.safeEnumFromTag(tag, ModNBTTag.MACHINE_FRONT, EnumFacing.NORTH));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.getControlState().serializeNBT(tag);
        if(this.powerSupply != null) this.powerSupply.serializeNBT(tag);
        if(this.bufferManager != null) this.bufferManager.serializeNBT(tag);
        if(this.hasFront() && this.getFront() != null) Useful.saveEnumToTag(tag, ModNBTTag.MACHINE_FRONT, this.getFront());
    }
    
    /**
     * Will be called after machine power and material buffers are updated (if applies).
     * Will be called irrespective of power or on/off, so check those if needed.
     * @param tick  current world tick, in case needed
     */
    protected void updateMachine(long tick){}

    @Override
    public void doOnTick()
    {
        super.doOnTick();
        long tick = Simulator.instance().getTick();
        
        if(this.powerSupply != null && this.powerSupply.tick(this, tick)) this.setDirty();
        
        if((tick & 0xF) == 0 && this.isOn()) this.restock();
        
        this.updateMachine(tick);
    };
    
    /**
     * Make false to disable on/off switch.
     */
    public boolean hasOnOff() { return true;}
    
    /**
     * Make false to disable redstone control.
     */
    public boolean hasRedstoneControl() { return true; }
    
    public static boolean computeIsOn(MachineControlState controlState, MachineStatusState statusState)
    {
        switch(controlState.getControlMode())
        {
        case ON:
            return true;
            
        case OFF_WITH_REDSTONE:
            return !statusState.hasRedstonePower();
            
        case ON_WITH_REDSTONE:
            return statusState.hasRedstonePower();
            
        case OFF:
        default:
            return false;
        
        }
    }
    public boolean isOn()
    {
        if(!this.hasOnOff()) return false;
        return computeIsOn(this.getControlState(), this.getStatusState());
    }
    
    public boolean isRedstoneControlEnabled()
    {
        return this.hasRedstoneControl() && this.getControlState().getControlMode().isRedstoneControlEnabled;
    }
    
    public boolean hasRedstonePowerSignal()
    {
        return this.getStatusState().hasRedstonePower();
    }
    
    protected void restock()
    {
        MaterialBufferManager bufferManager = this.getBufferManager();
        
        if(!bufferManager.canRestockAny() || this.machineTE == null) return;
        
        //FIXME: temp hack - should use world from location and check for loaded chunk
        World teWorld = machineTE.getWorld();
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            
            TileEntity tileentity = teWorld.getTileEntity(this.getLocation().offset(face));
            if (tileentity != null)
            {
                // Currently no power cost for pulling in items - would complicate fuel loading for fuel cells.
                // If want to add this will require special handling or machines must get 
                // power some other way to load fuel - seems tedious.
                // Assuming this is covered by the "emergency" power supply that uses ambient energy harvesters.
                IItemHandler capability = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
                if(bufferManager.restock(capability)) this.setDirty();
            }
        }
    }
    
    /**
     * Call when power stops production.
     * Power supply will forgive itself when it successfully provides power.
     */
    protected void blamePowerSupply()
    {
        if(!this.getPowerSupply().isFailureCause())
        {
            this.getPowerSupply().setFailureCause(true);
            this.markTEPlayerUpdateDirty(false);
        }
    }

    /**
     * Returns HDPE material buffer if this machine has one.
     */
    @Nullable
    public MaterialBufferDelegate bufferHDPE()
    {
        return this.bufferHDPE;
    }
    
    /**
     * Override to true if this machine has an orientation.
     * If true, then placement logic will call {@link #setFront(EnumFacing)}
     * right after the machine is created and before it is added 
     * to the device manager.  Will also cause front to be serialized.
     */
    public boolean hasFront() { return false; }
    
    /**
     * See {@link #hasFront()}
     */
    public void setFront(EnumFacing frontFace) {}
    
    /**
     * See {@link #hasFront()}
     */
    public EnumFacing getFront() { return null; }
}