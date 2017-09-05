package grondag.hard_science.superblock.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.library.world.WorldHelper;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/*
 * Implements the following logic for shapes that are additive: <br>
 * 
 * 1) If clicking on "top" of a block of same type then add to the stack. 
 * This may cause another block to be added. <br><br>
 * 
 * 2) First check for an additive block
 * in the position adjacent to the face clicked.  If an additive block is 
 * found there, add to it as in #1.  Otherwise, if the space is empty
 * place a new additive block on the face clicked. 
 */
public class AdditivePlacementHandler implements IPlacementHandler
{
    public static final AdditivePlacementHandler INSTANCE = new AdditivePlacementHandler();

    @Override
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {

        if(!(stack.getItem() instanceof SuperItemBlock)) return Collections.emptyList();
        
        if(((PlacementItem)stack.getItem()).getMode(stack) == PlacementMode.STATIC) 
            return CubicPlacementHandler.INSTANCE.getPlacementResults(playerIn, worldIn, posOn, hand, facing, hitX, hitY, hitZ, stack);

        final SuperBlock stackBlock = (SuperBlock) ((SuperItemBlock)stack.getItem()).getBlock();
        final ModelState stackModelState = SuperItemBlock.getStackModelState(stack);

        final IBlockState onBlockState = worldIn.getBlockState(posOn);

        if(onBlockState.getBlock() instanceof SuperBlock)
        {
            final SuperBlock onBlock = (SuperBlock) onBlockState.getBlock();
            final ModelState onModelState = onBlock.getModelStateAssumeStateIsCurrent(onBlockState, worldIn, posOn, true);

            if(onBlock == stackBlock 
                    && onModelState != null
                    && onModelState.getShape() == stackModelState.getShape()
                    && onModelState.doesAppearanceMatch(stackModelState)) 
            {
                // target is this additive block
    
                if(!onModelState.hasAxis() 
                        || (onModelState.getAxis() == facing.getAxis()
                        && (!onModelState.hasAxisOrientation() || onModelState.isAxisInverted() == (facing.getAxisDirection() == AxisDirection.NEGATIVE))))
                {
                    // we clicked on an additive face
                    
                    // confirm have space to add - the ItemStack handler will allow us to get 
                    // here if the adjacent position contains another additive block
                    if(onModelState.getMetaData() < 0xF || WorldHelper.isBlockReplaceable(worldIn, posOn.offset(facing), stackBlock != ModBlocks.virtual_block))
                    {
                        return addToBlockAtPosition(worldIn, stack, stackModelState, onModelState, posOn);
                    }
                }
            }
        }
        
        // is there an additive block against the face we clicked?
        final BlockPos facePos = posOn.offset(facing);
        final IBlockState faceBlockState = worldIn.getBlockState(facePos);
        if(faceBlockState.getBlock() instanceof SuperBlock)
        {
            final SuperBlock faceBlock = (SuperBlock) faceBlockState.getBlock();
            final ModelState faceModelState = faceBlock.getModelStateAssumeStateIsCurrent(faceBlockState, worldIn, facePos, true);

            if((faceBlock != null 
                    && faceBlock == stackBlock 
                    && faceModelState.getShape() == stackModelState.getShape())
                    && faceModelState.doesAppearanceMatch(stackModelState)) 
             {
                // add to the adjacent block 
                return addToBlockAtPosition(worldIn, stack, stackModelState, faceModelState, facePos);
             }
        }

        // fall back to standard placement logic
        return CubicPlacementHandler.INSTANCE.getPlacementResults(playerIn, worldIn, posOn, hand, facing, hitX, hitY, hitZ, stack);

    }


    private List<Pair<BlockPos, ItemStack>> addToBlockAtPosition(IBlockAccess worldIn, ItemStack stack, ModelState stackModelState, ModelState onModelState, BlockPos posOn)
    {
        int totalMeta = (onModelState.getMetaData() + stackModelState.getMetaData() + 1);
        ArrayList<Pair<BlockPos, ItemStack>> result = new ArrayList<Pair<BlockPos, ItemStack>>(2);

        // add to or top off existing block
        if(onModelState.getMetaData() < 0xF)
        {
            int targetMeta = Math.min(totalMeta, 0xF);
            ModelState modelState = onModelState.clone();
            ItemStack newStack = stack.copy();
            newStack.setItemDamage(targetMeta);
            modelState.setMetaData(targetMeta);
            SuperItemBlock.setStackModelState(newStack, modelState);
            result.add(Pair.of(posOn, newStack));
        }

        // add another block if we grew into next block and the space is open
        if(totalMeta > 0xF)
        {
            EnumFacing addFace = EnumFacing.UP;
            
            int targetMeta = totalMeta & 0xF;
            ModelState modelState = onModelState.clone();
            if(modelState.hasAxis())
            {
                modelState.setAxis(onModelState.getAxis());
                if(modelState.hasAxisOrientation())
                {
                    modelState.setAxisInverted(onModelState.isAxisInverted());
                    addFace = EnumFacing.getFacingFromAxis(onModelState.isAxisInverted() ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, 
                            onModelState.getAxis());
                }
                else
                {
                    addFace = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, onModelState.getAxis());
                }
            }

            if(WorldHelper.isBlockReplaceable(worldIn, posOn.offset(addFace), stack.getItem() != ModItems.virtual_block))
            {
                ItemStack newStack = stack.copy();
                newStack.setItemDamage(targetMeta);
                modelState.setMetaData(targetMeta);
                SuperItemBlock.setStackModelState(newStack, modelState);
                result.add(Pair.of(posOn.offset(addFace), newStack));
            }
        }

        return result;
    }
}
