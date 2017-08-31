package grondag.hard_science.gui.control.machine;

import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;

public class MachineName extends AbstractMachineControl<MachineName>
{

    public MachineName(MachineTileEntity tileEntity)
    {
        super(tileEntity);
    }

    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderMachineName(this.renderBounds, this.tileEntity.machineName(), 255);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
        
    }

}
