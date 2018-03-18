package grondag.hard_science.superblock.items;

import grondag.exotic_matter.model.BlockColorMapProvider;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.PaintLayer;
import grondag.hard_science.HardScience;
import net.minecraft.item.Item;

/**
 * Package sizes for solid material blocks
 * and fluid containers. 
 */
public class CraftingItem extends Item
{
    public final ISuperModelState modelState;
    
    public CraftingItem(String name, ISuperModelState modelState)
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
