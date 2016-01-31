package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.base.Function;

import grondag.adversity.Adversity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    public String getUnlocalizedName(ItemStack stack)
    {
        // TODO Auto-generated method stub
        return super.getUnlocalizedName(stack);
    }

    @Override
    public String getUnlocalizedName()
    {
        // TODO Auto-generated method stub
        return super.getUnlocalizedName();
    }

    @Override
    public int getMetadata(int damage)
    {
        // TODO Auto-generated method stub
        return super.getMetadata(damage);
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        // TODO Auto-generated method stub
        return super.getColorFromItemStack(stack, renderPass);
    }

    @Override
    public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining)
    {
        // TODO Auto-generated method stub
        return super.getModel(stack, player, useRemaining);
    }


    @Override
    public int getMetadata(ItemStack stack)
    {
        return ((NiceBlock)this.block).blockModelHelper.getModelStateForItem(stack).getMeta();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return ((NiceBlock)this.block).getItemStackDisplayName(stack);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState)
    {
        // TODO need to override this to enable multi-block placements
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }
 	
	
}