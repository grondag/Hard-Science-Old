package grondag.adversity.niceblocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class NiceItemBlock extends ItemBlock {
	public NiceItemBlock(Block block) {
		super(block);
		setMaxDamage(((NiceBlock) block).substances.length - 1);
		setHasSubtypes(true);
	}

	// @Override
	// public int getMetadata(int metadata)
	// {
	// return metadata;
	// }

	//
	// @Override
	// public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
	// // TODO Auto-generated method stub
	// super.getSubItems(itemIn, tab, subItems);
	// }

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + ((NiceBlock) block).style.toString() + "_" + stack.getMetadata();
	}
}