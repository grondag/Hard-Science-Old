package grondag.adversity.superblock.model.state;

import org.junit.Test;

import grondag.adversity.Output;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.model.painter.SurfacePainter;
import grondag.adversity.superblock.model.painter.surface.SurfaceType;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class ModelStateTest
{

    @Test
    public void test()
    {
        Output.getLog().info("bits0 length = "  + ModelStateFactory.PACKER_0.bitLength());
        Output.getLog().info("bits1 length = "  + ModelStateFactory.PACKER_1.bitLength());
        Output.getLog().info("bits2 length = "  + ModelStateFactory.PACKER_2.bitLength());
        
        Output.getLog().info("bits3 block length = "  + ModelStateFactory.PACKER_3_BLOCK.bitLength());
        Output.getLog().info("bits3 flow length = "  + ModelStateFactory.PACKER_3_FLOW.bitLength());
        
        ModelState state = new ModelState();
        
        state.setShape(ModelShape.COLUMN_SQUARE);
        state.setStatic(true);

        state.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(5));
        state.setLightingMode(0, LightingMode.FULLBRIGHT);
        state.setRenderLayer(0, BlockRenderLayer.CUTOUT_MIPPED);
        state.setRenderLayer(1, BlockRenderLayer.TRANSLUCENT);
        state.setRotationEnabled(0, true);
        state.setSurfacePainter(0, SurfacePainter.SURFACE_TILES);
        state.setSurfacePainter(1, SurfacePainter.SURFACE_CYLINDER);
        state.setSurfaceType(0, SurfaceType.BLOCKFACE);
        state.setTexture(0, Textures.ALL_TEXTURES.get(3));
        state.setAxis(EnumFacing.Axis.Z);
        state.setRotation(Rotation.ROTATE_270);
        state.setBlockVersion(7);
        state.setPosX(3);
        state.setPosY(7);
        state.setPosZ(15);
        state.setSpecies(13);
        state.setCornerJoin(CornerJoinBlockStateSelector.getJoinState(69));
        state.setAxisInverted(true);
        state.setStaticShapeBits(879579585L);
        
        int persistedState[] = state.getBits();
        
        ModelState reloadedState = new ModelState(persistedState);
        
        assert(state.equals(reloadedState));
        assert(state.hashCode() == reloadedState.hashCode());
        
        assert(reloadedState.getShape() == ModelShape.COLUMN_SQUARE);
        assert(reloadedState.isStatic());

        assert(reloadedState.getColorMap(0) == BlockColorMapProvider.INSTANCE.getColorMap(5));
        assert(reloadedState.getLightingMode(0) == LightingMode.FULLBRIGHT);
        assert(reloadedState.getRenderLayer(0) == BlockRenderLayer.CUTOUT_MIPPED);
        assert(reloadedState.getRenderLayer(1) == BlockRenderLayer.TRANSLUCENT);
        assert(reloadedState.getRotationEnabled(0) == true);
        assert(reloadedState.getSurfacePainter(0) == SurfacePainter.SURFACE_TILES);
        assert(reloadedState.getSurfacePainter(1) == SurfacePainter.SURFACE_CYLINDER);
        assert(reloadedState.getSurfaceType(0) == SurfaceType.BLOCKFACE);
        assert(reloadedState.getTexture(0) == Textures.ALL_TEXTURES.get(3));
        assert(reloadedState.getAxis()) == EnumFacing.Axis.Z;
        assert(reloadedState.getRotation()) == Rotation.ROTATE_270;
        assert(reloadedState.getBlockVersion()) == 7;
        assert(reloadedState.getPosX() == 3);
        assert(reloadedState.getPosY() == 7);
        assert(reloadedState.getPosZ() == 15);
        assert(reloadedState.getSpecies() == 13);
        assert(reloadedState.getCornerJoin() == CornerJoinBlockStateSelector.getJoinState(69));
        assert(reloadedState.getSimpleJoin().getIndex() == CornerJoinBlockStateSelector.getJoinState(69).simpleJoin.getIndex());
        assert(reloadedState.isAxisInverted());
        assert(reloadedState.getStaticShapeBits() == 879579585L);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.SOLID) == false);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.CUTOUT) == false);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.CUTOUT_MIPPED) == true);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.TRANSLUCENT) == true);

       
        assert(reloadedState.getSurfacePainter(2) == SurfacePainter.NONE);
        assert(reloadedState.getSurfacePainter(3) == SurfacePainter.NONE);
        
        
    }

}
