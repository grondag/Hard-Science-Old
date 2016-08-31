package grondag.adversity.niceblock;

import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.ModelDispatcher2;
import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.base.NiceItemBlock2;
import grondag.adversity.niceblock.modelstate.ModelSpeciesComponent;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;

public class HotBasaltBlock extends NiceBlock2 {
	
    public HotBasaltBlock(ModelDispatcher2 dispatcher, BaseMaterial material, String styleName) {
		super(dispatcher, material, styleName);
//		this.setTickRandomly(true);
	}

	// helps to prevent some weird lighting artifacts
    @Override
    public boolean isTranslucent(IBlockState state)
    {
    	return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
    }

//    @Override
//    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
//    {
//        if (!world.isRemote) {
//
//            // TODO: restore function
//            //OddUtils.igniteSurroundingBlocks(world, pos);
//
//            final int meta = state.getValue(NiceBlock.META);
//
//            // necessary to prevent too many block updates on large volcanoes
//            if (meta == 0 || world.getBlockState(pos.east()).getBlock() != Blocks.air 
//                    && world.getBlockState(pos.west()) != Blocks.air
//                    && world.getBlockState(pos.north()) != Blocks.air 
//                    && world.getBlockState(pos.south()) != Blocks.air
//                    && world.getBlockState(pos.up()) != Blocks.air) {
//                world.setBlockState(pos, NiceBlockRegistrar2.COOL_SQUARE_BASALT_BLOCK.getDefaultState(), 2);
//            } else {
//                world.setBlockState(pos, state.withProperty(NiceBlock.META, meta-1), 2);
//            }
//        }
//    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

    @Override
    public List<ItemStack> getSubItems()
    {
        ModelSpeciesComponent species = dispatcher.getStateSet().getFirstSpeciesComponent();
        int itemCount = (int) species.getValueCount();
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < itemCount; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            long key = dispatcher.getStateSet().computeKey(species.createValueFromBits(i));
            NiceItemBlock2.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
    

    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? 15 << 20 | 15 << 4 : super.getPackedLightmapCoords(state, source, pos);
    }

    // necessary to offset the AO lighting effects of a fully lit translucent layer
    // values >= 15 are changed to 1 by rendering code
    @Override
    public int getLightOpacity(IBlockState state) {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? 0 : 255;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getLightOpacity(state);
    }
    
//    @Override
//    public String getItemStackDisplayName(ItemStack stack)
//    {
//        String prefix;
//        switch(stack.getMetadata())
//        {
//        case 0:
//            prefix = "cooling";
//            break;
//        case 1:
//            prefix = "warm";
//            break;
//        case 2:
//            prefix = "hot";
//            break;
//        default:
//            prefix = "very_hot";
//        }
//        return "" + I18n.translateToLocal(prefix) + " " + I18n.translateToLocal("basalt");
//    }

}
