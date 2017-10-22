package grondag.hard_science.superblock.placement;

public enum PlacementEvent
{
    /**
     * Nothing happens, and click processing should continue.
     */
    NO_OPERATION_CONTINUE(false, false, false),
    /**
     * Nothing happens, and we eat the click event.
     */
    NO_OPERATION_STOP(false, false, false),
    
    /**
     * Place blocks, eat the event.
     * Cancel any previous operations in progress. 
     */
    PLACE(true, false, false),

    /**
     * Place blocks, also complete placement region selection operation, eat the event.
     */
    PLACE_AND_SET_REGION(true, false, true),
    
    /*
     * Finish placement region selection operation in progress. Eat the event. 
     */
    SET_PLACEMENT_REGION(false, false, true),
    
    /*
     * Start a new placement region selection operation.
     * Cancel any previous operations in progress. 
     * BlockPos is the first position that should be part of the region.
     * Eat the event. 
     */
    START_PLACEMENT_REGION(false, false, false),
    
    /*
     * Cancel placement region selection operation in progress.
     * Eat the event. 
     */
    CANCEL_PLACEMENT_REGION(false, false, false),
    
    /**
     * Remove blocks, also complete region selection operation, eat the event.
     * Block position identifies the block that was clicked.
     */
    EXCAVATE_AND_SET_REGION(false, true, true),
    
    /*
     * Remove blocks. Eat the event. 
     * Block position identifies the block that was clicked.
     */
    EXCAVATE(false, true, false),
    
    /**
     * Undo block placement. Block position identifies the block that was clicked.
     * Cancel any previous operations in progress. 
     * Eat the event. 
     */
    UNDO_PLACEMENT(false, false, false);
    
    public final boolean isExcavation;
    public final boolean isPlacement;
    public final boolean isSetRegion;
    
    private PlacementEvent(Boolean isPlacement, boolean isExcavation, boolean isSetRegion)
    {
        this.isPlacement = isPlacement;
        this.isExcavation = isExcavation;
        this.isSetRegion = isSetRegion;
    }
}
