package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;

public abstract class AbstractMachineControl<T extends AbstractMachineControl<T>> extends GuiControl<T>
{
    protected final MachineTileEntity tileEntity;
    
    protected RenderBounds renderBounds;
    
    protected AbstractMachineControl(MachineTileEntity tileEntity)
    {
        this.tileEntity = tileEntity;
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        this.renderBounds = new RenderBounds(this.left, this.top, this.width, this.height);
    }
    
    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        //nothing special for most
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        //nothing special for most
    }
}
