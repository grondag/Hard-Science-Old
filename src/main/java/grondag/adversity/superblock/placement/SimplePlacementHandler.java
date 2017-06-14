package grondag.adversity.superblock.placement;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SimplePlacementHandler implements IPlacementHandler
{
    public static final SimplePlacementHandler INSTANCE = new SimplePlacementHandler();

    @Override
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        IBlockState iblockstate = worldIn.getBlockState(posOn);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, posOn))
        {
            posOn = posOn.offset(facing);
        }
        
        if(WorldHelper.isBlockReplaceable(worldIn, posOn))
        {
            return ImmutableList.of(Pair.of(posOn, stack));
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
