package grondag.hard_science.gui.control.machine;

import java.util.ArrayList;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class MachineFuelCell extends AbstractMachineControl<MachineFuelCell, RadialRenderBounds>
{
    
    public MachineFuelCell(MachineTileEntity tileEntity, RadialRenderBounds bounds)
    {
        super(tileEntity, bounds);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        ArrayList<String> list = new ArrayList<String>(3);
        list.add(I18n.translateToLocalFormatted("machine.power_in", this.tileEntity.getPowerSupply().fuelCell().powerOutputWatts()));
        renderContext.drawToolTip(list, mouseX, mouseY);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderFuelCell(this.renderBounds, this.tileEntity.getPowerSupply().fuelCell(), 0xFF);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
