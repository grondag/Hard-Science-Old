package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.support.Battery;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.machines.support.PolyethyleneFuelCell;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFabricatorBlock extends MachineBlock
{
    public BlockFabricatorBlock(String name)
    {
        super(name, ModGui.BLOCK_FABRICATOR.ordinal(), MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_FILMSTRIP));
    }

    @Override
    protected AbstractMachine createNewMachine()
    {
        return new BlockFabricatorMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new BlockFabricatorTileEntity();
    }

    @Override
    public MachinePowerSupply createDefaultPowerSupply()
    {
        return new MachinePowerSupply(PolyethyleneFuelCell.BASIC_1KW, new Battery(MachinePower.JOULES_PER_KWH, BatteryChemistry.LITHIUM), null);
    }
}
