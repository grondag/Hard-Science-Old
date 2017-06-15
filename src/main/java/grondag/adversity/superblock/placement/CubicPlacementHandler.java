package grondag.adversity.superblock.placement;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.world.NeighborBlocks;
import grondag.adversity.library.world.NeighborBlocks.BlockCorner;
import grondag.adversity.library.world.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.world.WorldHelper;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.items.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.varia.BlockTests;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubicPlacementHandler implements IPlacementHandler
{
    public static final CubicPlacementHandler INSTANCE = new CubicPlacementHandler();

    @Override
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        if(!(stack.getItem() instanceof SuperItemBlock)) return Collections.emptyList();

        SuperBlock myBlock = (SuperBlock) ((SuperItemBlock)stack.getItem()).block;
        
        ModelState modelState = SuperItemBlock.getModelStateFromStack(stack);
        ItemStack result = stack.copy();
        IBlockState blockStateOn = worldIn.getBlockState(posOn);
        Block onBlock = blockStateOn.getBlock();
        BlockPos posPlaced = onBlock.isReplaceable(worldIn, posOn) ? posOn : posOn.offset(facing);
        
        // abort if target space is occupied
        if(!WorldHelper.isBlockReplaceable(worldIn, posPlaced))
        {
            return Collections.emptyList();
        }
        
        if(modelState.hasAxis())
        {
            boolean isAxisDone = false;
            // TODO: make fixed orientation a GUI option: match placement, match adjacent or fixed selected
            if(onBlock instanceof SuperBlock)
            {
                ModelState modelStateOn = ((SuperBlock)onBlock).getModelState(worldIn, posOn, true);
                if(modelStateOn.hasAxis())
                {
                    modelState.setAxis(modelStateOn.getAxis());
                    if(modelState.hasAxisOrientation())
                    {
                        modelState.setAxisInverted(modelStateOn.isAxisInverted());
                    }
                    isAxisDone = true;
                }
            }
            
            if(!isAxisDone)
            {
                modelState.setAxis(facing.getAxis());
                if(modelState.hasAxisOrientation())
                {
                    modelState.setAxisInverted(facing.getAxisDirection() == AxisDirection.NEGATIVE);
                }
            }
        }
        
        if(modelState.hasSpecies())
        {
            int species = getSpecies(playerIn, worldIn, posOn, blockStateOn, onBlock, posPlaced, myBlock, modelState);
            modelState.setSpecies(species);
            result.setItemDamage(modelState.getMetaData());
        }
        SuperItemBlock.setModelState(result, modelState);
 
        return ImmutableList.of(Pair.of(posPlaced, result));
    }

    
    private int getSpecies(EntityPlayer player, World worldIn, BlockPos posOn, IBlockState blockStateOn, Block blockOn, BlockPos posPlaced, SuperBlock myBlock, ModelState myModelState)
    {
        // If player is sneaking, force no match to adjacent species.
        // If not sneaking, try to match block on which placed, or failing that, any adjacent block it can match.
        if(player.isSneaking())
        {
            // Force non-match of species for any neighboring blocks
            int speciesInUseFlags = 0;

            NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced, false);
            NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch(myBlock, myModelState, false));
            
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
        else
        {
            // try to match block placed on
            if(blockOn == myBlock)
            {
                ModelState modelStateOn = ((SuperBlock)blockOn).getModelStateAssumeStateIsCurrent(blockStateOn, worldIn, posOn, true);
                if(myModelState.doShapeAndAppearanceMatch(modelStateOn)) return modelStateOn.getSpecies();

            }
            
            // try to match an adjacent block
            NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced, false);
            NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch(myBlock, myModelState, false));
            
            for(EnumFacing face : EnumFacing.VALUES)            
            {
                if (results.result(face)) 
                {
                    return neighbors.getModelState(face).getSpecies();
                }
            }
            
            // give up
            return 0;
        }
    }
}
