package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SmartChestTileEntity extends MachineTileEntity
{
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_CHEST.getSampleSprite();
    }

    @Override
    protected AbstractMachine createNewMachine()
    {
        return new SmartChestMachine();
    }
}