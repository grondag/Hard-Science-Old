package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SolarAggregatorBlock extends MachineBlock
{
    public SolarAggregatorBlock(String name)
    {
        super(name, ModGui.SOLAR_AGGREGATOR.ordinal(), MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_INVERSE_ZIGZAG));
    }

    @Override
    protected AbstractMachine createNewMachine()
    {
        return new SolarAggregatorMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new SolarAggregatorTileEntity();
    }

    @Override
    public MachinePowerSupply createDefaultPowerSupply()
    {
        return null;
    }
}
