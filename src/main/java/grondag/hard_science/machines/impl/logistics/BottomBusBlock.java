package grondag.hard_science.machines.impl.logistics;

import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.world.IBlockTest;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineSimpleBlock;
import grondag.hard_science.movetogether.BlockColorMapProvider;
import grondag.hard_science.movetogether.Chroma;
import grondag.hard_science.movetogether.Hue;
import grondag.hard_science.movetogether.ISuperBlock;
import grondag.hard_science.movetogether.ISuperModelState;
import grondag.hard_science.movetogether.Luminance;
import grondag.hard_science.movetogether.MachineMeshFactory;
import grondag.hard_science.movetogether.ModShapes;
import grondag.hard_science.movetogether.MachineMeshFactory.MachineShape;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.varia.BlockTests;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BottomBusBlock extends MachineSimpleBlock
{
    public BottomBusBlock(String blockName)
    {
        super(blockName, createDefaulModelState());
    }

    private static ISuperModelState createDefaulModelState()
    {
        ISuperModelState result = new ModelState();
        result.setShape(ModShapes.MACHINE);
        MachineMeshFactory.setMachineShape(MachineShape.BOTTOM_BUS, result);
        
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
        return new BottomBusMachine();
    }

    @Override
    public IBlockTest<ISuperModelState> blockJoinTest(IBlockAccess worldIn, IBlockState state, BlockPos pos, ISuperModelState modelState)
    {
        return new BlockTests.SuperBlockCableMatch(this.portLayout(worldIn, pos, state), state.getValue(ISuperBlock.META));
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
