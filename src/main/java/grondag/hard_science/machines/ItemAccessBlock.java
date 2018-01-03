package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineContainerBlock;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemAccessBlock extends MachineContainerBlock
{
    public ItemAccessBlock(String name) 
    {
        super(name, ModGui.SMART_CHEST.ordinal(), MachineBlock.creatBasicMachineModelState(Textures.DECAL_MATERIAL_SHORTAGE, Textures.BORDER_CHANNEL_CHECKERBOARD));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) 
    {
        return new MachineTileEntity();
    }
    
    @Override
    public AbstractMachine createNewMachine()
    {
        return new ItemAccessMachine();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_MATERIAL_SHORTAGE.getSampleSprite();
    }

}