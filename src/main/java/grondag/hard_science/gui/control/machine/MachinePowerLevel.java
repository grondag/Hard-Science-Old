package grondag.hard_science.gui.control.machine;

import java.util.ArrayList;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class MachinePowerLevel extends AbstractMachineControl<MachinePowerLevel, RadialRenderBounds>
{
    
    public MachinePowerLevel(MachineTileEntity tileEntity, RadialRenderBounds bounds)
    {
        super(tileEntity, bounds);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        ArrayList<String> list = new ArrayList<String>(3);
        //FIXME: put back all
//        list.add(I18n.translateToLocalFormatted("machine.power_in", this.tileEntity.getPowerProvider().avgPowerInputWatts()));
        list.add(I18n.translateToLocalFormatted("machine.power_out", this.tileEntity.getPowerSupply().powerOutputWatts()));
//        list.add(I18n.translateToLocalFormatted("machine.power_stored", this.tileEntity.getPowerProvider().availableEnergyJoules()));
        renderContext.drawToolTip(list, mouseX, mouseY);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderPower(this.renderBounds, this.tileEntity.getPowerSupply(), this.tileEntity, 0xFF);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
