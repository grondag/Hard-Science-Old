package grondag.hard_science.machines.impl.processing;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CrushinatorBlock extends MachineBlock
{
    public CrushinatorBlock(String name)
    {
        super(name, ModGui.CRUSHINATOR.ordinal(), MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_FILMSTRIP));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new CrushinatorMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new CrushinatorTileEntity();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_BUILDER.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
