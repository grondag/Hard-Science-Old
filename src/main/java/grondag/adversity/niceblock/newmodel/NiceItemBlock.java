package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.base.Function;

import grondag.adversity.Adversity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Exists to provide sub-items for NiceBlocks.
 * Doesn't do much else.
 */
public class NiceItemBlock extends ItemBlock {
	public NiceItemBlock(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	public void registerSelf()
	{
	    registerItemBlock(block, this);
	}
	
    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return ((NiceBlock)this.block).getItemStackDisplayName(stack);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(side);
        }

        if (stack.stackSize == 0)
        {
            return false;
        }
        else if (!playerIn.canPlayerEdit(pos, side, stack))
        {
            return false;
        }
        else if (worldIn.canBlockBePlaced(this.block, pos, false, side, (Entity)null, stack))
        {
            // Can't pass metadata to block state for blocks that are meant to be tile entities
            // because value will be out of range.  placeBlockAt will give the value to the TE state.
            int i = this.block instanceof NiceBlockPlus ? 0 : this.getMetadata(stack.getMetadata());
            
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, i, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ, iblockstate1))
            {
                worldIn.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
                --stack.stackSize;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    
    /**
     * Called to actually place the block, after the location is determined
     * and all permission checks have been made.
     *
     * @param stack The item stack that was used to place the block. This can be changed inside the method.
     * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
     * @param side The side the player (or machine) right-clicked on.
     */
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        if (!world.setBlockState(pos, newState, 3)) return false;

        if(newState.getBlock() instanceof NiceBlockPlus)
        {
            NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
            if (niceTE != null) 
            {
                niceTE.modelState.setColorIndex(stack.getMetadata());
                niceTE.markDirty();
            }
        }
        this.block.onBlockPlacedBy(world, pos, newState, player, stack);

        return true;
    }	
}