package grondag.adversity.superblock.texture;

public enum TextureLayout
{
    /**
     * Separate files with naming convention base_j_i where i is 0-7 and j is 0 or more.
     */
    SPLIT_X_8,
    /**
     * Single square file 
     */
    BIGTEX,
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the first 13 textures
     * out of every 16. Files won't exist or will be blank for 13-15.
     */       
    BORDER_13,
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the first 5 textures
     * out of every 8. Files won't exist or will be blank for 5-7.
     */ 
    MASONRY_5
    
    
}
