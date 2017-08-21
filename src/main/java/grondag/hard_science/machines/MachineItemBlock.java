package grondag.hard_science.machines;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MachineItemBlock extends ItemBlock
{
    /**
     * Anything under this tag will not be sent to clients.
     * If your machine only needs a single tag, can use this directly.
     * Otherwise create sub-tags under this tag.
     */
    public static final String NBT_SERVER_SIDE_TAG = "SrvData";
    
    public static final int MAX_DAMAGE = 100;
    
    public static final int CAPACITY_COLOR = 0xFF6080FF;
        
    public MachineItemBlock(Block block)
    {
        super(block);
        this.setMaxDamage(MAX_DAMAGE);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack)
    {
        return CAPACITY_COLOR;
    }
    
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    { 
        // world.setBlockState returns false if the state was already the requested state
        // this is OK normally, but if we need to update the TileEntity it is the opposite of OK
        boolean wasUpdated = world.setBlockState(pos, newState, 3)
                || world.getBlockState(pos) == newState;
            
        if(!wasUpdated) 
            return false;

        if(!world.isRemote && newState.getBlock() instanceof MachineBlock)
        {
            MachineTileEntity blockTE = (MachineTileEntity)world.getTileEntity(pos);
            if (blockTE != null) 
            {
                NBTTagCompound serverSideTag = stack.hasTagCompound() ? stack.getSubCompound(MachineItemBlock.NBT_SERVER_SIDE_TAG) : null;
                    
                blockTE.restoreStateFromStackAndReconnect(stack, serverSideTag);
            }
        }
        
        this.block.onBlockPlacedBy(world, pos, newState, player, stack);
        return true;
    }

    /**
     * <i>Grondag: don't want to overflow size limits or burden 
     * network by sending details of embedded storage that will 
     * never be used on the client anyway.</i><br><br>
     * 
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        NBTTagCompound result = stack.getTagCompound();
        if(result != null && result.hasKey(NBT_SERVER_SIDE_TAG))
        {
            result = result.copy();
            result.removeTag(NBT_SERVER_SIDE_TAG);
        }
        return result;
    }
}
