package grondag.hard_science.machines.base;

import java.util.List;

import grondag.hard_science.superblock.block.SuperSimpleBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class MachineSimpleBlock extends SuperSimpleBlock
{
    protected MachineSimpleBlock(String blockName, ModelState defaultModelState)
    {
        super(blockName, BlockSubstance.MACHINE, defaultModelState);
        this.metaCount = 1;
        this.setHarvestLevel(null, 0);
        this.setHardness(1);
    }

    //allow mined blocks to stack
    @Override
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    //allow mined blocks to stack
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getSubItems().get(0);
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
    public boolean isHypermatter()
    {
        return false;
    }
    
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
