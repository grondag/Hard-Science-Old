package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.exotic_matter.world.WorldInfo;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.support.MachineControlState.RenderLevel;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction.Action;
import grondag.hard_science.network.client_to_server.PacketMachineStatusAddListener;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.simulator.domain.Domain;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MachineTileEntity extends SuperTileEntity
{
    
    private AbstractMachine machine = null;
    
    /** on client, caches last result from {@link #getDistanceSq(double, double, double)} */
    private double lastDistanceSquared;
 
    /**
     * /!\ Lazy instantiation - do not use directly.
     * Use {@link #clientState()} instead.
     */
    protected MachineClientState clientState = null;
    
    /**
     * On server, next time update should be sent to players.
     * On client, next time keepalive request should be sent to server.
     */
    protected long nextPlayerUpdateMilliseconds = 0;
    
    /**
     * See {@link #getSymbolSprite()}
     */
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite symbolSprite;
    
    /**
     * Convenience for {@link MachineBlock#createNewMachine()}
     */
    protected final AbstractMachine createNewMachine()
    {
        return ((MachineBlock)this.getBlockType()).createNewMachine();
    }
    
    @SideOnly(Side.CLIENT)
    public MachineClientState clientState()
    {
        if(this.clientState == null) this.clientState = new MachineClientState(this);
        return this.clientState;
    }
    
    /**
     * Caches machine instance, and notifies it of loaded tile entity.
     * Will be null during early stages of block placement.
     */
    @Nullable
    public AbstractMachine machine()
    {
        assert !this.world.isRemote : "Attempt to access Machine on client.";
    
        if(this.world.isRemote) return null;
        
        if(this.machine == null && this.world != null && this.pos != null)
        {
            this.machine = ((MachineBlock)this.getBlockType()).machine(this.world, this.pos);
            if(this.machine != null) this.machine.machineTE = this;
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
     * Actual implementation is in block, but this provides
     * convenient access.
     */
    @SideOnly(Side.CLIENT)
    public final TextureAtlasSprite getSymbolSprite()
    {
        if(this.symbolSprite == null)
        {
            if(this.hasWorld())
            {
                this.symbolSprite = ((IMachineBlock)this.getBlockType()).getSymbolSprite();
            }
        }
        return this.symbolSprite;
    }
    
    

    /**
     * Called server-side after machine block has been placed to
     * avoid need for a machine look up and to immediately notify
     * machine of loaded tile entity.
     */
    public void onMachinePlaced(AbstractMachine machine)
    {
        this.machine = machine;
        this.machine.machineTE = this;
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
        if(!this.world.isRemote)
        {
            this.updateRedstonePower();
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        if(this.machine != null) this.machine.machineTE = null;
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
     * Writes the machine state for use in {@link #restoreMachineFromStack(ItemStack, Domain)}.
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
        long time = WorldInfo.currentTimeMillis();
        
        // don't send more frequently than needed
        if(time >= this.nextPlayerUpdateMilliseconds)
        {
            if(Configurator.logMachineActivity) 
                Log.info("MachineTileEntity.notifyServerPlayerWatching: %s sending keepalive packet", this.clientState().machineName);

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
        
        if(this.machine() == null) return;
        
        if(Configurator.logMachineActivity) 
            Log.info("MachineTileEntity.addPlayerListener %s got keepalive packet", this.machine().machineName());
        
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
        this.clientState().handleMachineStatusUpdate(packet);
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
            if(this.clientState().hasRedstoneControl)
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
        this.clientState().lastInViewMillis = WorldInfo.currentTimeMillis();
    }

    public long lastInViewMillis()
    {
        return this.clientState().lastInViewMillis;
    }

    public IItemHandler getItemHandler()
    {
        if(this.machine() != null && this.machine().hasItemStorage())
        {
            return (IItemHandler)this.machine().itemStorage();
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
                && this.machine().hasItemStorage())
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((IItemHandler) this.machine().itemStorage());
        }
        else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY 
                && this.machine().hasFluidStorage())
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast((IFluidHandler) this.machine().fluidStorage());
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.machine() != null)
        {
            return this.machine().hasItemStorage();
        }
        else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.machine() != null)
        {
            return this.machine().hasFluidStorage();
        }
        return super.hasCapability(capability, facing);
    }
}
