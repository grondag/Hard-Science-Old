package grondag.hard_science.machines;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.init.ModModels;
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
            MachineControlRenderer.renderGauge(spec, te, te.getBufferManager().getBuffer(spec.bufferIndex), alpha);
        }
        
        MachineControlRenderer.renderFabricationProgress(MachineControlRenderer.BOUNDS_PROGRESS, te, alpha);
        
        MachineControlRenderer.renderLinearProgress(tessellator, buffer, new RenderBounds(0.85, 0.2, 0.08, 0.5), ModModels.TEX_LINEAR_POWER_LEVEL, 8, 24, false, alpha << 24 | 0xFFFFFF);
        
//        MachineControlRenderer.renderLinearProgress(tessellator, buffer, new RenderBounds(0.2, 0.3, 0.5, 0.08),  ModModels.TEX_LINEAR_POWER_LEVEL,8, 24, true, alpha << 24 | 0xFFFFFF);
        
//        int maxBacklog = te.getMaxBacklog();
//        String msg = Integer.toString(maxBacklog - te.getCurrentBacklog()) + " / " + Integer.toString(maxBacklog);
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.42, 0.5, 0.2, 0.07), msg, HorizontalAlignment.LEFT, alpha);
        
//        msg = Integer.toString(te.getJobRemainingTicks()) + " / " + Integer.toString(te.getJobDurationTicks());
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.3, 0.2, 0.05), msg, HorizontalAlignment.CENTER, alpha);
        
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.4, 0.2, 0.10), te.getMachineState().name(), HorizontalAlignment.CENTER, alpha);
    }
 

}
