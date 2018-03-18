package grondag.hard_science.machines.impl.production;

import grondag.exotic_matter.model.PaintLayer;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineSimpleBlock;
import grondag.hard_science.movetogether.BlockColorMapProvider;
import grondag.hard_science.movetogether.Chroma;
import grondag.hard_science.movetogether.Hue;
import grondag.hard_science.movetogether.ISuperModelState;
import grondag.hard_science.movetogether.Luminance;
import grondag.hard_science.movetogether.MachineMeshFactory;
import grondag.hard_science.movetogether.ModShapes;
import grondag.hard_science.movetogether.MachineMeshFactory.MachineShape;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.texture.Textures;

public class PhotoElectricBlock extends MachineSimpleBlock
{
    public PhotoElectricBlock(String blockName)
    {
        super(blockName, createDefaulModelState());
    }

    private static ISuperModelState createDefaulModelState()
    {
        ISuperModelState result = new ModelState();
        result.setShape(ModShapes.MACHINE);
        MachineMeshFactory.setMachineShape(MachineShape.PHOTOELECTRIC_CELL, result);
        
        // top is main, sides/bottom are lamp
        result.setTexture(PaintLayer.BASE, Textures.BLOCK_NOISE_SUBTLE_ZOOM);
        result.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VIOLET, Chroma.WHITE, Luminance.EXTRA_DARK));
        
        result.setTexture(PaintLayer.OUTER, Textures.BORDER_GRITTY_INSET_PINSTRIPE);
        result.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VIOLET, Chroma.NEUTRAL, Luminance.DARK));
        
        result.setTexture(PaintLayer.LAMP, Textures.BLOCK_NOISE_SUBTLE);
        result.setColorMap(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.DARK));

//        result.setStatic(true);
        return result;
    }
    
    @Override
    public AbstractMachine createNewMachine()
    {
        return new PhotoElectricMachine();
        
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.solar_cell;
    }
}
