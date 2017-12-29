package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineStorageBlock;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TransportTestBlock extends MachineStorageBlock
{
    public TransportTestBlock(String name) 
    {
        super(name, ModGui.SMART_CHEST.ordinal(), MachineBlock.creatBasicMachineModelState(Textures.DECAL_FAT_DIAGONAL_BARS, Textures.BORDER_GRITTY_FAT_LINE));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) 
    {
        return new SmartChestTileEntity();
    }
    
    @Override
    public AbstractMachine createNewMachine()
    {
        return new TransportTestMachine();
    }
}