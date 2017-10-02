package grondag.hard_science.machines;

import grondag.hard_science.machines.base.MachineStorageTileEntity;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SmartChestTileEntity extends MachineStorageTileEntity
{
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_CHEST.getSampleSprite();
    }
    
    @Override
    public boolean hasOnOff() { return false;}
    
    @Override
    public boolean hasRedstoneControl() { return false; }
}