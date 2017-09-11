package grondag.hard_science.gui.control.machine;

import grondag.hard_science.Log;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialRenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MaterialBuffer;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import net.minecraft.client.Minecraft;

public class MachineFabricationProgressGauge extends AbstractMachineControl<MachineFabricationProgressGauge>
{

    
    public MachineFabricationProgressGauge(MachineTileEntity tileEntity)
    {
        super(tileEntity);
    }
    
 
    @Override
    protected void handleCoordinateUpdate()
    {
        super.handleCoordinateUpdate();
        double radius = this.renderBounds.height() / 2;
        this.renderBounds = new RadialRenderBounds(this.renderBounds.left() + radius, this.renderBounds.top() + radius, radius);
    }


    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        String key;
        if(this.tileEntity.getMachineState() == MachineState.FABRICATING)
        {
            key = "machine.fabrication_progress";
        }
        else if(this.tileEntity.getMachineState() == MachineState.THINKING && this.tileEntity.getBufferManager().hasFailureCauseClientSideOnly())
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
        MachineControlRenderer.renderFabricationProgress((RadialRenderBounds)this.renderBounds, this.tileEntity, 0xFF);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
