package grondag.hard_science.init;

import grondag.exotic_matter.placement.IPlacementItem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;

public enum ModKeyConflictContext implements IKeyConflictContext
{
    HOLDING_PLACEMENT_ITEM
    {
        @Override
        public boolean isActive()
        {
            final ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
            return IPlacementItem.isPlacementItem(held);
        }

        @Override
        public boolean conflicts(
                final IKeyConflictContext other )
        {
            return this == other;
        }
    }
}
