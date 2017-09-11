package grondag.hard_science.library.serialization;

/**
 * Provides concise and consistent (albeit human-unfriendly) tags for serialization. DO NOT MODIFY ORDER OR REMOVE VALUES UNLESS PUBLISHING A WORLD-BREAKING RELEASE.
 * 
 * @author grondag
 *
 */
public enum ModNBTTag
{
    ASSIGNED_IDENTIFER,
    ASSIGNED_NUMBERS_AUTHORITY,
    BASALT_BLOCKS,
    DOMAIN_USER_FLAGS,
    DOMAIN_USER_NAME,
    DOMIAN_ITEM_STORAGE,
    DOMIAN_MANAGER_DOMAINS,
    DOMIAN_MANAGER,
    DOMIAN_NAME,
    DOMIAN_SECURITY_ENABLED,
    DOMIAN_USERS,
    ITEM_RESOURCE_ITEM,
    ITEM_RESOURCE_META,
    ITEM_RESOURCE_STACK_CAPS,
    ITEM_RESOURCE_STACK_TAG,
    LAVA_ADD_EVENTS,
    LAVA_CELLS,
    LAVA_PARTICLE_AMOUNT,
    LAVA_PARTICLE_MANAGER,
    LAVA_PLACEMENT_EVENTS,
    LAVA_SIMULATOR,
    LOCATION_DIMENSION,
    LOCATION_POSITION,
    MACHINE_CONTROL_STATE,
    MACHINE_JOB_DURATION_TICKS,
    MACHINE_JOB_REMAINING_TICKS,
    MACHINE_MODEL_STATE,
    MACHINE_POWER_BUFFER,
    MACHINE_TARGET_BLOCKPOS,
    MODEL_STATE,
    PLACEMENT_FACE,
    PLACEMENT_MODE,
    PLACEMENT_ROTATION,
    RESOURCE_QUANTITY,
    SERVER_SIDE_ONLY,
    SIMULATION_LAST_TICK,
    SIMULATION_WORLD_TICK_OFFSET,
    SIMULATOR,
    STORAGE_CAPACITY,
    STORAGE_CONTENTS,
    STORAGE_ID,
    STORAGE_MANAGER_STORES,
    SUPER_MODEL_SUBSTANCE,
    VOLCANO_BUILD_LEVEL,
    VOLCANO_CLEARING_LEVEL,
    VOLCANO_COOLDOWN_TICKS,
    VOLCANO_GROUND_LEVEL,
    VOLCANO_LAVA_COUNTER,
    VOLCANO_LEVEL,
    VOLCANO_MANAGER_IS_CREATED,
    VOLCANO_MANAGER,
    VOLCANO_NODE_TAG_ACTIVE,
    VOLCANO_NODE_TAG_DIMENSION,
    VOLCANO_NODE_TAG_HEIGHT,
    VOLCANO_NODE_TAG_LAST_ACTIVATION_TICK,
    VOLCANO_NODE_TAG_STAGE,
    VOLCANO_NODE_TAG_WEIGHT,
    VOLCANO_NODE_TAG_X,
    VOLCANO_NODE_TAG_Y,
    VOLCANO_NODE_TAG_Z,
    VOLCANO_NODES,
    VOLCANO_STAGE,
    VOLCANO_TICKS_ACTIVE,
    WORLD_STATE_BUFFER;
    
    public final String tag;

    private ModNBTTag()
    {
        this.tag = Integer.toHexString(this.ordinal());
    }
}
