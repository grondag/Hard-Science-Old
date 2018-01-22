package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SolarAggregatorBlock extends MachineBlock
{
    public SolarAggregatorBlock(String name)
    {
        super(name, ModGui.SOLAR_AGGREGATOR.ordinal(), MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_INVERSE_ZIGZAG));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new SolarAggregatorMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new SolarAggregatorTileEntity();
    }
    
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_STAR_12.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.empty;
    }
}
