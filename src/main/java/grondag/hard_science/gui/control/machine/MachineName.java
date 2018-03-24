package grondag.hard_science.gui.control.machine;

import grondag.exotic_matter.font.FontHolder;
import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RectRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;

public class MachineName extends AbstractMachineControl<MachineName, RectRenderBounds>
{

    public MachineName(MachineTileEntity tileEntity, RectRenderBounds bounds)
    {
        super(tileEntity, bounds);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderMachineText(FontHolder.FONT_RENDERER_LARGE, this.renderBounds, this.tileEntity.clientState().machineName, HorizontalAlignment.CENTER, 0xFFFFFFFF);
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
