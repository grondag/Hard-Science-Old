package grondag.adversity.niceblock;

import java.util.Random;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

public class HotBasaltBlock extends NiceBlock {
	
    public HotBasaltBlock(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName) {
		super(blockModelHelper, material, styleName);
		this.setTickRandomly(true);
	}

	// helps to prevent some weird lighting artifacts
    @Override
    public boolean isTranslucent(IBlockState state)
    {
    	return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if (!world.isRemote) {

            // TODO: restore function
            //OddUtils.igniteSurroundingBlocks(world, pos);

            final int meta = state.getValue(NiceBlock.META);

            // necessary to prevent too many block updates on large volcanoes
            if (meta == 0 || world.getBlockState(pos.east()).getBlock() != Blocks.air 
                    && world.getBlockState(pos.west()) != Blocks.air
                    && world.getBlockState(pos.north()) != Blocks.air 
                    && world.getBlockState(pos.south()) != Blocks.air
                    && world.getBlockState(pos.up()) != Blocks.air) {
                world.setBlockState(pos, NiceBlockRegistrar.BLOCK_COOL_BASALT.getDefaultState(), 2);
            } else {
                world.setBlockState(pos, state.withProperty(NiceBlock.META, meta-1), 2);
            }
        }
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

    
}
