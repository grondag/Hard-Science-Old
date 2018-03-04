package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineFabricationProgressGauge extends AbstractMachineControl<MachineFabricationProgressGauge, RadialRenderBounds>
{
    
    public MachineFabricationProgressGauge(MachineTileEntity tileEntity, RadialRenderBounds bounds)
    {
        super(tileEntity, bounds);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        String key;
        if(this.tileEntity.clientState().controlState.getMachineState() == MachineState.FABRICATING)
        {
            key = "machine.fabrication_progress";
        }
        else if(this.tileEntity.clientState().controlState.getMachineState() == MachineState.THINKING 
                && this.tileEntity.clientState().bufferInfo.hasFailureCause())
        {
            key = "machine.fabrication_shortage";
        }
        else
        {
            key = "machine.fabrication_searching";
        }
        
        renderContext.drawLocalizedToolTip(mouseX, mouseY, key);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderFabricationProgress(this.renderBounds, this.tileEntity, 0xFF);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
