package grondag.adversity.niceblocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class NiceItemBlock extends ItemBlock
{
  public NiceItemBlock(NiceBlock block)
  {
    super(block);
    this.setMaxDamage(block.substances.length - 1);
    this.setHasSubtypes(true);
  }

//  @Override
//  public int getMetadata(int metadata)
//  {
//    return metadata;
//  }
  
//
//  @Override
//public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
//	// TODO Auto-generated method stub
//	super.getSubItems(itemIn, tab, subItems);
//}

@Override
  public String getUnlocalizedName(ItemStack stack)
  {
    return super.getUnlocalizedName() + "." + ((NiceBlock)block).style.toString() + "_" + stack.getMetadata();
  }
}