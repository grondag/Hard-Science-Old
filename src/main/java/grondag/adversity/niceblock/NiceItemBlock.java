package grondag.adversity.niceblock;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Exists to provide sub-items for NiceBlocks.
 * Doesn't do much else.
 */
public class NiceItemBlock extends ItemBlock {
	public NiceItemBlock(Block block) {
		super(block);
		setMaxDamage(((NiceBlock) block).substances.length - 1);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + ((NiceBlock) block).style.toString() + "_" + stack.getMetadata();
	}
}