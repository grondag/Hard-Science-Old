package grondag.hard_science.superblock.placement;

public enum PlacementEvent
{
    /**
     * Nothing happens, and click processing should continue.
     */
    NO_OPERATION_CONTINUE,
    /**
     * Nothing happens, and we eat the click event.
     */
    NO_OPERATION_STOP,
    
    /**
     * Place blocks, eat the event.
     * Cancel any previous operations in progress. 
     */
    PLACE,

    /**
     * Place blocks, also complete placement region selection operation, eat the event.
     */
    PLACE_AND_SET_REGION,
    
    /*
     * Finish placement region selection operation in progress. Eat the event. 
     */
    SET_PLACEMENT_REGION,
    
    /*
     * Start a new placement region selection operation.
     * Cancel any previous operations in progress. 
     * BlockPos is the first position that should be part of the region.
     * Eat the event. 
     */
    START_PLACEMENT_REGION,
    
    /*
     * Cancel placement region selection operation in progress.
     * Eat the event. 
     */
    CANCEL_PLACEMENT_REGION,
    
    /**
     * Remove blocks, (or mark them for removal) eat the event.
     * Cancel any previous operations in progress. 
     * Block position identifies the block that was clicked.
     */
    EXCAVATE,
    
    /**
     * Remove blocks, also complete excavation region selection operation, eat the event.
     * Block position identifies the block that was clicked.
     */
    EXCAVATE_AND_SET_REGION,
    
    /*
     * Finish excavation region selection operation in progress. Eat the event. 
     * Block position identifies the block that was clicked.
     */
    SET_EXCAVATION_REGION,
    
    /*
     * Start a new excavation region selection operation.
     * Cancel any previous operations in progress. 
     * Block position identifies the block that was clicked.
     * Eat the event. 
     */
    START_EXCAVATION_REGION,
    
    /*
     * Cancel excavation region selection operation in progress.
     * Block position identifies the block that was clicked.
     * Eat the event. 
     */
    CANCEL_EXCAVATION_REGION,
    
    /**
     * Undo block placement. Block position identifies the block that was clicked.
     * Cancel any previous operations in progress. 
     * Eat the event. 
     */
    UNDO_PLACEMENT,
    
    /**
     * Undo block excavation. Block position identifies the block that was clicked.
     * Cancel any previous operations in progress. 
     * Eat the event. 
     */
    UNDO_EXCAVATION;
}
