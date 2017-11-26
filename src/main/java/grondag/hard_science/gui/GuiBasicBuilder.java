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
import grondag.hard_science.machines.BlockFabricatorTileEntity;
import grondag.hard_science.machines.base.MachineTileEntity;
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
                BlockFabricatorTileEntity.BUFFER_INDEX_CYAN, 
                BlockFabricatorTileEntity.BUFFER_INDEX_MAGENTA, 
                BlockFabricatorTileEntity.BUFFER_INDEX_YELLOW, 
                RenderBounds.BOUNDS_BOTTOM_0), RenderBounds.BOUNDS_BOTTOM_0));

        mainPanel.add(sizeControl(mainPanel, new MachineFabricationProgressGauge(te, RenderBounds.BOUNDS_PROGRESS), RenderBounds.BOUNDS_PROGRESS));
        
        if(te.getPowerSupply() != null)
        {
            mainPanel.add(sizeControl(mainPanel, new MachinePowerUsage(te, RenderBounds.BOUNDS_POWER_0), RenderBounds.BOUNDS_POWER_0));
            
            if(te.getPowerSupply().battery() != null)
            {
                mainPanel.add(sizeControl(mainPanel, new MachineBattery(te, RenderBounds.BOUNDS_POWER_1), RenderBounds.BOUNDS_POWER_1));
            }
            
            if(te.getPowerSupply().fuelCell() != null)
            {
                mainPanel.add(sizeControl(mainPanel, new MachineFuelCell(te, RenderBounds.BOUNDS_POWER_2), RenderBounds.BOUNDS_POWER_2));
            }
        }
    } 
}