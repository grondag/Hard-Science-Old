package grondag.adversity.init;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class DummyColorHandler implements IItemColor
{
    public static final DummyColorHandler INSTANCE = new DummyColorHandler();
    
    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return 0xFFFFFFFF;
    }
}