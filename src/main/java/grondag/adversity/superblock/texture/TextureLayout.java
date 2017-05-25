package grondag.adversity.superblock.texture;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

public enum TextureLayout
{
    /**
     * Separate files with naming convention base_j_i where i is 0-7 and j is 0 or more.
     */
    SPLIT_X_8 (STATE_FLAG_NONE),
    /**
     * Single square file 
     */
    BIGTEX (STATE_FLAG_NONE),
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the first 13 textures
     * out of every 16. Files won't exist or will be blank for 13-15.
     */       
    BORDER_13 (STATE_FLAG_NEEDS_CORNER_JOIN),
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the first 5 textures
     * out of every 8. Files won't exist or will be blank for 5-7.
     */ 
    MASONRY_5 (STATE_FLAG_NEEDS_SIMPLE_JOIN);
    
    private TextureLayout( int stateFlags)
    {
        this.modelStateFlag = stateFlags;
    }
    
    /** identifies the world state needed to drive texture random rotation/selection */
    public final int modelStateFlag;
}