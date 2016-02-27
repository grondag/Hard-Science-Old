package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.base.Function;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.support.NicePlacement;
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
        return ((NiceBlock)this.block).blockModelHelper.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        ((NiceBlock)this.block).blockModelHelper.addInformation(stack, playerIn, tooltip, advanced);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos onPos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        BlockPos placedPos = block.isReplaceable(worldIn, onPos) ? onPos : onPos.offset(side);
        
        if (stack.stackSize == 0)
        {
            return false;
        }        
        else if (!playerIn.canPlayerEdit(placedPos, side, stack))
        {
            return false;
        }
        
        else if (worldIn.canBlockBePlaced(this.block, placedPos, false, side, (Entity)null, stack))
        {
            int newMeta = ((NiceBlock)this.block).blockModelHelper.getMetaForPlacedBlockFromStack(worldIn, placedPos, onPos, side, stack, playerIn);
            
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, placedPos, side, hitX, hitY, hitZ, newMeta, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, placedPos, side, hitX, hitY, hitZ, iblockstate1))
            {
                worldIn.playSoundEffect((double)((float)placedPos.getX() + 0.5F), (double)((float)placedPos.getY() + 0.5F), (double)((float)placedPos.getZ() + 0.5F), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
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
        NiceBlock block = (NiceBlock)(this.block);
 
        if (!world.setBlockState(pos, newState, 3)) return false;

        Adversity.log.info("placeBlockAt world.isRemote = " + world.isRemote);
        if(newState.getBlock() instanceof NiceBlockPlus)
        {
            NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
            if (niceTE != null) 
            {
                block.blockModelHelper.updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);
                if(world.isRemote)
                {
                    Adversity.log.info("client color index after place = " + niceTE.modelState.getColorIndex());
                    world.markBlockForUpdate(pos);
                }
            }
        }
        
        this.block.onBlockPlacedBy(world, pos, newState, player, stack);
        return true;
    }	
}