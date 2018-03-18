package grondag.hard_science.movetogether;

import java.util.List;

import grondag.exotic_matter.render.EnhancedSprite;
import grondag.hard_science.movetogether.TextureRotationType.TextureRotationSetting;

public interface ITexturePalette
{

    /**
     * Max number of texture palettes that can be registered, loaded and represented in model state.
     */
    public static final int MAX_PALETTES = 4096;

    /**
     * Identifies all textures needed for texture stitch.
     */
    List<String> getTexturesForPrestich();

    /** 
     * Used by dispatcher as nominal particle texture.
     * More important usage is by GUI texture picker.
     */
    String getSampleTextureName();

    /**
     * For use by TESR and GUI to conveniently and quickly access default sprite
     */
    EnhancedSprite getSampleSprite();

    String getTextureName(int version);

    String getTextureName(int version, int index);

    String localizedName();
    
    public String textureBaseName();
    
    /** number of texture versions must be a power of 2 */
    public int textureVersionCount();
    
    public TextureScale textureScale();

    public TextureLayout textureLayout();

    
    /** 
     * Used to display appropriate label for texture.
     * 0 = no zoom, 1 = 2x zoom, 2 = 4x zoom
     */
    public int zoomLevel();

    /**
     * Masks the version number provided by consumers - alternators that
     * drive number generation may support larger number of values. 
     * Implies number of texture versions must be a power of 2 
     */
    public int textureVersionMask();
    
    /** Governs default rendering rotation for texture and what rotations are allowed. */
    public TextureRotationSetting rotation();
    
    /** 
     * Determines layer that should be used for rendering this texture.
     */
    public TextureRenderIntent renderIntent();
    
    /**
     * Globally unique id
     */
    public int ordinal();
    
    /**
     * Used by modelstate to know which world state must be retrieved to drive this texture
     * (rotation and block version)
     */
    public int stateFlags();
    
    public int textureGroupFlags();
    
    /**
     * Number of ticks each frame should be rendered on the screen
     * before progressing to the next frame.
     */
    public int ticksPerFrame();

    /** for border-layout textures, controls if "no border" texture is rendered */
    public boolean renderNoBorderAsTile();

}