package grondag.hard_science.gui.control.machine;

import java.util.ArrayList;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.energy.ClientEnergyInfo;
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
        ClientEnergyInfo mps = this.tileEntity.clientState().powerSupplyInfo;
        if(mps != null)
        {
            ArrayList<String> list = new ArrayList<String>(3);
            list.add(I18n.translateToLocalFormatted("machine.power_out", 
                    mps.deviceDrawWatts()));
            renderContext.drawToolTip(list, mouseX, mouseY);
        }
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        ClientEnergyInfo mpi = this.tileEntity.clientState().powerSupplyInfo;
        if(mpi != null)
        {
            MachineControlRenderer.renderPower(this.renderBounds, mpi, 0xFF);
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
