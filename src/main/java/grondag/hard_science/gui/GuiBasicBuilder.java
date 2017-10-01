package grondag.hard_science.gui;

import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.machine.MachineBufferGauge;
import grondag.hard_science.gui.control.machine.MachineFabricationProgressGauge;
import grondag.hard_science.gui.control.machine.MachinePowerLevel;
import grondag.hard_science.gui.control.machine.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.RenderBounds;
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
        for(RadialGaugeSpec spec : BasicBuilderTileEntity.BASIC_BUILDER_GAUGE_SPECS)
        {
            mainPanel.add(sizeControl(mainPanel, new MachineBufferGauge(te, spec), spec));
        }
        mainPanel.add(sizeControl(mainPanel, new MachineFabricationProgressGauge(te, RenderBounds.BOUNDS_PROGRESS), RenderBounds.BOUNDS_PROGRESS));
        
        mainPanel.add(sizeControl(mainPanel, new MachinePowerLevel(te, RenderBounds.BOUNDS_POWER_0), RenderBounds.BOUNDS_POWER_0));
    } 
}