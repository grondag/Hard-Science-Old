package grondag.adversity.superblock.placement;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.items.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AdditivePlacementHandler implements IPlacementHandler
{
    public static final AdditivePlacementHandler INSTANCE = new AdditivePlacementHandler();

    @Override
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        
        if(!(stack.getItem() instanceof SuperItemBlock)) return Collections.emptyList();
        
        SuperBlock myBlock = (SuperBlock) ((SuperItemBlock)stack.getItem()).block;
        ModelState myModelState = SuperItemBlock.getModelState(stack);
        
        IBlockState onBlockState = worldIn.getBlockState(posOn);
        SuperBlock onBlock = null;
        ModelState onModelState = null;
        if(onBlockState.getBlock() instanceof SuperBlock)
        {
            onBlock = (SuperBlock) onBlockState.getBlock();
            onModelState = onBlock.getModelStateAssumeStateIsCurrent(onBlockState, worldIn, posOn, true);
        }
        
        if(!(onBlock != null && onBlock == myBlock && onModelState.getShape() == myModelState.getShape()))
        {
            return SimplePlacementHandler.INSTANCE.getPlacementResults(playerIn, worldIn, posOn, hand, facing, hitX, hitY, hitZ, stack);
        }
        
        int species = (onModelState.getSpecies() + 1) & 0xF;
        BlockPos posPlaced = species == 0 ? posOn.offset(facing) : posOn;

        ModelState modelState = SuperItemBlock.getModelState(stack);
      
        ItemStack result = stack.copy();
        result.setItemDamage(species);
        modelState.setSpecies(species);
        SuperItemBlock.setModelState(result, modelState);
        return ImmutableList.of(Pair.of(posPlaced, result));
        
    }
}
