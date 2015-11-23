package grondag.adversity.library;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class NiceItemBlock2 extends ItemBlock
{
  public NiceItemBlock2(Block block)
  {
    super(block);
    this.setMaxDamage(0);
    this.setHasSubtypes(true);
  }

  @Override
  public int getMetadata(int metadata)
  {
    return metadata;
  }
  
  

  @Override
public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
	// TODO Auto-generated method stub
	super.getSubItems(itemIn, tab, subItems);
}

@Override
  public String getUnlocalizedName(ItemStack stack)
  {
	NiceBlock2.EnumStyle style = NiceBlock2.EnumStyle.byMetadata(stack.getMetadata());
    return super.getUnlocalizedName() + "." + style.toString();
  }
}