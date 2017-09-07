package grondag.hard_science.gui;

import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.machine.MachineBufferGauge;
import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineOnOff;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.machines.BasicBuilderTileEntity;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBasicBuilder extends AbstractSimpleGui<BasicBuilderTileEntity>
{
    public GuiBasicBuilder(BasicBuilderTileEntity containerTileEntity) 
    {
        super(containerTileEntity);
    }

    @Override
    public void addControls(Panel mainPanel, MachineTileEntity tileEntity)
    {
        for(RadialGaugeSpec spec : ModModels.BASIC_BUILDER_GAUGE_SPECS)
        {
            mainPanel.add(sizeControl(mainPanel, new MachineBufferGauge(te, spec), spec));
        }
    } 
}