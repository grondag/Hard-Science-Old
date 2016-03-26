package grondag.adversity.niceblock;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.library.Useful;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.niceblock.support.NicePlacement;

import java.util.List;

//import mcp.mobius.waila.api.IWailaConfigHandler;
//import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BigBlockHelper extends ColorHelperPlus
{
    //tried using a byte array here but kept reading as a string tag for reason I couldn't fathom
    private int defaultPlacementShape;
    
    public BigBlockHelper(ModelDispatcher dispatcher, int placementShape)
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

        // If player is sneaking and placing on same block with same color, force matching metadata.
        // Or, if player is sneaking and places on a block that cannot mate, force non-matching metadata
        if(player.isSneaking())
        {
            IBlockState placedOn = worldIn.getBlockState(posOn);
            if(placedOn.getBlock() == this.block && this.getModelStateForBlock(placedOn, worldIn, posOn, false).getColorIndex() == stack.getMetadata())
            {
                // Force match the metadata of the block on which we are placed
                return placedOn.getValue(NiceBlock.META);
            }
            else
            {
                // Force non-match of metadata for any neighboring blocks
                
                int colorIndex = this.getColorIndexFromItemStack(stack);
                int speciesInUseFlags = 0;
                NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced);
                NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.TestForBlockColorMatch(this.block, colorIndex));
                
                for(EnumFacing face : EnumFacing.VALUES)            
                {
                     if (results.result(face)) 
                     {
                         speciesInUseFlags |= (1 << neighbors.getBlockState(face).getValue(NiceBlock.META));
                     }
                }

                // try to avoid corners also if picking a species that won't connect
                for(BlockCorner corner : BlockCorner.values())
                {
                    if(results.result(corner))
                    {
                        speciesInUseFlags |= (1 << neighbors.getBlockState(corner).getValue(NiceBlock.META));
                    }
                }
                
                // now randomly choose a species 
                //that will not connect to what is surrounding
                int salt = Useful.SALT_SHAKER.nextInt(16);
                for(int i = 0; i < 16; i++)
                {
                    int species = (i + salt) % 16;
                    if((speciesInUseFlags & (1 << species)) == 0)
                    {
                        return species;
                    }
                }
            }
        }
        
        
        NBTTagCompound tag = stack.getTagCompound();
        
        if(tag != null && tag.hasKey(NiceTileEntity.PLACEMENT_SHAPE_TAG))
        {
            int shape = tag.getInteger(NiceTileEntity.PLACEMENT_SHAPE_TAG);
            
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
        if(tag != null && tag.hasKey(NiceTileEntity.PLACEMENT_SHAPE_TAG))
        {
            niceTE.setPlacementShape(tag.getInteger(NiceTileEntity.PLACEMENT_SHAPE_TAG));
            niceTE.markDirty();
        }
    }

    @Override
    public void updateItemStackForPickBlock(ItemStack stack, IBlockState blockState, ModelState modelState, NiceTileEntity niceTE)
    {
        super.updateItemStackForPickBlock(stack, blockState, modelState, niceTE);
        int placementShape = niceTE.getPlacementShape() != 0 ? niceTE.getPlacementShape() : defaultPlacementShape;
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
        tag.setInteger(NiceTileEntity.PLACEMENT_SHAPE_TAG, placementShape);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        int placementShape = stack.getTagCompound().getInteger(NiceTileEntity.PLACEMENT_SHAPE_TAG);
        if(placementShape != 0)
        {
            tooltip.add(String.format("Block Size: %1$d x %2$d x %3$d", placementShape & 0xFF, (placementShape >> 8) & 0xFF, (placementShape >> 16) & 0xFF));
        }
    }
    
//    @Override
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
//    {
//        List<String> retVal = super.getWailaBody(itemStack, currenttip, accessor, config);
//        retVal.add(LanguageRegistry.instance().getStringLocalization("species" + accessor.getMetadata()));
//        if(accessor.getTileEntity() != null && accessor.getTileEntity() instanceof NiceTileEntity)
//        {
//            retVal.add("Client Shape " + ((NiceTileEntity) accessor.getTileEntity()).modelState.getClientShapeIndex(BlockRenderLayer.CUTOUT_MIPPED.ordinal()));
//        }
//        return retVal;
//    }


}
