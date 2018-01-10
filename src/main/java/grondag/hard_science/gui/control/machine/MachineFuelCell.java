package grondag.hard_science.gui.control.machine;

import java.util.ArrayList;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MachinePowerSupplyInfo;
import grondag.hard_science.machines.support.MachinePowerSupplyStatus;
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
        MachinePowerSupplyStatus mps = this.tileEntity.clientState().powerSupplyStatus;
        if(mps != null && mps.hasFuelCell())
        {
            ArrayList<String> list = new ArrayList<String>(3);
            list.add(I18n.translateToLocalFormatted("machine.power_in", mps.fuelCell().powerOutputWatts()));
            renderContext.drawToolTip(list, mouseX, mouseY);
        }
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachinePowerSupplyInfo mpi = this.tileEntity.clientState().powerSupplyInfo;
        if(mpi != null)
        {
            MachinePowerSupplyStatus mps = this.tileEntity.clientState().powerSupplyStatus;
            if(mps != null && mps.hasFuelCell())
            {
                MachineControlRenderer.renderFuelCell(this.renderBounds, mpi, mps, 0xFF);
            }
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
