package grondag.hard_science.gui.control.machine;

import java.util.ArrayList;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.DeviceEnergyInfo;
import grondag.hard_science.machines.support.DeviceEnergyStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class MachinePowerUsage extends AbstractMachineControl<MachinePowerUsage, RadialRenderBounds>
{
    public MachinePowerUsage(MachineTileEntity tileEntity, RadialRenderBounds bounds)
    {
        super(tileEntity, bounds);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        DeviceEnergyStatus mps = this.tileEntity.clientState().powerSupplyStatus;
        if(mps != null)
        {
            ArrayList<String> list = new ArrayList<String>(3);
            list.add(I18n.translateToLocalFormatted("machine.power_out", 
                    mps.powerOutputWatts()));
            renderContext.drawToolTip(list, mouseX, mouseY);
        }
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        DeviceEnergyInfo mpi = this.tileEntity.clientState().powerSupplyInfo;
        if(mpi != null)
        {
            DeviceEnergyStatus mps = this.tileEntity.clientState().powerSupplyStatus;
            if(mps != null && mps.hasBattery())
            {
                MachineControlRenderer.renderPower(this.renderBounds, mpi, mps, 0xFF);
            }
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
