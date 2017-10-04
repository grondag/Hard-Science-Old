package grondag.hard_science.gui.control;

import javax.annotation.Nonnull;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketOpenContainerStorageInteraction;
import grondag.hard_science.network.client_to_server.PacketOpenContainerStorageInteraction.Action;
import grondag.hard_science.simulator.wip.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.wip.ItemResource;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Used as a callback by controls to container to handle mouse input on items within the control.
 */
@SideOnly(Side.CLIENT)
public interface IClickHandler<T>
{
    public void handleMouseClick(Minecraft mc, int mouseButton, @Nonnull T target);
    public void handleMouseDrag(Minecraft mc, int mouseButton, @Nonnull T target);
    
    public static class StorageClickHandlerStack implements IClickHandler<AbstractResourceWithQuantity<StorageTypeStack>>
    {
        public static final StorageClickHandlerStack INSTANCE = new StorageClickHandlerStack();
        
        private StorageClickHandlerStack() {}
        
        @Override
        public void handleMouseClick(Minecraft mc, int mouseButton, @Nonnull AbstractResourceWithQuantity<StorageTypeStack> target)
        {
            Action action = null;
            
            boolean isShift = GuiScreen.isShiftKeyDown();
            
            ItemStack heldStack = mc.player.inventory.getItemStack();
            
            // if alt/right/middle clicking on same resource, don't count that as a depsit
            if(heldStack != null && !heldStack.isEmpty() && 
                    !(ItemResource.fromStack(heldStack).equals(target.resource()) && (GuiScreen.isAltKeyDown() || mouseButton > 0)))
            {
                // putting something in
                if(mouseButton == GuiUtil.MOUSE_LEFT && !GuiScreen.isAltKeyDown())
                {
                    action = Action.PUT_ALL_HELD;
                }
                else
                {
                    action = Action.PUT_ONE_HELD;
                }
            }
            else
            {
                if(mouseButton == GuiUtil.MOUSE_LEFT && !GuiScreen.isAltKeyDown())
                {
                    action = isShift ? Action.QUICK_MOVE_STACK : Action.TAKE_STACK;
                }
                else if(mouseButton == GuiUtil.MOUSE_MIDDLE || GuiScreen.isAltKeyDown())
                {
                    action = isShift ? Action.QUICK_MOVE_ONE : Action.TAKE_ONE;
                }
                else if(mouseButton == GuiUtil.MOUSE_RIGHT)
                {
                    action = isShift ? Action.QUICK_MOVE_HALF : Action.TAKE_HALF;
                }
            }
            
            if(action != null) ModMessages.INSTANCE.sendToServer(new PacketOpenContainerStorageInteraction(action, target.resource()));
        }

        @Override
        public void handleMouseDrag(Minecraft mc, int mouseButton, @Nonnull AbstractResourceWithQuantity<StorageTypeStack> target)
        {
            // doesn't seem like a useful interaction
        }
        
    }
}