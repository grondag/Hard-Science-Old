package grondag.hard_science.machines.impl.logistics;

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

public class ChemicalBatteryBlock extends MachineBlock
{
    public ChemicalBatteryBlock(String name)
    {
        super(name, ModGui.GLASS_BATTERY.ordinal(), 
                MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_INVERSE_ZIGZAG));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new ChemicalBatteryMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new ChemicalBatteryTileEntity();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_ELECTRICITY.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.power_low_carrier_flex_all;
    }
}
