package grondag.hard_science.machines.base;

import java.util.List;

import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.Log;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.domain.Privilege;
import grondag.hard_science.superblock.block.SuperBlockPlus;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.Chroma;
import grondag.hard_science.superblock.color.Hue;
import grondag.hard_science.superblock.color.Luminance;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Translucency;
import grondag.hard_science.superblock.model.state.WorldLightOpacity;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.varia.BlockSubstance;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class MachineBlock extends SuperBlockPlus
{
    public static final Material MACHINE_MATERIAL = new Material(MapColor.SILVER_STAINED_HARDENED_CLAY) 
    {
        @Override
        public boolean isToolNotRequired() { return true; }

        @Override
        public EnumPushReaction getMobilityFlag() { return EnumPushReaction.BLOCK; }
    };
    
    public final int guiID;
    
    public MachineBlock(String name, int guiID, ModelState modelState)
    {
        super(name, MACHINE_MATERIAL, modelState, modelState.getRenderPassSet().blockRenderMode);
        this.guiID = guiID;
        this.setHarvestLevel(null, 0);
        this.setHardness(1);
    }
    
    /**
     * Used in tile entity or during placement (for non-TE) blocks to create new
     * machine instances that are registered with device manager.
     */
    protected abstract AbstractMachine createNewMachine();

    /**
     * Returns machine instance associated with block at this position.
     * Will create a new machine if the world has this block at the given position.
     * Server-side only.
     */
    @Nullable
    public AbstractMachine machine(World world, BlockPos pos)
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
    
    protected static ModelState creatBasicMachineModelState(TexturePallette decalTex, TexturePallette borderTex)
    {
        ModelState modelState = new ModelState();
        modelState.setShape(ModelShape.MACHINE);
        modelState.setTexture(PaintLayer.BASE, Textures.BLOCK_NOISE_MODERATE);
        modelState.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT));

        modelState.setTexture(PaintLayer.LAMP, Textures.BLOCK_NOISE_SUBTLE);
        modelState.setColorMap(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.PURE_NETURAL, Luminance.EXTRA_DARK));
        
        if(decalTex != null)
        {
            modelState.setTexture(PaintLayer.MIDDLE, decalTex);
            modelState.setColorMap(PaintLayer.MIDDLE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.PURE_NETURAL, Luminance.BRILLIANT));
            modelState.setTranslucent(PaintLayer.MIDDLE, true);
            modelState.setTranslucency(Translucency.CLEAR);
        }
        
        if(borderTex != null)
        {
            modelState.setTexture(PaintLayer.OUTER, borderTex);
            modelState.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.GREY, Luminance.MEDIUM_DARK));
        }
        return modelState;
    }
    
    @Override
    public int damageDropped(IBlockState state)
    {
        // don't want species to "stick" with machines - is purely cosmetic
        return 0;
    }
    
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        TileEntity myTE = worldIn.getTileEntity(pos);
        if(myTE != null && myTE instanceof MachineTileEntity)
        {
            ((MachineTileEntity)myTE).updateRedstonePower();
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        // for simple blocks without a container, activation is client-only
        if (!world.isRemote || this.guiID < 0) 
        {
            return true;
        }
        
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof MachineTileEntity)) 
        {
            return false;
        }
        
        player.openGui(HardScience.INSTANCE, this.guiID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    /**
     * Default machine implementation only handles single-block machines.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!(worldIn == null || worldIn.isRemote))
        {
            if(Configurator.logDeviceChanges)
                Log.info("MachineBlock.breakBlock: @ %d.%d.%d in dim %d", 
                        pos.getX(), pos.getY(), pos.getZ(), worldIn.provider.getDimension());
            
            IDevice device = DeviceManager.getDevice(worldIn, pos);
            DeviceManager.removeDevice(device);
        }

        // NB: device removal and any tile entity updates should come BEFORE this call
        super.breakBlock(worldIn, pos, state);
    }

    protected float hitX (EnumFacing side, float hitX, float hitZ)
    {
        switch (side) 
        {
            case NORTH:
                return 1 - hitX;
                
            case SOUTH:
                return hitX;
                
            case WEST:
                return hitZ;
                
            case EAST:
                return 1 - hitZ;
                
            default:
                return 0;
        }
    }
    
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if (worldIn.isRemote) return;

        MachineTileEntity machineTE = (MachineTileEntity) getTileEntityReliably(worldIn, pos);
        
        if(machineTE.machine() == null) return;
        
        if(!machineTE.machine().hasOnOff() && !machineTE.machine().hasRedstoneControl()) return;

        RayTraceResult rayResult = net.minecraftforge.common.ForgeHooks.rayTraceEyes(playerIn, 
                ((EntityPlayerMP) playerIn).interactionManager.getBlockReachDistance() + 1);
        
        if (rayResult == null) return;



        EnumFacing side = rayResult.sideHit;
        if (machineTE.getCachedModelState().getAxisRotation().horizontalFace != side) return;

        // translate to block frame of reference
        float hitX = (float)(rayResult.hitVec.x - pos.getX());
        float hitY = (float)(rayResult.hitVec.y - pos.getY());
        float hitZ = (float)(rayResult.hitVec.z - pos.getZ());
        
        // translate to 2d face coordinates
        float faceX = this.hitX(side, hitX, hitZ);
        float faceY = 1f - hitY;

        if(machineTE.clientState().hasOnOff && RenderBounds.BOUNDS_ON_OFF.contains(faceX, faceY))
        {
            if(machineTE.togglePower((EntityPlayerMP) playerIn))
            {
                worldIn.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, .2f, ((worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * .7f + 1) * 2);
            }
        }
        else if(machineTE.clientState().hasRedstoneControl && RenderBounds.BOUNDS_REDSTONE.contains(faceX, faceY))
        {
            if(machineTE.toggleRedstoneControl((EntityPlayerMP) playerIn))
            {
                worldIn.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, .2f, ((worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * .7f + 1) * 2);
            }
        }
      

    }
    
    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
    {
        return true;
    }

    @Override
    public boolean addHitEffects(IBlockState blockState, World world, RayTraceResult target, ParticleManager manager)
    {
        return true;
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles)
    {
        return true;
    }

    /**
     * On server-side after machine block has been placed will
     * restore machine state from stack (if present) or 
     * create new machine state and register with simulation.<p>
     * 
     * Relies on machine state to be saved by {@link #writeModNBT(NBTTagCompound)}
     * for harvested machines.<p>
     * 
     * New machines are added to the placer's domain. Machines with an 
     * existing domain are not changed.<p>
     * 
     * Location of the machine is always changed to event parameters.
     * 
     * {@inheritDoc}
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
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
        

        // NB: important super goes after machine is initialized so that
        // machine instance is updated before tile entity tries to access it

        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if(machine.hasFront())
        {
            machine.setFront(this.getModelState(worldIn, pos, true).getAxisRotation().horizontalFace);
        }
        
        TileEntity blockTE = worldIn.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof MachineTileEntity) 
        {
            ((MachineTileEntity)blockTE).onMachinePlaced(machine);
        }

    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        // We allow metadata for machine blocks to support texturing
        // but we only want to show one item in creative search / JEI
        // No functional difference.
        list.add(this.getSubItems().get(0));
    }

    @Override
    protected List<ItemStack> createSubItems()
    {
        List<ItemStack> items = super.createSubItems();
        
        MachinePowerSupply defaultPower = createDefaultPowerSupply();
        if(defaultPower != null)
        {
            for(ItemStack stack : items)
            {
                NBTTagCompound tag = stack.getTagCompound();
                if(tag == null)
                {
                    tag = new NBTTagCompound();
                }
                defaultPower.serializeNBT(tag);
                stack.setTagCompound(tag);
            }
        }
        return items;
    }
    
    public abstract MachinePowerSupply createDefaultPowerSupply();
    
    @Override
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return BlockSubstance.MACHINE;
    }

    @Override
    public boolean isGeometryFullCube(IBlockState state)
    {
        return true;
    }

    @Override
    public boolean isHypermatter()
    {
        return false;
    }

    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state)
    {
        return WorldLightOpacity.SOLID;
    }
    
//    @Override
//    public EnumBlockRenderType getRenderType(IBlockState iBlockState)
//    {
//      return EnumBlockRenderType.INVISIBLE;
//    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
    {
        //NOOP for now on machines - don't want all the stuff we get for normal superblocks
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
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
