package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.control.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MaterialBuffer;
import grondag.hard_science.machines.support.MaterialBufferManager;
import jline.internal.Log;
import net.minecraft.client.Minecraft;

public class MachineBufferGauge extends AbstractMachineControl<MachineBufferGauge>
{
    private RadialGaugeSpec spec;
    private final MaterialBuffer buffer;
    
    public MachineBufferGauge(MachineTileEntity tileEntity, RadialGaugeSpec spec)
    {
        super(tileEntity);
        this.spec = spec;
        MaterialBufferManager mbm = tileEntity.materialBuffer();
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
        double radius = this.width / 2.0;
        this.spec = new RadialGaugeSpec(this.spec.bufferIndex, new RadialRenderBounds(this.left + radius, this.top + radius, radius), 
                this.spec.spriteScale, this.spec.sprite, this.spec.color);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.buffer == null) return;
        //TODO buffer tooltip
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.buffer == null) return;
        MachineControlRenderer.renderGauge(this.spec,  this.buffer.getLevel(), 1, 0xFF);
        
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
