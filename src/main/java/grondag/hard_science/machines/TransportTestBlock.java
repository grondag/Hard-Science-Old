package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineStorageBlock;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TransportTestBlock extends MachineStorageBlock
{
    private final ItemStack stack;
    
    public TransportTestBlock(String name, ItemStack stack) 
    {
        super(name, ModGui.SMART_CHEST.ordinal(), MachineBlock.creatBasicMachineModelState(Textures.DECAL_FAT_DIAGONAL_BARS, Textures.BORDER_GRITTY_FAT_LINE));
        this.stack = stack;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) 
    {
        return new MachineTileEntityTickable();
    }
    
    @Override
    public AbstractMachine createNewMachine()
    {
        return new TransportTestMachine(stack);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_CHEST.getSampleSprite();
    }
}