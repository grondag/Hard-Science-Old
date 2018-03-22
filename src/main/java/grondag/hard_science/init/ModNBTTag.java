package grondag.hard_science.init;

/**
 * Provides concise and consistent (albeit human-unfriendly) tags for serialization. 
 * DO NOT MODIFY ORDER OR REMOVE VALUES UNLESS PUBLISHING A WORLD-BREAKING RELEASE.
 * 
 * @author grondag
 *
 */
public class ModNBTTag
{
    public static int nextID = 0;

    public final static String BASALT_BLOCKS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String DOMAIN_ITEM_STORAGE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String DOMAIN_MANAGER_DOMAINS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String DOMAIN_MANAGER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String DOMAIN_NAME = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String DOMAIN_SECURITY_ENABLED = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String DOMAIN_USERS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String LAVA_ADD_EVENTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String LAVA_CELLS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String LAVA_PARTICLE_AMOUNT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String LAVA_PARTICLE_MANAGER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String LAVA_PLACEMENT_EVENTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String LAVA_SIMULATOR = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MACHINE_CONTROL_STATE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MACHINE_JOB_DURATION_TICKS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MACHINE_JOB_REMAINING_TICKS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MACHINE_MODEL_STATE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MACHINE_STORED_ENERGY_JOULES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MACHINE_TARGET_BLOCKPOS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String RESOURCE_QUANTITY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String RESOURCE_IDENTITY= "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String SERVER_SIDE_ONLY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String STORAGE_CAPACITY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String STORAGE_CONTENTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_BUILD_LEVEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_CLEARING_LEVEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_COOLDOWN_TICKS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_GROUND_LEVEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_LAVA_COUNTER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_LEVEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_MANAGER_IS_CREATED = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_MANAGER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_ACTIVE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_DIMENSION = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_HEIGHT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_LAST_ACTIVATION_TICK = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_STAGE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_WEIGHT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_X = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_Y = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODE_TAG_Z = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_NODES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_STAGE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String VOLCANO_TICKS_ACTIVE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String WORLD_STATE_BUFFER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    
    public static final String MATERIAL_HDPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_MINERAL_FILLER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public final static String MATERIAL_RESIN_A = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_RESIN_B = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_NANO_LIGHTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_DYE_CYAN = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_DYE_MAGENTA = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_DYE_YELLOW = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_CARBON = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MATERIAL_TiO2 = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String MACHINE_FUEL_CELL_PLATE_SIZE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MACHINE_FUEL_CELL_PLATE_COUNT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MACHINE_FUEL_CELL_HAS_THERMAL_CAPTURE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MACHINE_BATTERY_MAX_STORED_JOULES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String MACHINE_BATTERY_CHEMISTRY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

//    public static final String SUPER_MODEL_LIGHT_VALUE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    
    public final static String PLACEMENT_DELETE_ENABLED = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    
    public final static String DOMAIN_JOB_MANAGER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String CONSTRUCTION_MANAGER_REQUESTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String REQUEST_STATUS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String REQUEST_PRIORITY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String REQUEST_USER_NAME = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String REQUEST_DEPENDENCIES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String REQUEST_CHILDREN = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String REQUEST_TYPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PLACMENT_IS_HOLLOW = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PLACEMENT_FIXED_REGION_END_POS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

//    public static final String PLACEMENT_FIXED_REGION_ENABLED = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

//    public static final String PLACEMENT_FIXED_REGION_SELECT_POS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PLACEMENT_PLAYER_NAME = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PLACEMENT_SPEC_TYPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PLACEMENT_SPEC_INDEX = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String TASK_POSITION = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String JOB_HELD_FLAG = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

//    public static final String BUILD_MANAGER_BUILDS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String BUILD_POSITIONS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

//    public static final String DOMAIN_BUILD_MANAGER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String BUILD_JOB_ID = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String BUILD_DIMENSION_ID = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DOMAIN_PLAYER_DOMAINS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DOMAIN_ACTIVE_DOMAINS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PLACEMENT_IS_VIRTUAL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DOMAIN_ID = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String BUILD_ID = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String TASK_STACK = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PROCUREMENT_TASK_ID = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String TASK_ALLOCATED_QTY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String TASK_REQUESTED_QTY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String RESOURCE_TYPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String RESOURCE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PREDICATE_TYPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DEVICE_TYPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DEVICE_MANAGER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DEVICE_MANAGER_DEVICES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String MACHINE_ID = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String MACHINE_STATE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DEVICE_CHANNEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String DEVICE_COMPONENT_TYPE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String MACHINE_GENERATOR = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String ENERGY_OUTPUT_BUFFER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String ENERGY_INPUT_BUFFER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String PORT_LAYOUT_NAME = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PORT_LAYOUT_FACEMAP = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String DEVICE_PORT_LAYOUT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

    public static final String RECIPE_INPUT_JOULES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String RECIPE_TICKS_DURATION = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String RECIPE_OUTPUT_JOULES = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String RECIPE_FLUID_INPUTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String RECIPE_ITEM_INPUTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String RECIPE_FLUID_OUTPUTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String RECIPE_ITEM_OUTPUTS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PROCESS_RESERVE_STOCKING_LEVEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PROCESS_FLUID_SETTINGS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String BUFFER_ITEMS_IN = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String BUFFER_ITEMS_OUT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String BUFFER_FLUIDS_IN = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String BUFFER_FLUIDS_OUT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String BULK_ITEM_NL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String BUFFER_PURPOSE = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String TASK_DELIVERED_QTY = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PRIVATE_BUFFER = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PROCESS_TARGET_STOCKING_LEVEL = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PROCESS_INGREDIENT = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);
    public static final String PROCESS_INGREDIENT_SETTINGS = "hs" + Integer.toUnsignedString(++nextID, Character.MAX_RADIX);

}
