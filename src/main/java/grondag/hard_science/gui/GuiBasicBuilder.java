package grondag.hard_science.gui;

import grondag.hard_science.gui.control.machine.MachineBufferGauge;
import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.MachineOnOff;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.machines.BasicBuilderTileEntity;
import grondag.hard_science.machines.base.MachineContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBasicBuilder extends AbstractMachineGui<BasicBuilderTileEntity>
{
    public GuiBasicBuilder(BasicBuilderTileEntity containerTileEntity, MachineContainer machineContainer) 
    {
        super(containerTileEntity, machineContainer);
    }

    @Override
    public void addControls()
    {
        this.mainPanel.add(sizeControl(new MachineOnOff(te), MachineControlRenderer.BOUNDS_ON_OFF));
        
        for(RadialGaugeSpec spec : ModModels.BASIC_BUILDER_GAUGE_SPECS)
        {
            this.mainPanel.add(sizeControl(new MachineBufferGauge(te, spec), spec));
        }
    } 
}