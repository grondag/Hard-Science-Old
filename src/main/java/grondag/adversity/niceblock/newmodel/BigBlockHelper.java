package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.niceblock.support.NicePlacement;

import java.util.ArrayList;
import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

public class BigBlockHelper extends ColoredBlockHelperPlus
{
    //TODO: I'm sure this doesn't belong here
    public static final String PLACEMENT_SHAPE_TAG = "BBPlace";
    
    //tried using a byte array here but kept reading as a string tag for reason I couldn't fathom
    private int defaultPlacementShape;
    
    public BigBlockHelper(ModelDispatcherBase dispatcher, int placementShape)
    {
        super(dispatcher);
        defaultPlacementShape = placementShape;
    }

    // Can't pass metadata to block state for blocks that are meant to be tile entities
    // because the item metadata value (colorIndex) will be out of range.  
    // placeBlockAt will give the colorIndex value to the TE state.
    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {

        // if player is sneaking and placing on same block with same color, force matching metadata
        if(player.isSneaking())
        {
            IBlockState placedOn = worldIn.getBlockState(posOn);
            if(placedOn.getBlock() == this.block && this.getModelStateForBlock(placedOn, worldIn, posOn, false).getColorIndex() == stack.getMetadata())
            {
                return placedOn.getValue(NiceBlock.META);
            }
        }
        
        NBTTagCompound tag = stack.getTagCompound();
        
        if(tag != null && tag.hasKey(PLACEMENT_SHAPE_TAG))
        {
            int shape = tag.getInteger(PLACEMENT_SHAPE_TAG);
            
            //tried using a byte array here but kept reading as a string tag for reason I couldn't fathom
            NicePlacement placer = new NicePlacement.PlacementBigBlock(
                    new PlacementValidatorCubic(shape & 0xFF, (shape >> 8) & 0xFF, (shape >> 16) & 0xFF));
            
            return placer.getMetaForPlacedStack(worldIn, posPlaced, facing, stack, this);
        }

        return 0;
    }

    @Override
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState,
            NiceTileEntity niceTE)
    {
        super.updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);

        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey(PLACEMENT_SHAPE_TAG))
        {
            niceTE.placementShape = tag.getInteger(PLACEMENT_SHAPE_TAG);
            niceTE.markDirty();
        }
    }

    @Override
    public void updateItemStackForPickBlock(ItemStack stack, IBlockState blockState, ModelState modelState, NiceTileEntity niceTE)
    {
        super.updateItemStackForPickBlock(stack, blockState, modelState, niceTE);
        int placementShape = niceTE.placementShape != 0 ? niceTE.placementShape : defaultPlacementShape;
        updateStackPlacementShape(stack, placementShape);
    }

    @Override
    public List<ItemStack> getSubItems()
    {
        List<ItemStack> subItems = super.getSubItems();
        for(ItemStack stack : subItems)
        {
            updateStackPlacementShape(stack, defaultPlacementShape);
        }
            
        return subItems;
    }
    
    private void updateStackPlacementShape(ItemStack stack, int placementShape)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(PLACEMENT_SHAPE_TAG, placementShape);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        int placementShape = stack.getTagCompound().getInteger(PLACEMENT_SHAPE_TAG);
        if(placementShape != 0)
        {
            tooltip.add(String.format("Block Size: %1$d x %2$d x %3$d", placementShape & 0xFF, (placementShape >> 8) & 0xFF, (placementShape >> 16) & 0xFF));
        }
    }
    
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        List<String> retVal = super.getWailaBody(itemStack, currenttip, accessor, config);
        retVal.add(LanguageRegistry.instance().getStringLocalization("species" + accessor.getMetadata()));
        return retVal;
    }


}
