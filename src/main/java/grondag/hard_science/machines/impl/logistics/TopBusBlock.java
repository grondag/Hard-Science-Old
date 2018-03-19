package grondag.hard_science.machines.impl.logistics;

import grondag.exotic_matter.model.BlockColorMapProvider;
import grondag.exotic_matter.model.Chroma;
import grondag.exotic_matter.model.Hue;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.Luminance;
import grondag.exotic_matter.model.PaintLayer;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.init.ModTextures;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineSimpleBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.model.shape.machine.MachineMeshFactory;
import grondag.hard_science.superblock.model.shape.machine.MachineMeshFactory.MachineShape;
import grondag.hard_science.superblock.model.state.ModelState;

public class TopBusBlock extends MachineSimpleBlock
{
    public TopBusBlock(String blockName)
    {
        super(blockName, createDefaulModelState());
    }

    private static ISuperModelState createDefaulModelState()
    {
        ISuperModelState result = new ModelState();
        result.setShape(grondag.hard_science.init.ModShapes.MACHINE);
        MachineMeshFactory.setMachineShape(MachineShape.MIDDLE_BUS, result);
        
        result.setTexture(PaintLayer.BASE, ModTextures.BLOCK_NOISE_SUBTLE);
        result.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT));
        
        result.setTexture(PaintLayer.OUTER, ModTextures.BORDER_SIGNAL);
        result.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VIOLET, Chroma.NEUTRAL, Luminance.MEDIUM_DARK));
        
//        result.setTexture(PaintLayer.LAMP, Textures.TILE_DOTS_INVERSE);
//        result.setColorMap(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.DARK));

//        result.setStatic(true);
        return result;
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new TopBusMachine();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_top_bridge_all;
    }
}
