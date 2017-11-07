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
     * Returns true is selection can be built and will have a non-empty result.
     * @param isPreview TODO
     */
    public boolean validate(boolean isPreview);

    /**
        If stack is in selection mode, renders selection region
        that would result if the selection region is terminated at
        the given position.
        <p>
        For CSG shapes, this may be a shape other than a cuboid.
        <p>
        If placement is valid (via {@link #validate(boolean)})
        buffers quads or lines showing where it will be in world.
        If placement is not valid because of obstacles,
        uses color to indicate obstructed/invalid and 
        show where at least some of the obstacles are (if not too expensive).
     */
    @SideOnly(Side.CLIENT)
    void renderPreview(RenderWorldLastEvent event, EntityPlayerSP player);
    
    
    /**
       Used if user clicks to place a valid placement. Does the following:
       <ul>
       <li>Finalizes affected block positions and item stacks
       <li>Selects optimal species if needed
       <li>creates spec
       <li>creates job w/ spec attached, all needed tasks
           <ul>
           <li>place virtual blocks in empty spaces (handled by fast, cost-free service)
               virtual blocks reference job so can update it if removed
           <li>initial synch of preview renders for occupied blocks
           <li>excavation tasks
           <li>fab tasks
           <li>placement tasks
           </ul>
       </ul>
     * @return
     */
    public AbstractPlacementSpec build();

    
}
