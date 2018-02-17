package grondag.hard_science.superblock.items;

import grondag.hard_science.HardScience;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import net.minecraft.item.Item;

/**
 * Package sizes for solid material blocks
 * and fluid containers. 
 */
public class CraftingItem extends Item
{
    public final ModelState modelState;
    
    public CraftingItem(String name, ModelState modelState)
    {
        super();
        this.modelState = modelState;
        int colorIndex = this.hashCode() % BlockColorMapProvider.INSTANCE.getColorMapCount();
        this.modelState.setColorMap(PaintLayer.BASE, 
                BlockColorMapProvider.INSTANCE.getColorMap(colorIndex));
        this.setCreativeTab(HardScience.tabMod);
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
    }
}
