package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SmartChestBlock extends MachineBlock
{
    public SmartChestBlock(String name) 
    {
        super(name, ModGui.SMART_CHEST.ordinal(), MachineBlock.creatBasicMachineModelState(Textures.SYMBOL_CHEST));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) 
    {
        return new SmartChestTileEntity();
    }
    

}