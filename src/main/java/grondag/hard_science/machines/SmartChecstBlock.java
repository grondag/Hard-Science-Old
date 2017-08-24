package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.MachineBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SmartChecstBlock extends MachineBlock
{
    public SmartChecstBlock(String name) 
    {
        super(name, ModGui.SMART_CHEST.ordinal());
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SmartChestTileEntity();
    }
}