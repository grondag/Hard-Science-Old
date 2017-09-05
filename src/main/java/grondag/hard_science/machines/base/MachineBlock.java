package grondag.hard_science.machines.base;

import java.util.List;

import grondag.hard_science.HardScience;
import grondag.hard_science.superblock.block.SuperBlockPlus;
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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity myTE = worldIn.getTileEntity(pos);
        if(myTE != null && myTE instanceof MachineTileEntity)
        {
            ((MachineTileEntity)myTE).disconnect();
        }
        super.breakBlock(worldIn, pos, state);
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
        if (world.isRemote || this.guiID < 0) {
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

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        
        // notify re-placed machines to reconnect to simulation or initialize transient state
        if(worldIn.isRemote) return;
        TileEntity blockTE = worldIn.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof MachineTileEntity) 
        {
            ((MachineTileEntity)blockTE).reconnect();
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
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return BlockSubstance.DURASTONE;
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
        //NOOP for now on machines - don't want all the stuff we get for normal superblocks
    }
    
    
}
