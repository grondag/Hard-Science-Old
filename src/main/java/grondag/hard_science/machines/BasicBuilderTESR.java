package grondag.hard_science.machines;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
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
            MachineControlRenderer.renderGauge(spec,  te.materialBuffer().getBuffer(spec.bufferIndex).getLevel(), 1, alpha);
        }
    }
 

}
