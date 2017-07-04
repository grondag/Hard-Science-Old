package grondag.hard_science.superblock.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

/**
 * Tool to rotate & connect/disconnect blocks
 */
public class BlockAdjuster extends Item
{
    private static final String MODE_TAG = "mode";

    private enum WandMode
    {
        ROTATE,
        CONNECT;
    }
    
    public BlockAdjuster()
    {
        setRegistryName("block_wand"); 
        setUnlocalizedName("block_wand");
        this.setMaxStackSize(1);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        
        if(!worldIn.isRemote)
        {
            WandMode newMode = WandMode.ROTATE;
            NBTTagCompound tag;

            if(stack.hasTagCompound())
            {
                tag = stack.getTagCompound();
                if(tag.getString(MODE_TAG).equals(WandMode.ROTATE.name()))
                {
                    newMode = WandMode.CONNECT;
                }
            }
            else
            {
                tag = new NBTTagCompound();

            }

            tag.setString(MODE_TAG, newMode.name());
            stack.setTagCompound(tag);

            @SuppressWarnings("deprecation")
            String message = I18n.translateToLocalFormatted("misc.mode_set",  I18n.translateToLocal("misc." + newMode.toString().toLowerCase()));
            playerIn.sendMessage(new TextComponentString(message));

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }    

    public WandMode getMode(ItemStack itemStackIn)
    {
        if(itemStackIn.hasTagCompound() && itemStackIn.getTagCompound().getString(MODE_TAG).equals(WandMode.ROTATE.name()))
        {
            return WandMode.ROTATE;
        }
        else
        {
            return WandMode.CONNECT;
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return EnumActionResult.SUCCESS;
        
        ItemStack stack = playerIn.getHeldItem(hand);
        
        if(getMode(stack) == WandMode.CONNECT)
        {
            return handleUseConnectMode(stack, playerIn, worldIn, pos);
        }
        else
        {
            return handleUseRotateMode(stack, playerIn, worldIn, pos);
        }
    }

    private EnumActionResult handleUseRotateMode(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private EnumActionResult handleUseConnectMode(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
