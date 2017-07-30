package grondag.hard_science.virtualblock;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VirtualBlock extends Block implements ITileEntityProvider
{
    public static final String VIRTUAL_BLOCK_NAME = "virtual_block";
    
    public VirtualBlock()
    {
        super(Material.AIR);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new VirtualBlockTileEntity();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return false;
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
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
       return true;
    }

 
    
    
    
}

