package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BasicBuilderBlock extends MachineBlock
{
    public BasicBuilderBlock(String name)
    {
        super(name, ModGui.BASIC_BUILDER.ordinal(), MachineBlock.creatBasicMachineModelState(Textures.SYMBOL_BUILDER));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new BasicBuilderTileEntity();
    }
}
