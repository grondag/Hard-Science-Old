package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineStorageBlock;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SmartChestBlock extends MachineStorageBlock
{
    public SmartChestBlock(String name) 
    {
        super(name, ModGui.SMART_CHEST.ordinal(), MachineBlock.creatBasicMachineModelState(Textures.DECAL_SKINNY_DIAGNAL_CROSS_BARS, Textures.BORDER_SINGLE_BOLD_LINE));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) 
    {
        return new MachineTileEntityTickable();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_CHEST.getSampleSprite();
    }

}