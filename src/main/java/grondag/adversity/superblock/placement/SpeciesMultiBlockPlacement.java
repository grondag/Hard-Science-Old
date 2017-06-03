package grondag.adversity.superblock.placement;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpeciesMultiBlockPlacement implements IPlacementHandler
{
    public static final SpeciesMultiBlockPlacement INSTANCE = new SpeciesMultiBlockPlacement();
    
    @Override
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        IBlockState blockStateOn = worldIn.getBlockState(posOn);
        Block blockOn = blockStateOn.getBlock();
        
        BlockPos posPlaced = blockOn.isReplaceable(worldIn, posOn)? posOn : posOn.offset(facing);
        
        ModelState modelState = SuperItemBlock.getModelState(stack);
        int species = getSpecies(modelState, worldIn, posPlaced, posOn, facing, stack, playerIn);
        if(species == modelState.getSpecies())
        {
            return ImmutableList.of(Pair.of(posPlaced, stack));
        }
        else
        {
            ItemStack result = stack.copy();
            stack.setItemDamage(species);
            modelState.setSpecies(species);
            SuperItemBlock.setModelState(result, modelState);
            return ImmutableList.of(Pair.of(posPlaced, result));
        }
    }

    private int getSpecies(ModelState modelState, World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        if(!modelState.hasSpecies()) return 0;

        if(modelState.getShape().meshFactory().isSpeciesUsedForHeight()) return stack.getMetadata();
        
        if(!(stack.getItem() instanceof SuperItemBlock)) return 0;
        
        SuperBlock block = (SuperBlock) ((SuperItemBlock)stack.getItem()).block;
        
        // If player is sneaking and placing on same block, force matching species.
        // Or, if player is sneaking and places on a block that cannot mate, force non-matching species
        if(player.isSneaking())
        {
            IBlockState placedOn = worldIn.getBlockState(posOn);
            if(placedOn.getBlock() == block)
            {
                // Force match the metadata of the block on which we are placed
                return ((SuperBlock)placedOn.getBlock()).getModelStateAssumeStateIsCurrent(placedOn, worldIn, posOn, true).getSpecies();
            }
            else
            {
                // Force non-match of metadata for any neighboring blocks
                int speciesInUseFlags = 0;
                SuperBlock myBlock = (SuperBlock) (((SuperItemBlock)stack.getItem()).block);

                NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced, false);
                NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch(myBlock, modelState, false));
                
                for(EnumFacing face : EnumFacing.VALUES)            
                {
                    if (results.result(face)) 
                    {
                        speciesInUseFlags |= (1 << neighbors.getModelState(face).getSpecies());
                    }
                }

                // try to avoid corners also if picking a species that won't connect
                for(BlockCorner corner : BlockCorner.values())
                {
                    if (results.result(corner)) 
                    {
                        speciesInUseFlags |= (1 << neighbors.getModelState(corner).getSpecies());
                    }
                }

                // now randomly choose a species 
                //that will not connect to what is surrounding
                int salt = ThreadLocalRandom.current().nextInt(16);
                for(int i = 0; i < 16; i++)
                {
                    int candidate = (i + salt) % 16;
                    if((speciesInUseFlags & (1 << candidate)) == 0)
                    {
                        return candidate;
                    }
                }
                
                // give up
                return 0;
            }
        }
        // player not sneaking, so choose species based on placement shape
        else
        {
            int shape = SpeciesGenerator.PLACEMENT_3x3x3;

            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null && tag.hasKey(NiceTileEntity.PLACEMENT_SHAPE_TAG))
            {
                shape = tag.getInteger(NiceTileEntity.PLACEMENT_SHAPE_TAG);
            }

            SpeciesGenerator placer = new SpeciesGenerator.PlacementBigBlock(
                    new PlacementValidatorCubic(shape & 0xFF, (shape >> 8) & 0xFF, (shape >> 16) & 0xFF));

            return placer.getSpeciesForPlacedStack(worldIn, posPlaced, facing, stack, block);
        }
    }
}
