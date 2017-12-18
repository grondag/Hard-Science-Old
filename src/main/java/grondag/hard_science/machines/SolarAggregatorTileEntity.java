package grondag.hard_science.machines;

import grondag.hard_science.gui.control.machine.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.materials.MatterColors;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SolarAggregatorTileEntity extends MachineTileEntity
{
    public static final int BUFFER_INDEX_HDPE = 0;
    public static final int BUFFER_INDEX_ETHANOL = BUFFER_INDEX_HDPE + 1;
    
     @SideOnly(value = Side.CLIENT)
    public static final RadialGaugeSpec[] GAUGE_SPECS = new RadialGaugeSpec[2];

    @SideOnly(value = Side.CLIENT)
    public static void initRenderSpecs()
    {
        GAUGE_SPECS[BUFFER_INDEX_ETHANOL] = new RadialGaugeSpec(BUFFER_INDEX_ETHANOL, 
                RenderBounds.BOUNDS_DUAL_MEDIUM_LEFT, 1.2, 
                Textures.DECAL_LARGE_DOT.getSampleSprite(), MatterColors.ETHANOL, Rotation.ROTATE_NONE,
                "C2H6O", 0xFFFFFFFF);
        
        GAUGE_SPECS[BUFFER_INDEX_HDPE] = new RadialGaugeSpec(BUFFER_INDEX_HDPE, 
                RenderBounds.BOUNDS_DUAL_MEDIUM_RIGHT, 1.4, 
                Textures.DECAL_LARGE_DOT.getSampleSprite(), MatterColors.HDPE, Rotation.ROTATE_NONE,
                "C2H4", 0xFF000000);
    }
    
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_STAR_12.getSampleSprite();
    }

    @Override
    protected AbstractMachine createNewMachine()
    {
        return new SolarAggregatorMachine();
    }
}
