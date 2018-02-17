package grondag.hard_science.gui;

import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.machine.MachineBattery;
import grondag.hard_science.gui.control.machine.MachineBufferGauge;
import grondag.hard_science.gui.control.machine.MachineCMYGauge;
import grondag.hard_science.gui.control.machine.MachineFabricationProgressGauge;
import grondag.hard_science.gui.control.machine.MachineFuelCell;
import grondag.hard_science.gui.control.machine.MachinePowerUsage;
import grondag.hard_science.gui.control.machine.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.energy.DeviceEnergyInfo;
import grondag.hard_science.machines.impl.building.BlockFabricatorMachine;
import grondag.hard_science.machines.impl.building.BlockFabricatorTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBasicBuilder extends AbstractSimpleGui<BlockFabricatorTileEntity>
{
    public GuiBasicBuilder(BlockFabricatorTileEntity containerTileEntity) 
    {
        super(containerTileEntity);
    }

    @Override
    public void addControls(Panel mainPanel, MachineTileEntity tileEntity)
    {
        //FIXME:  sizeControl nonsense to indirectly scale render bounds is uuuggglly. 
        // should just scale the bounds being passed in on construction, or at least
        // don't pass the same bounds twice as arguments each line.
        
        for(RadialGaugeSpec spec : BlockFabricatorTileEntity.BASIC_BUILDER_GAUGE_SPECS)
        {
            mainPanel.add(sizeControl(mainPanel, new MachineBufferGauge(te, spec), spec));
        }
        
        mainPanel.add(sizeControl(mainPanel, new MachineCMYGauge(te,
                BlockFabricatorMachine.BUFFER_INDEX_CYAN, 
                BlockFabricatorMachine.BUFFER_INDEX_MAGENTA, 
                BlockFabricatorMachine.BUFFER_INDEX_YELLOW, 
                RenderBounds.BOUNDS_BOTTOM_0), RenderBounds.BOUNDS_BOTTOM_0));

        mainPanel.add(sizeControl(mainPanel, new MachineFabricationProgressGauge(te, RenderBounds.BOUNDS_PROGRESS), RenderBounds.BOUNDS_PROGRESS));
        
        DeviceEnergyInfo ps = te.clientState().powerSupplyInfo;
        
        if(ps != null)
        {
            mainPanel.add(sizeControl(mainPanel, new MachinePowerUsage(te, RenderBounds.BOUNDS_POWER_0), RenderBounds.BOUNDS_POWER_0));
            
            mainPanel.add(sizeControl(mainPanel, new MachineBattery(te, RenderBounds.BOUNDS_POWER_1), RenderBounds.BOUNDS_POWER_1));
            
            if(ps.hasGenerator())
            {
                mainPanel.add(sizeControl(mainPanel, new MachineFuelCell(te, RenderBounds.BOUNDS_POWER_2), RenderBounds.BOUNDS_POWER_2));
            }
        }
    } 
}