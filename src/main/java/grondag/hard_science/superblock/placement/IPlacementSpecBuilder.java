package grondag.hard_science.superblock.placement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPlacementSpecBuilder
{

    /**
     * Checks for obstacles (if matters according to stack settings)
     * Check for world boundaries.
     * Adjusts selection to avoid obstacles (if configured to do so)
     * Returns true is selection can be placed and have a non-empty result.
     * This is only called during preview and will not check every
     * block position for large regions. Detailed checked for every position
     * are performed incrementally by the submitted job.
     */
    public boolean validate();

    /**
        If stack is in selection mode, renders selection region
        that would result if the selection region is terminated at
        the given position.
        <p>
        For CSG shapes, this may be a shape other than a cuboid.
        <p>
        If placement is valid (via {@link #validate()})
        buffers quads or lines showing where it will be in world.
        If placement is not valid because of obstacles,
        uses color to indicate obstructed/invalid and 
        show where at least some of the obstacles are (if not too expensive).
     */
    @SideOnly(Side.CLIENT)
    void renderPreview(RenderWorldLastEvent event, EntityPlayerSP player);
    
    
    /**
       Creates a placement spec that can be submitted to the build manager.
       Captures all information necessary for the build but should not
       do any significant computation. <p>
       
       Defer internal work to the {@link AbstractPlacementSpec#entries()}
       method so that it can happen off the game thread.
       
     */
    public AbstractPlacementSpec build();

    
}
