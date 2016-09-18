package grondag.adversity.feature.volcano;

import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.modelstate.ModelSpeciesComponent;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class HotBasaltBlock extends NiceBlock
{
	
    public HotBasaltBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName) {
		super(dispatcher, material, styleName);
//		this.setTickRandomly(true);
	}

	// helps to prevent some weird lighting artifacts
//    @Override
//    public boolean isTranslucent(IBlockState state)
//    {
//    	return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
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
            NiceItemBlock.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }

//    @Override
//    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
//    {
//        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? 15 << 20 | 15 << 4 : super.getPackedLightmapCoords(state, source, pos);
//    }

    // necessary to offset the AO lighting effects of a fully lit translucent layer
    // values >= 15 are changed to 1 by rendering code
//    @Override
//    public int getLightOpacity(IBlockState state) {
//        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? 0 : 255;
//    }
//
//    @Override
//    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
//        return getLightOpacity(state);
//    }
    
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
