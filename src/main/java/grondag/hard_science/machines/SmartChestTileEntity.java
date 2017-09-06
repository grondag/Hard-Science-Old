package grondag.hard_science.machines;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.machines.base.MachineStorageTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SmartChestTileEntity extends MachineStorageTileEntity
{
    
    @SideOnly(Side.CLIENT)
    @Override
    public int getSymbolGlTextureId()
    {
        return ModModels.TEX_SYMBOL_CHEST;
    }
}