package grondag.hard_science.machines;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

public class BasicBuilderTESR extends MachineTESR
{

    public static final BasicBuilderTESR INSTANCE = new BasicBuilderTESR();
    
    @Override
    protected void renderControlFace(Tessellator tessellator, BufferBuilder buffer, MachineTileEntity te, int alpha)
    {
        for(RadialGaugeSpec spec : ModModels.BASIC_BUILDER_GAUGE_SPECS)
        {
            MachineControlRenderer.renderGauge(spec,  te.getBufferManager().getBuffer(spec.bufferIndex).getLevel(), 1, alpha);
        }
        
        String msg = Integer.toString(te.getCurrentBacklog()) + " / " + Integer.toString(te.getMaxBacklog());
        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.2, 0.2, 0.5, 0.10), msg, HorizontalAlignment.CENTER, alpha);
        
        msg = Integer.toString(te.getJobRemainingTicks()) + " / " + Integer.toString(te.getJobDurationTicks());
        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.2, 0.3, 0.5, 0.10), msg, HorizontalAlignment.CENTER, alpha);
        
        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.2, 0.4, 0.5, 0.10), te.getMachineState().name(), HorizontalAlignment.CENTER, alpha);
    }
 

}
