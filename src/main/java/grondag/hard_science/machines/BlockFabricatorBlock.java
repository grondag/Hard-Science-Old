package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
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
    public AbstractMachine createNewMachine()
    {
        return new BlockFabricatorMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new BlockFabricatorTileEntity();
    }
}
