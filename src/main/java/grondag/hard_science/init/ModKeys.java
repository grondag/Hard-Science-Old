package grondag.hard_science.init;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ModKeys
{
    public static KeyBinding PLACEMENT_MODE;
    public static KeyBinding PLACEMENT_HISTORY;
    public static KeyBinding PLACEMENT_ORIENTATION;
    public static KeyBinding PLACEMENT_PREVIEW;

    public static void init(FMLInitializationEvent event)
    {
        PLACEMENT_MODE = new KeyBinding("key.placement_mode", KeyConflictContext.IN_GAME, Keyboard.KEY_B, "key.categories.hard_science");
        PLACEMENT_HISTORY = new KeyBinding("key.placement_history", KeyConflictContext.IN_GAME, Keyboard.KEY_V, "key.categories.hard_science");
        PLACEMENT_ORIENTATION = new KeyBinding("key.placement_orientation", KeyConflictContext.IN_GAME, Keyboard.KEY_R, "key.categories.hard_science");
        PLACEMENT_PREVIEW = new KeyBinding("key.placement_preview", KeyConflictContext.IN_GAME, Keyboard.KEY_P, "key.categories.hard_science");

        ClientRegistry.registerKeyBinding(PLACEMENT_MODE);
        ClientRegistry.registerKeyBinding(PLACEMENT_HISTORY);
        ClientRegistry.registerKeyBinding(PLACEMENT_ORIENTATION);
        ClientRegistry.registerKeyBinding(PLACEMENT_PREVIEW);
    }

}
