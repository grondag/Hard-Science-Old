package grondag.adversity.niceblock.block;

import java.util.List;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.library.Useful;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.ModelDispatcher2;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.base.NiceBlockPlus2;
import grondag.adversity.niceblock.base.NiceItemBlock2;
import grondag.adversity.niceblock.base.NiceTileEntity2;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BigBlock2 extends NiceBlockPlus2
{
    //tried using a byte array here but kept reading as a string tag for reason I couldn't fathom
    private int defaultPlacementShape;


    public BigBlock2(ModelDispatcher2 dispatcher, BaseMaterial material, String styleName, int placementShape)
    {
        super(dispatcher, material, styleName);
        defaultPlacementShape = placementShape;
    }
    
    
     /**
      * Can't just override onBlockPlaced because need to know what block we were placed on
      * and method signature for onBlockPlaced doesn't provide that info.
      * (Might be possible to work back to it, but this is straightforward.)
      */
    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {

        // If player is sneaking and placing on same block with same color, force matching metadata.
        // Or, if player is sneaking and places on a block that cannot mate, force non-matching metadata
        if(player.isSneaking())
        {
            ModelColorMapComponent colorComponent = dispatcher.getStateSet().getFirstColorMapComponent();
            
            IBlockState placedOn = worldIn.getBlockState(posOn);
            if(placedOn.getBlock() == this 
                    && this.dispatcher.getStateSet().doComponentValuesMatch(colorComponent, 
                            this.getModelStateKey(placedOn, worldIn, posOn),
                            NiceItemBlock2.getModelStateKey(stack)))                 
            {
                // Force match the metadata of the block on which we are placed
                return placedOn.getValue(NiceBlock2.META);
            }
            else
            {
                // Force non-match of metadata for any neighboring blocks
                int speciesInUseFlags = 0;
                IBlockTest colorMatch = new BlockTests.TestForBlockColorMatch2(this, NiceItemBlock2.getModelStateKey(stack));
                NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced);
                NeighborTestResults results = neighbors.getNeighborTestResults(colorMatch);
                
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
        
        if(tag != null && tag.hasKey(NiceTileEntity2.PLACEMENT_SHAPE_TAG))
        {
            int shape = tag.getInteger(NiceTileEntity2.PLACEMENT_SHAPE_TAG);
            
            //tried using a byte array here but kept reading as a string tag for reason I couldn't fathom
            NicePlacement placer = new NicePlacement.PlacementBigBlock(
                    new PlacementValidatorCubic(shape & 0xFF, (shape >> 8) & 0xFF, (shape >> 16) & 0xFF));
            
            return placer.getMetaForPlacedStack(worldIn, posPlaced, facing, stack, this);
        }

        return 0;
    }

    @Override
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState,
            NiceTileEntity2 niceTE)
    {
        super.updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);

        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey(NiceTileEntity2.PLACEMENT_SHAPE_TAG))
        {
            niceTE.setPlacementShape(tag.getInteger(NiceTileEntity2.PLACEMENT_SHAPE_TAG));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        NiceTileEntity2 myTE = (NiceTileEntity2) world.getTileEntity(pos);
        int placementShape = myTE.getPlacementShape() != 0 ? myTE.getPlacementShape() : defaultPlacementShape;
        updateStackPlacementShape(stack, placementShape);
        return stack;
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
        tag.setInteger(NiceTileEntity2.PLACEMENT_SHAPE_TAG, placementShape);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        int placementShape = stack.getTagCompound().getInteger(NiceTileEntity2.PLACEMENT_SHAPE_TAG);
        if(placementShape != 0)
        {
            tooltip.add(String.format("Block Size: %1$d x %2$d x %3$d", placementShape & 0xFF, (placementShape >> 8) & 0xFF, (placementShape >> 16) & 0xFF));
        }
    }
}
