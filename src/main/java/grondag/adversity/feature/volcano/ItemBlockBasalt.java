package grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockBasalt extends ItemBlock
{
  public ItemBlockBasalt(Block block)
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
  public String getUnlocalizedName(ItemStack stack)
  {
    BlockBasalt.EnumStyle style = BlockBasalt.EnumStyle.byMetadata(stack.getMetadata());
    return super.getUnlocalizedName() + "." + style.toString();
  }
}