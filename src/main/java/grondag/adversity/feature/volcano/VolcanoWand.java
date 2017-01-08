package grondag.adversity.feature.volcano;

import java.util.Map;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.LavaTerrainHelper;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
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

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ)
    {
        
        LavaTerrainHelper terrainThingy = new LavaTerrainHelper(worldIn);
        
        BlockPos targetPos = pos.up();
        float h = terrainThingy.computeIdealBaseFlowHeight(targetPos);
        
        if(h > 1)
        {
            worldIn.setBlockState(targetPos, IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), 1F));
            targetPos = targetPos.up();
            h -= 1F;
        }
        worldIn.setBlockState(targetPos, IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), h));

        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.up());
        
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.east());
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.west());
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.north());
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.south());
        
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.north().east());
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.south().east());
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.north().west());
        IFlowBlock.adjustFillIfNeeded(worldIn, targetPos.south().west());
        return EnumActionResult.SUCCESS;
    }




    
}
