package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.support.Battery;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.machines.support.PolyethyleneFuelCell;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BasicBuilderBlock extends MachineBlock
{
    public BasicBuilderBlock(String name)
    {
        super(name, ModGui.BASIC_BUILDER.ordinal(), MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_FILMSTRIP));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new BasicBuilderTileEntity();
    }

    @Override
    public MachinePowerSupply createDefaultPowerSupply()
    {
        return new MachinePowerSupply(PolyethyleneFuelCell.BASIC_1KW, new Battery(MachinePower.JOULES_PER_KWH, BatteryChemistry.LITHIUM), null);
    }
}
