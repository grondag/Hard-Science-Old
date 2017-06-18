package grondag.adversity.superblock.placement;

/** 
 * Determines how placed blocks are oriented (axis, rotation)
 */
public enum PlacementMode
{
    /** based on the face on which it is placed */
    FACE,
    
    /** match the closest similar block */
    MATCH_CLOSEST,
    
    /** ignore face and surrounding - based on facing and orientation setting */
    STATIC
}
