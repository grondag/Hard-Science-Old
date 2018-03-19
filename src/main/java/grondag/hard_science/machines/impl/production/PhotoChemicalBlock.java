package grondag.hard_science.machines.impl.production;

import grondag.exotic_matter.model.BlockColorMapProvider;
import grondag.exotic_matter.model.Chroma;
import grondag.exotic_matter.model.Hue;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.Luminance;
import grondag.exotic_matter.model.ModelState;
import grondag.exotic_matter.model.PaintLayer;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.init.ModTextures;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineSimpleBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.model.shape.machine.MachineMeshFactory;
import grondag.hard_science.superblock.model.shape.machine.MachineMeshFactory.MachineShape;

public class PhotoChemicalBlock extends MachineSimpleBlock
{
    public PhotoChemicalBlock(String blockName)
    {
        super(blockName, createDefaulModelState());
    }

    private static ISuperModelState createDefaulModelState()
    {
        ISuperModelState result = new ModelState();
        result.setShape(grondag.hard_science.init.ModShapes.MACHINE);
        MachineMeshFactory.setMachineShape(MachineShape.PHOTOCHEM_CELL, result);
        
        // top is main, sides/bottom are lamp
        result.setTexture(PaintLayer.BASE, grondag.exotic_matter.init.ModTextures.BLOCK_NOISE_SUBTLE);
        result.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VIOLET, Chroma.WHITE, Luminance.EXTRA_DARK));
        
        result.setTexture(PaintLayer.OUTER, ModTextures.BORDER_GRITTY_INSET_PINSTRIPE);
        result.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VIOLET, Chroma.NEUTRAL, Luminance.DARK));
        
        result.setTexture(PaintLayer.LAMP, ModTextures.TILE_DOTS_INVERSE);
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
        // TODO implement
        return ModPortLayouts.empty;
    }
}
