package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.base.MachineSimpleBlock;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.Chroma;
import grondag.hard_science.superblock.color.Hue;
import grondag.hard_science.superblock.color.Luminance;
import grondag.hard_science.superblock.model.shape.MachineMeshFactory;
import grondag.hard_science.superblock.model.shape.MachineMeshFactory.MachineShape;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.texture.Textures;

public class ExtensionBusBlock extends MachineSimpleBlock
{
    public ExtensionBusBlock(String blockName)
    {
        super(blockName, createDefaulModelState());
    }

    private static ModelState createDefaulModelState()
    {
        ModelState result = new ModelState();
        result.setShape(ModelShape.MACHINE);
        MachineMeshFactory.setMachineShape(MachineShape.EXTENSION_BUS, result);
        
        result.setTexture(PaintLayer.BASE, Textures.BLOCK_NOISE_SUBTLE);
        result.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.DARK));
        
//        result.setTexture(PaintLayer.OUTER, Textures.BORDER_GRITTY_INSET_PINSTRIPE);
//        result.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VIOLET, Chroma.NEUTRAL, Luminance.DARK));
        
//        result.setTexture(PaintLayer.LAMP, Textures.TILE_DOTS_INVERSE);
//        result.setColorMap(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.DARK));

//        result.setStatic(true);
        return result;
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new AbstractSimpleMachine()
        {

            @Override
            public long onProduce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
            {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long onConsume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
            {
                // TODO Auto-generated method stub
                return 0;
            }
        };
    }
}
