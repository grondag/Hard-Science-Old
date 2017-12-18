package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.support.MachineControlState.RenderLevel;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction.Action;
import grondag.hard_science.network.client_to_server.PacketMachineStatusAddListener;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.superblock.block.SuperTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class MachineTileEntity extends SuperTileEntity
{
    
    /**
     * Machine unique ID.  Is persisted if machine is picked and put back by player.
     * Most logic is in IIdentified.<p>
     * 
     * Initialized to signal value that means hasn't been initialized yet.
     * {@link #machine()} will return null until has been set to something else.
     */
    private int machineID = IIdentified.SIGNAL_ID;
    
    private AbstractMachine machine = null;
    
    /** on client, caches last result from {@link #getDistanceSq(double, double, double)} */
    private double lastDistanceSquared;
 
    protected MachineClientState clientState = null;
    
    /**
     * On server, next time update should be sent to players.
     * On client, next time keepalive request should be sent to server.
     */
    protected long nextPlayerUpdateMilliseconds = 0;
    
    protected abstract AbstractMachine createNewMachine();
    
    @SideOnly(Side.CLIENT)
    public MachineClientState clientState()
    {
        return this.clientState;
    }
    
    /**
     * Used for tear down when block is broken.
     */
    public void clearMachine()
    {
        if(this.machine != null)
        {
            DeviceManager.removeDevice(this.machine);
            this.machine.machineTE = null;
            this.machine = null;
        }
    }
    
    /**
     * Will return null if machine not yet initialized by placement.
     */
    @Nullable
    public AbstractMachine machine()
    {
        assert !this.world.isRemote : "Attempt to access Machine on client.";
    
        if(this.world.isRemote) return null;
        
        if(this.machineID == IIdentified.SIGNAL_ID) return null;
        
        if(this.machine == null)
        {
            if(this.machineID == IIdentified.UNASSIGNED_ID)
            {
                Log.warn("TileEntity @ %s not initialized on placement. New device created.", this.pos.toString());
                this.machine = createNewMachine();
                this.machine.setLocation(this.pos, this.world);
                DeviceManager.addDevice(this.machine);
                this.machineID = this.machine.getId();
            }
            else
            {
                this.machine = (AbstractMachine) DeviceManager.getDevice(this.machineID);
                if(this.machine == null)
                {
                    Log.warn("TileEntity @ %s missing device. New device created. Contents may be lost.", this.pos.toString());
                    this.machine = createNewMachine();
                    this.machine.setLocation(this.pos, this.world);
                    DeviceManager.addDevice(this.machine);
                    this.machineID = this.machine.getId();
                }
            }
            this.machine.machineTE = this;
        }
        return this.machine;
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
     * Used by GUI and TESR to draw machine's symbol.
     */
    @SideOnly(Side.CLIENT)
    public abstract TextureAtlasSprite getSymbolSprite();
    

    /**
     * Called server-side after machine block has been placed to
     * restore machine state from stack (if present) or 
     * create new machine state and register with simulation.<p>
     * 
     * Relies on machine state to be saved by {@link #writeModNBT(NBTTagCompound)}
     * for harvested machines.<p>
     * 
     * New machines are added to the given domain. Machines with an 
     * existing domain are not changed.<p>
     * 
     * Location of the machine is always changed to what is provided.
     */
    public void restoreMachineFromStack(ItemStack stack, Domain defaultDomain)
    {
        AbstractMachine machine = createNewMachine();
        
        if(stack.hasTagCompound())
        {
            NBTTagCompound serverTag = SuperTileEntity.getServerTag(stack.getTagCompound());
            
            if(serverTag.hasKey(ModNBTTag.MACHINE_STATE))
            {
                machine.deserializeNBT(serverTag.getCompoundTag(ModNBTTag.MACHINE_STATE));
            }
        }
        
        if(machine.getDomain() == null)
        {
            machine.setDomain(defaultDomain);
        }
        
        machine.setLocation(this.pos, this.world);
        machine.machineTE = this;
        DeviceManager.addDevice(machine);
        this.machineID = machine.getId();
        this.machine = machine;
        this.updateRedstonePower();
        this.markPlayerUpdateDirty(true);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        // machines always render in translucent pass
        return pass == 1 && this.clientState().controlState.getRenderLevel() != RenderLevel.NONE;
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
    
    /**
     * Returns cached value from {@link #getDistanceSq(double, double, double)}
     * that is called each render pass by TE render dispatcher so that 
     * we don't have to compute again when needed in TESR.
     */
    @SideOnly(Side.CLIENT)
    public double getLastDistanceSquared()
    {
        return lastDistanceSquared;
    }
    
    @Override
    public void onLoad()
    {
        super.onLoad();
        if(this.world.isRemote)
        {
            this.clientState = new MachineClientState(this);
        }
        else
        {
            this.updateRedstonePower();
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
    }

    public void updateRedstonePower()
    {
        if(this.isRemote()) return;
        if(this.machine() == null) return;
        
        boolean shouldBePowered = this.world.isBlockPowered(this.pos);
        if(shouldBePowered != this.machine().statusState.hasRedstonePower())
        {
            this.machine().statusState.setHasRestonePower(shouldBePowered);
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
    
    /**
     * Recovers machine ID so we can find the machine for which this
     * TE is a delegate.  Did not blockPos and world because
     * some machines could be multiblocks.
     * 
     * Note this does NOT attempt to restore the actual machine state.
     * Machine state for machines within the simulation
     * is persisted by the device manager.  The NBT saved
     * in {@link #writeModNBT(NBTTagCompound)} is used by 
     * {@link #restoreMachineFromStack(ItemStack, Domain)}. <p>
     * 
     * {@inheritDoc}
     */
    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        
        // check for key to avoid enabling ID creation when reading blank tag
        this.machineID = compound.hasKey(ModNBTTag.MACHINE_ID)
                ? compound.getInteger(ModNBTTag.MACHINE_ID)
                : IIdentified.SIGNAL_ID;
    }
    
    /**
     * Saves association with device in simulation.<p>
     * 
     * Also writes the machine state for use in {@link #restoreMachineFromStack(ItemStack, Domain)}.
     * Stores it in server-side tag so that large states don't get sent to client.<p>
     * 
     * Machine state stored here is <em>not</em> used in {@link #readModNBT(NBTTagCompound)}.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        if(!this.world.isRemote)
        {
            compound.setInteger(ModNBTTag.MACHINE_ID, this.machineID);
            if(this.machine() != null)
            {
                SuperTileEntity.getServerTag(compound)
                    .setTag(ModNBTTag.MACHINE_STATE, this.machine().serializeNBT());
            }
        }
    }
    
    /**
     * Called to notify observing players of an update to machine status.
     * If isUrgent == true will send update on next tick.
     * isUrgent should be used for big visible changes, like power on/off<br><br>
     * 
     * If {@link #playerUpdateMilliseconds} has been set to a negative value, will have 
     * no effect unless isUrgent == true. <br><br>
     * 
     * Seldom called directly except with required updates because is 
     * called automatically by {@link #markDirty()}<p>
     * 
     * Has no effect unless this TE is tickable.
     */
    public void markPlayerUpdateDirty(boolean isUrgent)
    {
        //NOOP
        //Only implemented in tickable version
    }
    
    @SideOnly(Side.CLIENT)
    public void notifyServerPlayerWatching()
    {
        long time = CommonProxy.currentTimeMillis();
        
        // don't send more frequently than needed
        if(time >= this.nextPlayerUpdateMilliseconds)
        {
            if(Configurator.logMachineNetwork) Log.info("sending keepalive packet");

            ModMessages.INSTANCE.sendToServer(new PacketMachineStatusAddListener(this.pos));
            this.nextPlayerUpdateMilliseconds = time + Configurator.MACHINES.machineKeepaliveIntervalMilliseconds;
        }
    }
    
    /**
     * If isRequired == true, will send client updates more frequently.
     * Does not downgrade player to non-focused if previously added as focused.
     * Will cause an immediate player refresh if the listener is new or
     * if existing listener goes from non-urgent to urgent.<p>
     * 
     * If this TE is not tickable, will simply send an immediate update
     * of current state. Non-tickable machine TEs are assumed to have
     * infrequent display changes.
     */
    public void addPlayerListener(EntityPlayerMP player)
    {
        if(world.isRemote) return;
        
        if(Configurator.logMachineNetwork) Log.info("got keepalive packet");
        
        if(this.machine() == null) return;
        
        // send immediate refresh
        ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
    }
    
    @Override
    public void markDirty()
    {
        assert !this.world.isRemote : "Machine TE mark dirty on client";
        super.markDirty();
        this.markPlayerUpdateDirty(false);
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
        this.clientState.handleMachineStatusUpdate(packet);
    }
    
    /**
     * Handles packet from player to toggle power on or off.
     * Returns false if denied.
     */
    public boolean togglePower(EntityPlayerMP player)
    {
        if(!this.hasWorld() || this.isInvalid() 
                || this.machine() == null || !this.machine().hasOnOff()) return false;
        
        if(this.world.isRemote)
        {
            // send to server
            ModMessages.INSTANCE.sendToServer(new PacketMachineInteraction(Action.TOGGLE_POWER, this.pos));
            return true;
        }
        else
        {
            // called by packet handler on server side
            this.machine().togglePower(player);
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
        if(!this.hasWorld() || this.isInvalid()) return false;
        
        if(this.world.isRemote)
        {
            // send to server
            if(this.clientState.hasRedstoneControl)
            {
                ModMessages.INSTANCE.sendToServer(new PacketMachineInteraction(Action.TOGGLE_REDSTONE_CONTROL, this.pos));
                return true;
            }
            return false;
        }
        else
        {
            // called by packet handler on server side
            if(this.machine() == null && !this.machine().hasRedstoneControl()) return false;
            
            this.machine().toggleRedstoneControl(player);
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
        this.clientState.lastInViewMillis = CommonProxy.currentTimeMillis();
    }

    public long lastInViewMillis()
    {
        return this.clientState.lastInViewMillis;
    }

    public IItemHandler getItemHandler()
    {
        if(this.machine() != null && this.machine() instanceof ItemStorage)
        {
            return (IItemHandler)this.machine();
        }
        return null;
    }
    
    public boolean canInteractWith(EntityPlayer playerIn)
    {
         return !this.isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                && this.machine() != null
                && this.machine() instanceof ItemStorage)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((IItemHandler) this.machine());
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.machine() != null)
        {
            return this.machine() instanceof ItemStorage;
        }
        return super.hasCapability(capability, facing);
    }
    
    /**
     * Convenience method for casting machine reference to storage type.
     * Will be null if this machine is not an Item Storage
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public IStorage<StorageTypeStack> storageMachine()
    {
        return this.machine() != null && this.machine() instanceof ItemStorage 
                ? (IStorage<StorageTypeStack>) this.machine()
                : null;
    }
}
