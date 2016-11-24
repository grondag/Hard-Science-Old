package grondag.adversity.feature.volcano;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;


public class VolcanoWand extends Item
{
    public VolcanoWand() 
    {
        setRegistryName("volcano_wand"); 
        setUnlocalizedName("volcano_wand");
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(!worldIn.isRemote)
        {
            BlockPos targetPos = null;
            Map<BlockPos, TileEntity> map = worldIn.getChunkFromBlockCoords(playerIn.getPosition()).getTileEntityMap();
            for(Map.Entry<BlockPos, TileEntity> entry : map.entrySet())
            {
                if(entry.getValue() instanceof TileVolcano)
                {
                    targetPos = entry.getKey();
                    break;
                }
            }
            if(targetPos == null)
            {
                playerIn.addChatComponentMessage(new TextComponentString("No volcano in this chunk."));
            }
            else
            {
                playerIn.addChatComponentMessage(new TextComponentString("Found volcano at " + targetPos.toString()));
            }
            
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }




    
}