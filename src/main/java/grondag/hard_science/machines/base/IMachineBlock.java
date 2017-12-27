package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.domain.Privilege;
import grondag.hard_science.superblock.block.SuperTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import grondag.hard_science.superblock.block.SuperBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public interface IMachineBlock
{
    /**
     * Returns machine instance associated with block at this position.
     * Will create a new machine if the world has this block at the given position.
     * Server-side only.
     */
    @Nullable
    public default AbstractMachine machine(World world, BlockPos pos)
    {
        assert !world.isRemote : "Attempt to access Machine on client.";
    
        if(world.isRemote) return null;
        
        IDevice result = DeviceManager.getDevice(world, pos);
        
        IBlockState blockState = world.getBlockState(pos);
        
        if(blockState.getBlock() == this)
        {
            // should have a device - so create one if not found
            if(result == null)
            {
                if(Configurator.logDeviceChanges)
                    Log.info("MachineBlock.machine creating new machine: @ %d.%d.%d in dim %d", 
                            pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());

                result = this.createNewMachine();
                result.setLocation(pos, world);
                DeviceManager.addDevice(result);
            }
        }
        else
        {
            assert result == null : "Device found at location without matching block";
        }
                
        return (AbstractMachine) result;
    }
    
    /**
     * Used to create new machine instances appropriate for this block.
     */
    public AbstractMachine createNewMachine();
    
    /**
     * Handler for block placement - call this from Block#OnBlockPlacedBy
     * BEFORE calling super.
     */
    public default void handleOnBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if(worldIn.isRemote) return;
        
        if(Configurator.logDeviceChanges)
            Log.info("MachineBlock.onBlockPlacedBy: @ %d.%d.%d in dim %d", 
                    pos.getX(), pos.getY(), pos.getZ(), worldIn.provider.getDimension());

        // restore placed machines or initialize them with simulation
       
        AbstractMachine machine = this.machine(worldIn, pos);
        
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
            Domain domain = DomainManager.instance().getActiveDomain((EntityPlayerMP) placer);
            if(domain == null || !domain.hasPrivilege((EntityPlayer) placer, Privilege.ADD_NODE))
            {
                domain = DomainManager.instance().defaultDomain();
            }
            machine.setDomain(domain);
        }
        
        if(machine.hasFront())
        {
            machine.setFront(((SuperBlock)this).getModelState(worldIn, pos, true).getAxisRotation().horizontalFace);
        }
        
        TileEntity blockTE = worldIn.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof MachineTileEntity) 
        {
            ((MachineTileEntity)blockTE).onMachinePlaced(machine);
        }

    }

    /**
     * Handler for block break - call this from Block#breakBlock
     * BEFORE calling super.
     */
    public default void handleBreakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!(worldIn == null || worldIn.isRemote))
        {
            if(Configurator.logDeviceChanges)
                Log.info("MachineBlock.breakBlock: @ %d.%d.%d in dim %d", 
                        pos.getX(), pos.getY(), pos.getZ(), worldIn.provider.getDimension());
            
            IDevice device = DeviceManager.getDevice(worldIn, pos);
            DeviceManager.removeDevice(device);
        }
    }
    
    /**
     * Call from addProbeInfo
     */
    public default void addMachineProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    { 
        if(world.isRemote) return;
        
        AbstractMachine machine = this.machine(world, data.getPos());

        probeInfo.text(I18n.translateToLocalFormatted("probe.machine.domain", 
                machine.getDomain() == null ? I18n.translateToLocal("misc.unassigned") : machine.getDomain().getName()));
        
        if(machine.blockManager().itemCircuit() != null)
        {
            probeInfo.text(I18n.translateToLocalFormatted("probe.machine.item_transport", 
                    machine.blockManager().itemCircuit().carrierAddress()));
        }
        
        if(machine.blockManager().powerCircuit() != null)
        {
            probeInfo.text(I18n.translateToLocalFormatted("probe.machine.power_transport", 
                    machine.blockManager().powerCircuit().carrierAddress()));
        }

    }
}
