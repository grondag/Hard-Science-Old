package grondag.hard_science.virtualblock;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VirtualRenderEntry
{
    public final boolean isExcavation;
    
    
    public VirtualRenderEntry(ItemStack stack)
    {
        this.isExcavation = stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemBlock);
    }
}
