package grondag.hard_science.machines;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialRenderBounds;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.init.ModItems;
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
        
        MachineControlRenderer.renderProgress( new RadialRenderBounds(0.22, 0.38, 0.20), te, alpha);
        
        MachineControlRenderer.renderItem(tessellator, buffer, new RadialRenderBounds(0.22, 0.38, 0.20), ModItems.basalt_cobble.getDefaultInstance(), alpha);
        
        String msg = Integer.toString(te.getCurrentBacklog()) + " / " + Integer.toString(te.getMaxBacklog());
        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.2, 0.2, 0.05), msg, HorizontalAlignment.CENTER, alpha);
        
        msg = Integer.toString(te.getJobRemainingTicks()) + " / " + Integer.toString(te.getJobDurationTicks());
        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.3, 0.2, 0.05), msg, HorizontalAlignment.CENTER, alpha);
        
        MachineControlRenderer.renderMachineText(tessellator, buffer, new RenderBounds(0.7, 0.4, 0.2, 0.10), te.getMachineState().name(), HorizontalAlignment.CENTER, alpha);
    }
 

}
