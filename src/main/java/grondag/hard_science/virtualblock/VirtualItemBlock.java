package grondag.hard_science.virtualblock;

import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.placement.FilterMode;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementItemFeature;
import grondag.hard_science.superblock.placement.RegionOrientation;
import net.minecraft.item.ItemStack;

public class VirtualItemBlock extends SuperItemBlock implements PlacementItem
{
    
    public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(
            PlacementItemFeature.FIXED_REGION,
            PlacementItemFeature.REGION_SIZE,
            PlacementItemFeature.GUI,
            PlacementItemFeature.REGION_ORIENTATION,
            PlacementItemFeature.SELECTION_RANGE,
            PlacementItemFeature.SPECIES_MODE,
            PlacementItemFeature.TARGET_MODE,
            PlacementItemFeature.BLOCK_ORIENTATION);
    
    public VirtualItemBlock(VirtualBlock block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }
    
    @Override
    public int featureFlags(ItemStack stack)
    {
        return FEATURE_FLAGS;
    }
    
    @Override
    public int guiOrdinal()
    {
        return ModGuiHandler.ModGui.SUPERMODEL_ITEM.ordinal();
    }
    
    @Override
    public boolean isVirtual(ItemStack stack)
    {
        return true;
    }
    
    @Override
    public FilterMode getFilterMode(ItemStack stack)
    {
        return FilterMode.FILL_REPLACEABLE;
    }
    
    @Override
    public RegionOrientation getRegionOrientation(ItemStack stack)
    {
        return RegionOrientation.XYZ;
    }
}
