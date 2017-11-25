package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Placement that defines target region based on blocks that match a given predicate.
 */
public class PredicatePlacementSpec extends SingleStackPlacementSpec
{
    public PredicatePlacementSpec() {};
    
    protected PredicatePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
    {
        super(builder, sourceStack);
    }
    
    public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        return new PredicateBuilder(placedStack, player, pPos);
    }

    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.PREDICATE;
    }
}