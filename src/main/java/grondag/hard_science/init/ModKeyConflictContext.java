package grondag.hard_science.init;

import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;

public enum ModKeyConflictContext implements IKeyConflictContext
{
    HOLDING_PLACEMENT_ITEM
    {
        @Override
        public boolean isActive()
        {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if(player == null) return false;
                    
            final ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
            return PlacementItem.isPlacementItem(held);
        }

        @Override
        public boolean conflicts(
                final IKeyConflictContext other )
        {
            return this == other;
        }
    }
}
