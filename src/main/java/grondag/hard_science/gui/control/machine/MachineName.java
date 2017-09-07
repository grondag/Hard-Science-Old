package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;

public class MachineName extends AbstractMachineControl<MachineName>
{

    public MachineName(MachineTileEntity tileEntity)
    {
        super(tileEntity);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderMachineText(this.renderBounds, this.tileEntity.machineName(), HorizontalAlignment.CENTER, 255);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
        
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawLocalizedToolTip("machine.name", mouseX, mouseY);
    }

}
