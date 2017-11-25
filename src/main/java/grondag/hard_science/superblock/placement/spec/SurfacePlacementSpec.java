package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Builder's wand type of placement
 */
public class SurfacePlacementSpec extends SingleStackPlacementSpec
{
    public SurfacePlacementSpec() {};
    
    protected SurfacePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
    {
        super(builder, sourceStack);
    }
    
    public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        return new SurfaceBuilder(placedStack, player, pPos);
    }

    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.SURFACE;
    }
}