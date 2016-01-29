package grondag.adversity.niceblock;

import com.google.common.base.Function;

import grondag.adversity.Adversity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;

/**
 * Exists to provide sub-items for NiceBlocks.
 * Doesn't do much else.
 */
public class NiceItemBlock extends ItemMultiTexture {
	public NiceItemBlock(Block block, Block block2) {
		super(block, block2, new Function<ItemStack, String>() {
            @Override
            public String apply(ItemStack stack) 
            {
                return String.valueOf(stack.getMetadata());
            }
		});
		setHasSubtypes(true);
	}

    private static Function nameGetter = new Function<ItemStack, String>()
    {
        public String apply(ItemStack stack)
        {
            return stack.getUnlocalizedName();
        }
    }; 
    
    @Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + ((NiceBlock) block).style.toString() + "_" + stack.getMetadata();
	}
 	
}