package grondag.adversity.feature.volcano;

import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class VolcanicLavaBlock extends FlowDynamicBlock
{
    public VolcanicLavaBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller)
    {
        super(dispatcher, material, styleName, isFiller);
        this.setLightLevel(4F/15F);
        this.setBlockUnbreakable();
        this.setResistance(2000F);
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return false;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
    {
        return false;
    }



}
