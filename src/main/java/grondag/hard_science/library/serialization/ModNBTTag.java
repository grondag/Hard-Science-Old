package grondag.hard_science.library.serialization;

/**
 * Provides concise and consistent (albeit human-unfriendly) tags for serialization. 
 * DO NOT MODIFY ORDER OR REMOVE VALUES UNLESS PUBLISHING A WORLD-BREAKING RELEASE.
 * 
 * @author grondag
 *
 */
public class ModNBTTag
{
    private static int nextID = 0;

    public final static String ASSIGNED_IDENTIFER = "hs" + Integer.toHexString(++nextID);
    public final static String ASSIGNED_NUMBERS_AUTHORITY = "hs" + Integer.toHexString(++nextID);
    public final static String BASALT_BLOCKS = "hs" + Integer.toHexString(++nextID);
    public final static String DOMAIN_USER_FLAGS = "hs" + Integer.toHexString(++nextID);
    public final static String DOMAIN_USER_NAME = "hs" + Integer.toHexString(++nextID);
    public final static String DOMIAN_ITEM_STORAGE = "hs" + Integer.toHexString(++nextID);
    public final static String DOMIAN_MANAGER_DOMAINS = "hs" + Integer.toHexString(++nextID);
    public final static String DOMIAN_MANAGER = "hs" + Integer.toHexString(++nextID);
    public final static String DOMIAN_NAME = "hs" + Integer.toHexString(++nextID);
    public final static String DOMIAN_SECURITY_ENABLED = "hs" + Integer.toHexString(++nextID);
    public final static String DOMIAN_USERS = "hs" + Integer.toHexString(++nextID);
    public final static String ITEM_RESOURCE_ITEM = "hs" + Integer.toHexString(++nextID);
    public final static String ITEM_RESOURCE_META = "hs" + Integer.toHexString(++nextID);
    public final static String ITEM_RESOURCE_STACK_CAPS = "hs" + Integer.toHexString(++nextID);
    public final static String ITEM_RESOURCE_STACK_TAG = "hs" + Integer.toHexString(++nextID);
    public final static String LAVA_ADD_EVENTS = "hs" + Integer.toHexString(++nextID);
    public final static String LAVA_CELLS = "hs" + Integer.toHexString(++nextID);
    public final static String LAVA_PARTICLE_AMOUNT = "hs" + Integer.toHexString(++nextID);
    public final static String LAVA_PARTICLE_MANAGER = "hs" + Integer.toHexString(++nextID);
    public final static String LAVA_PLACEMENT_EVENTS = "hs" + Integer.toHexString(++nextID);
    public final static String LAVA_SIMULATOR = "hs" + Integer.toHexString(++nextID);
    public final static String LOCATION_DIMENSION = "hs" + Integer.toHexString(++nextID);
    public final static String LOCATION_POSITION = "hs" + Integer.toHexString(++nextID);
    public final static String MACHINE_CONTROL_STATE = "hs" + Integer.toHexString(++nextID);
    public final static String MACHINE_JOB_DURATION_TICKS = "hs" + Integer.toHexString(++nextID);
    public final static String MACHINE_JOB_REMAINING_TICKS = "hs" + Integer.toHexString(++nextID);
    public final static String MACHINE_MODEL_STATE = "hs" + Integer.toHexString(++nextID);
    public final static String MACHINE_POWER_BUFFER = "hs" + Integer.toHexString(++nextID);
    public final static String MACHINE_TARGET_BLOCKPOS = "hs" + Integer.toHexString(++nextID);
    public final static String MODEL_STATE = "hs" + Integer.toHexString(++nextID);
    public final static String PLACEMENT_FACE = "hs" + Integer.toHexString(++nextID);
    public final static String PLACEMENT_MODE = "hs" + Integer.toHexString(++nextID);
    public final static String PLACEMENT_ROTATION = "hs" + Integer.toHexString(++nextID);
    public final static String RESOURCE_QUANTITY = "hs" + Integer.toHexString(++nextID);
    public final static String SERVER_SIDE_ONLY = "hs" + Integer.toHexString(++nextID);
    public final static String SIMULATION_LAST_TICK = "hs" + Integer.toHexString(++nextID);
    public final static String SIMULATION_WORLD_TICK_OFFSET = "hs" + Integer.toHexString(++nextID);
    public final static String SIMULATOR = "hs" + Integer.toHexString(++nextID);
    public final static String STORAGE_CAPACITY = "hs" + Integer.toHexString(++nextID);
    public final static String STORAGE_CONTENTS = "hs" + Integer.toHexString(++nextID);
    public final static String STORAGE_ID = "hs" + Integer.toHexString(++nextID);
    public final static String STORAGE_MANAGER_STORES = "hs" + Integer.toHexString(++nextID);
    public final static String SUPER_MODEL_SUBSTANCE = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_BUILD_LEVEL = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_CLEARING_LEVEL = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_COOLDOWN_TICKS = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_GROUND_LEVEL = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_LAVA_COUNTER = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_LEVEL = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_MANAGER_IS_CREATED = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_MANAGER = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_ACTIVE = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_DIMENSION = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_HEIGHT = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_LAST_ACTIVATION_TICK = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_STAGE = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_WEIGHT = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_X = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_Y = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODE_TAG_Z = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_NODES = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_STAGE = "hs" + Integer.toHexString(++nextID);
    public final static String VOLCANO_TICKS_ACTIVE = "hs" + Integer.toHexString(++nextID);
    public final static String WORLD_STATE_BUFFER = "hs" + Integer.toHexString(++nextID);
}
