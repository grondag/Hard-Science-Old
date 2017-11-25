package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Surface, but adds to height of existing block surfaces 
 * that may not be aligned with block boundaries.
 */
public class AdditivePlacementSpec extends SurfacePlacementSpec
{
    public AdditivePlacementSpec() {};
    
    protected AdditivePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
    {
        super(builder, sourceStack);
    }
    
    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.ADDITIVE;
    }
    
    public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        return new AdditiveBuilder(placedStack, player, pPos);
    }
}