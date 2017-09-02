package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.control.IGuiRenderContext;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;

public class MachineSymbol extends AbstractMachineControl<MachineSymbol>
{

    public MachineSymbol(MachineTileEntity tileEntity)
    {
        super(tileEntity);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderMachineText(this.renderBounds, this.tileEntity.machineName(), 255);
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
