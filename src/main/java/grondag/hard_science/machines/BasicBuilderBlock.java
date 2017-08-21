package grondag.hard_science.machines;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BasicBuilderBlock extends MachineBlock implements ITileEntityProvider
{
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return super.createTileEntity(world, state);
    }

    public BasicBuilderBlock(String name)
    {
        super(name);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new BasicBuilderTileEntity();
    }
}
