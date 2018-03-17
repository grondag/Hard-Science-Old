package grondag.hard_science.superblock.texture;

import static grondag.hard_science.superblock.model.state.ModelStateData.*;

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
     * Separate files with naming convention same as SPLIT_X_8 except only the start 13 textures
     * out of every 16 are used for borders.  Texture 14 contains the face that should be
     * rendered if the border is rendered in the solid render layer.  It is IMPORTANT that texture
     * 14 have a solid alpha channel - otherwise mipmap generation will be borked.  The solid face
     * won't be used at all if rendering in a non-solid layer. 
     * Files won't exist or will be blank for 14 and 15.
     */       
    BORDER_13 (STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SPECIES),
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the start 5 textures
     * out of every 8. Files won't exist or will be blank for 5-7.
     */ 
    MASONRY_5 (STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN | STATE_FLAG_NEEDS_SPECIES),
    
    /**
     * Animated big textures stored as series of .jpg files
     */
    BIGTEX_ANIMATED (STATE_FLAG_NONE);
    
    private TextureLayout( int stateFlags)
    {
        this.modelStateFlag = stateFlags;
    }
    
    /** identifies the world state needed to drive texture random rotation/selection */
    public final int modelStateFlag;
}
