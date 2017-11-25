package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** placeholder class for CSG multiblock placements */
public class CSGPlacementSpec extends VolumetricPlacementSpec
{
    public CSGPlacementSpec() {};
    
    protected CSGPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
    {
        super(builder, sourceStack);
    }
    
    public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        return new CSGBuilder(placedStack, player, pPos);
    }
    
    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.CSG;
    }
}