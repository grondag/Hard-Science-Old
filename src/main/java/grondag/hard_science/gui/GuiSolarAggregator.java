package grondag.hard_science.gui;

import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.machine.MachineBufferGauge;
import grondag.hard_science.gui.control.machine.RadialGaugeSpec;
import grondag.hard_science.machines.SolarAggregatorTileEntity;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSolarAggregator extends AbstractSimpleGui<SolarAggregatorTileEntity>
{
    public GuiSolarAggregator(SolarAggregatorTileEntity containerTileEntity) 
    {
        super(containerTileEntity);
    }

    @Override
    public void addControls(Panel mainPanel, MachineTileEntity tileEntity)
    {
        for(RadialGaugeSpec spec : SolarAggregatorTileEntity.GAUGE_SPECS)
        {
            mainPanel.add(sizeControl(mainPanel, new MachineBufferGauge(te, spec), spec));
        }
    } 
}