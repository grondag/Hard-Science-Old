package grondag.hard_science.machines;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
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
            MachineControlRenderer.renderGauge(spec,  te.getBufferManager().getBuffer(spec.bufferIndex), alpha);
        }
        
        MachineControlRenderer.renderProgress(MachineControlRenderer.BOUNDS_PROGRESS, te, alpha);
        
        if(te.getMachineState() == MachineState.FABRICATING)
        {
            MachineControlRenderer.renderItem(tessellator, buffer, MachineControlRenderer.BOUNDS_PROGRESS_INNER, te.getStatusStack(), alpha);
        }
        else if(te.getMachineState() == MachineState.THINKING && te.getBufferManager().hasFailureCauseClientSideOnly() && MachineControlRenderer.warningLightBlinkOn())
        {
            MachineControlRenderer.renderTextureInBoundsWithColor(tessellator, buffer, MachineControlRenderer.BOUNDS_PROGRESS_INNER, ModModels.TEX_MATERIAL_SHORTAGE, alpha << 24 | 0xFFFF40);
            MachineControlRenderer.renderTextureInBoundsWithColor(tessellator, buffer, MachineControlRenderer.BOUNDS_PROGRESS, ModModels.TEX_GAUGE_MINOR, alpha << 24 | 0xFFFF40);
        }
        
        MachineControlRenderer.renderLinearProgress(tessellator, buffer, new RenderBounds(0.85, 0.2, 0.08, 0.5), 8, 24, false, alpha << 24 | 0xFF4040);
        
        MachineControlRenderer.renderLinearProgress(tessellator, buffer, new RenderBounds(0.2, 0.3, 0.5, 0.08), 8, 24, true, alpha << 24 | 0xFF4040);
        
//        int maxBacklog = te.getMaxBacklog();
//        String msg = Integer.toString(maxBacklog - te.getCurrentBacklog()) + " / " + Integer.toString(maxBacklog);
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.42, 0.5, 0.2, 0.07), msg, HorizontalAlignment.LEFT, alpha);
        
//        msg = Integer.toString(te.getJobRemainingTicks()) + " / " + Integer.toString(te.getJobDurationTicks());
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.3, 0.2, 0.05), msg, HorizontalAlignment.CENTER, alpha);
        
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.4, 0.2, 0.10), te.getMachineState().name(), HorizontalAlignment.CENTER, alpha);
    }
 

}
