package grondag.hard_science.gui.control.machine;

import grondag.hard_science.Log;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.MaterialBufferManager.MaterialBufferDelegate;
import net.minecraft.client.Minecraft;

public class MachineBufferGauge extends AbstractMachineControl<MachineBufferGauge, RadialRenderBounds>
{
    private RadialGaugeSpec spec;
    private final MaterialBufferDelegate buffer;
    
    public MachineBufferGauge(MachineTileEntity tileEntity, RadialGaugeSpec spec)
    {
        super(tileEntity, spec);
        this.spec = spec;
        MaterialBufferManager mbm = tileEntity.getBufferManager();
        if(mbm == null || spec.bufferIndex >= mbm.bufferCount()) 
        {
            Log.warn("Machine buffer gauge GUI could not be initialized.  Bad buffer of buffer index.");
            this.buffer = null;
        }
        else
        {
            this.buffer = mbm.getBuffer(spec.bufferIndex);
        }
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        super.handleCoordinateUpdate();
        this.spec = new RadialGaugeSpec(this.spec.bufferIndex, spec.scale(this.left, this.top, this.width, this.height),
                this.spec.spriteScale, this.spec.sprite, this.spec.color, this.spec.rotation, this.spec.formula, this.spec.formulaColor);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.buffer == null) return;
        renderContext.drawLocalizedToolTip(mouseX, mouseY, buffer.tooltipKey(), String.format("%,.9fL", buffer.getLevelNanoLiters() / 1000000000.0));
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.buffer == null) return;
        MachineControlRenderer.renderGauge(this.spec, this.tileEntity, this.buffer, 0xFF);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
