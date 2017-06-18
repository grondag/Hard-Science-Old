package grondag.adversity.init;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ModKeys
{
    public static KeyBinding PLACEMENT_MODE;
    public static KeyBinding PLACEMENT_FACE;
    public static KeyBinding PLACEMENT_ROTATION;
    public static KeyBinding PLACEMENT_PREVIEW;

    public static void init(FMLInitializationEvent event)
    {
        PLACEMENT_MODE = new KeyBinding("key.placement_mode", KeyConflictContext.IN_GAME, Keyboard.KEY_B, "key.categories.adversity");
        PLACEMENT_FACE = new KeyBinding("key.placement_face", KeyConflictContext.IN_GAME, Keyboard.KEY_V, "key.categories.adversity");
        PLACEMENT_ROTATION = new KeyBinding("key.placement_rotation", KeyConflictContext.IN_GAME, Keyboard.KEY_R, "key.categories.adversity");
        PLACEMENT_PREVIEW = new KeyBinding("key.placement_preview", KeyConflictContext.IN_GAME, Keyboard.KEY_P, "key.categories.adversity");

        ClientRegistry.registerKeyBinding(PLACEMENT_MODE);
        ClientRegistry.registerKeyBinding(PLACEMENT_FACE);
        ClientRegistry.registerKeyBinding(PLACEMENT_ROTATION);
        ClientRegistry.registerKeyBinding(PLACEMENT_PREVIEW);
    }

}
