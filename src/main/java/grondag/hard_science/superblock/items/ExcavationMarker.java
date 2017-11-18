package grondag.hard_science.superblock.items;

import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *  Virtual tool crafted using the tablet, marks real-world blocks for removal. 
 *  Has several selection modes.
    All actions are immediately submitted as jobs.
 */
public class ExcavationMarker extends Item implements PlacementItem
{
    public ExcavationMarker()
    {
        setRegistryName("excavation_marker"); 
        setUnlocalizedName("excavation_marker");
        this.setMaxStackSize(1);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        
        if(!worldIn.isRemote)
        {

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }    

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return EnumActionResult.SUCCESS;
    }

    @Override
    public SuperBlock getSuperBlock()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int guiOrdinal()
    {
        // TODO Auto-generated method stub
        return 0;
    }

   
}
