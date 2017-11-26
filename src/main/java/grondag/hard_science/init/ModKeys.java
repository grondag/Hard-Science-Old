package grondag.hard_science.init;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ModKeys
{
    public static KeyBinding PLACEMENT_HISTORY_FORWARD;
    public static KeyBinding PLACEMENT_HISTORY_BACK;
    public static KeyBinding PLACEMENT_PREVIEW;
    public static KeyBinding PLACEMENT_DISPLAY_GUI;
    public static KeyBinding PLACEMENT_CYCLE_FILTER_MODE;
    public static KeyBinding PLACEMENT_CYCLE_SPECIES_HANDLING;
    public static KeyBinding PLACEMENT_CYCLE_TARGET_MODE;
    public static KeyBinding PLACEMENT_CYCLE_BLOCK_ORIENTATION;
    public static KeyBinding PLACEMENT_CYCLE_REGION_ORIENTATION;
    public static KeyBinding PLACEMENT_UNDO;
    public static KeyBinding PLACEMENT_INCREASE_WIDTH;
    public static KeyBinding PLACEMENT_DECREASE_WIDTH;
    public static KeyBinding PLACEMENT_INCREASE_HEIGHT;
    public static KeyBinding PLACEMENT_DECREASE_HEIGHT;
    public static KeyBinding PLACEMENT_INCREASE_DEPTH;
    public static KeyBinding PLACEMENT_DECREASE_DEPTH;
    public static KeyBinding PLACEMENT_MOVE_SELECTION;
    public static KeyBinding PLACEMENT_CYCLE_SELECTION_TARGET;
    public static KeyBinding PLACEMENT_LAUNCH_BUILD;

    public static void init(FMLInitializationEvent event)
    {
        PLACEMENT_CYCLE_SELECTION_TARGET = new KeyBinding("key.placement_cycle_selection_target", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.CONTROL, Keyboard.KEY_R, "key.categories.hard_science");
        PLACEMENT_CYCLE_REGION_ORIENTATION = new KeyBinding("key.placement_cycle_region_orientation", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.ALT, Keyboard.KEY_R, "key.categories.hard_science");
        PLACEMENT_CYCLE_BLOCK_ORIENTATION = new KeyBinding("key.placement_cycle_block_orientation", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_R, "key.categories.hard_science");
        PLACEMENT_HISTORY_FORWARD = new KeyBinding("key.placement_history_forward", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.SHIFT, Keyboard.KEY_B, "key.categories.hard_science");
        PLACEMENT_HISTORY_BACK = new KeyBinding("key.placement_history_back", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.CONTROL, Keyboard.KEY_B, "key.categories.hard_science");
        PLACEMENT_DISPLAY_GUI = new KeyBinding("key.placement_display_gui", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_B, "key.categories.hard_science");
        PLACEMENT_CYCLE_FILTER_MODE = new KeyBinding("key.placement_cycle_filter_mode", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.CONTROL, Keyboard.KEY_F, "key.categories.hard_science");
        PLACEMENT_CYCLE_SPECIES_HANDLING = new KeyBinding("key.placement_cycle_species_handling", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.CONTROL, Keyboard.KEY_V, "key.categories.hard_science");
        PLACEMENT_CYCLE_TARGET_MODE = new KeyBinding("key.placement_cycle_target_mode", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_V, "key.categories.hard_science");
        PLACEMENT_PREVIEW = new KeyBinding("key.placement_preview", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_P, "key.categories.hard_science");
        PLACEMENT_INCREASE_WIDTH = new KeyBinding("key.placement_increase_width", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_RIGHT, "key.categories.hard_science");
        PLACEMENT_DECREASE_WIDTH = new KeyBinding("key.placement_decrease_width", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_LEFT, "key.categories.hard_science");
        PLACEMENT_INCREASE_HEIGHT = new KeyBinding("key.placement_increase_height", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.SHIFT, Keyboard.KEY_UP, "key.categories.hard_science");
        PLACEMENT_DECREASE_HEIGHT = new KeyBinding("key.placement_decrease_height", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.SHIFT, Keyboard.KEY_DOWN, "key.categories.hard_science");
        PLACEMENT_INCREASE_DEPTH = new KeyBinding("key.placement_increase_depth", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_UP, "key.categories.hard_science");
        PLACEMENT_DECREASE_DEPTH = new KeyBinding("key.placement_decrease_depth", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.NONE, Keyboard.KEY_DOWN, "key.categories.hard_science");
        PLACEMENT_UNDO = new KeyBinding("key.placement_undo", ModKeyConflictContext.HOLDING_PLACEMENT_ITEM, 
                KeyModifier.CONTROL, Keyboard.KEY_Z, "key.categories.hard_science");
        PLACEMENT_LAUNCH_BUILD = new KeyBinding("key.placement_launch_build", KeyConflictContext.IN_GAME, 
                KeyModifier.CONTROL, Keyboard.KEY_L, "key.categories.hard_science");
        
        ClientRegistry.registerKeyBinding(PLACEMENT_HISTORY_FORWARD);
        ClientRegistry.registerKeyBinding(PLACEMENT_HISTORY_BACK);
        ClientRegistry.registerKeyBinding(PLACEMENT_PREVIEW);
        ClientRegistry.registerKeyBinding(PLACEMENT_DISPLAY_GUI);
        ClientRegistry.registerKeyBinding(PLACEMENT_CYCLE_FILTER_MODE);
        ClientRegistry.registerKeyBinding(PLACEMENT_CYCLE_SPECIES_HANDLING);
        ClientRegistry.registerKeyBinding(PLACEMENT_CYCLE_TARGET_MODE);
        ClientRegistry.registerKeyBinding(PLACEMENT_CYCLE_BLOCK_ORIENTATION);
        ClientRegistry.registerKeyBinding(PLACEMENT_CYCLE_REGION_ORIENTATION);
        ClientRegistry.registerKeyBinding(PLACEMENT_UNDO);
        ClientRegistry.registerKeyBinding(PLACEMENT_INCREASE_WIDTH);
        ClientRegistry.registerKeyBinding(PLACEMENT_DECREASE_WIDTH);
        ClientRegistry.registerKeyBinding(PLACEMENT_INCREASE_HEIGHT);
        ClientRegistry.registerKeyBinding(PLACEMENT_DECREASE_HEIGHT);
        ClientRegistry.registerKeyBinding(PLACEMENT_INCREASE_DEPTH);
        ClientRegistry.registerKeyBinding(PLACEMENT_DECREASE_DEPTH);
        ClientRegistry.registerKeyBinding(PLACEMENT_CYCLE_SELECTION_TARGET);
        ClientRegistry.registerKeyBinding(PLACEMENT_LAUNCH_BUILD);
    }

}
