package grondag.hard_science.virtualblock;

import java.util.List;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import grondag.hard_science.superblock.model.state.WorldLightOpacity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VirtualBlock extends SuperModelBlock
{
    public VirtualBlock(String blockName)
    {
        super(blockName, Material.AIR, BlockRenderMode.TESR, WorldLightOpacity.TRANSPARENT, false, false);
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState)
    {
        return false;
    }
    
    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState iBlockState)
    {
      return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean isCollidable()
    {
        return HardScience.proxy.allowCollisionWithVirtualBlocks(null);
    }

    /** Can't report AIR or right click doesn't work on client. */
    @Override
    public Material getMaterial(IBlockState state)
    {
        return  HardScience.proxy.allowCollisionWithVirtualBlocks(null) ? Material.STRUCTURE_VOID : Material.AIR;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
       return true;
    }
    
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return false;
    }
    
    public boolean doesSideBlockVirtualRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return super.doesSideBlockRendering(state, world, pos, face);
    }
    
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {

        if(this.getSubstance(blockState, blockAccess, pos).isTranslucent)
        {
            BlockPos otherPos = pos.offset(side);
            IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
            Block block = otherBlockState.getBlock();
            if(block instanceof SuperBlock)
            {
                SuperBlock sBlock = (SuperBlock)block;
                if(((SuperBlock) block).getSubstance(otherBlockState, blockAccess, otherPos).isTranslucent)
                {
                    ModelState myModelState = this.getModelStateAssumeStateIsCurrent(blockState, blockAccess, pos, false);
                    ModelState otherModelState = sBlock.getModelStateAssumeStateIsCurrent(otherBlockState, blockAccess, otherPos, false);
                    // for transparent blocks, want blocks with same apperance and species to join
                    return (myModelState.hasSpecies() && myModelState.getSpecies() != otherModelState.getSpecies())
                            || !myModelState.doShapeAndAppearanceMatch(otherModelState);

                }
            }
        }
        
        AxisAlignedBB axisalignedbb = blockState.getBoundingBox(blockAccess, pos);

        switch (side)
        {
            case DOWN:

                if (axisalignedbb.minY > 0.0D)
                {
                    return true;
                }

                break;
            case UP:

                if (axisalignedbb.maxY < 1.0D)
                {
                    return true;
                }

                break;
            case NORTH:

                if (axisalignedbb.minZ > 0.0D)
                {
                    return true;
                }

                break;
            case SOUTH:

                if (axisalignedbb.maxZ < 1.0D)
                {
                    return true;
                }

                break;
            case WEST:

                if (axisalignedbb.minX > 0.0D)
                {
                    return true;
                }

                break;
            case EAST:

                if (axisalignedbb.maxX < 1.0D)
                {
                    return true;
                }
        }

        BlockPos otherPos = pos.offset(side);
        IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
        if(otherBlockState.getBlock() == ModBlocks.virtual_block)
        {
            return !((VirtualBlock)ModBlocks.virtual_block).doesSideBlockVirtualRendering(otherBlockState, blockAccess, otherPos, side.getOpposite());
        }
        else
        {
            return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
    }
    
    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        list.add(this.getSubItems().get(0));
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return Block.NULL_AABB;
    }
    
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
            Entity entityIn, boolean p_185477_7_)
    {
        if(HardScience.proxy.allowCollisionWithVirtualBlocks(worldIn) && entityIn == null)
        {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
        else
        {
            return;
        }
    }
}

