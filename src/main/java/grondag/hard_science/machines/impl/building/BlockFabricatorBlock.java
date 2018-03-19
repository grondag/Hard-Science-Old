package grondag.hard_science.machines.impl.building;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.init.ModTextures;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFabricatorBlock extends MachineBlock
{
    public BlockFabricatorBlock(String name)
    {
        super(name, ModGui.BLOCK_FABRICATOR.ordinal(), MachineBlock.creatBasicMachineModelState(null, ModTextures.BORDER_FILMSTRIP));
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
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return ModTextures.DECAL_BUILDER.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
