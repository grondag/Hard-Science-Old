package grondag.hard_science.machines.base;

import grondag.hard_science.HardScience;
import grondag.hard_science.movetogether.ISuperModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MachineContainerBlock extends MachineBlock
{

    public MachineContainerBlock(String name, int guiID, ISuperModelState modelState)
    {
        super(name, guiID, modelState);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        // this is main difference for container blocks - activation happens server-side 
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
}
