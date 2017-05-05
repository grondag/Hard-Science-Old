package grondag.adversity.superblock.model.state;

import static org.junit.Assert.*;

import org.junit.Test;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.model.painter.SurfacePainter;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.shape.SurfaceType;
import grondag.adversity.superblock.model.state.ModelState.StateValue;
import grondag.adversity.superblock.texture.TextureProvider2;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class ModelStateTest
{

    @Test
    public void test()
    {
        System.out.println("bits0 length = "  + ModelState.PACKER_0.bitLength());
        System.out.println("bits1 length = "  + ModelState.PACKER_1.bitLength());
        System.out.println("bits2 length = "  + ModelState.PACKER_2.bitLength());
        
        StateValue state = new StateValue();
        
        state.setShape(ModelShape.ICOSAHEDRON);
        state.setStatic(true);

        state.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(5));
        state.setLightingMode(0, LightingMode.FULLBRIGHT);
        state.setRenderLayer(0, BlockRenderLayer.SOLID);
        state.setRotationEnabled(0, true);
        state.setSurfacePainter(0, SurfacePainter.SURFACE_TILES);
        state.setSurfaceType(0, SurfaceType.BLOCKFACE);
        state.setTexture(0, Textures.ALL_TEXTURES.get(3));
        state.setAxis(EnumFacing.Axis.Z);
        state.setRotation(Rotation.ROTATE_270);
        state.setBlockVersion(7);
        state.setBigTexIndex(3674);
        state.setSpecies(13);
        state.setSimpleJoin(new SimpleJoin(14));
        state.setCornerJoin(CornerJoinBlockStateSelector.getJoinState(69));
        
        long persistedState[] = state.getBits();
        
        StateValue reloadedState = new StateValue(true, persistedState);
        
        assert(state.equals(reloadedState));
        assert(state.hashCode() == reloadedState.hashCode());
        
        assert(reloadedState.getShape() == ModelShape.ICOSAHEDRON);
        assert(reloadedState.isStatic());

        assert(reloadedState.getColorMap(0) == BlockColorMapProvider.INSTANCE.getColorMap(5));
        assert(reloadedState.getLightingMode(0) == LightingMode.FULLBRIGHT);
        assert(reloadedState.getRenderLayer(0) == BlockRenderLayer.SOLID);
        assert(reloadedState.getRotationEnabled(0) == true);
        assert(reloadedState.getSurfacePainter(0) == SurfacePainter.SURFACE_TILES);
        assert(reloadedState.getSurfaceType(0) == SurfaceType.BLOCKFACE);
        assert(reloadedState.getTexture(0) == Textures.ALL_TEXTURES.get(3));
        assert(reloadedState.getAxis()) == EnumFacing.Axis.Z;
        assert(reloadedState.getRotation()) == Rotation.ROTATE_270;
        assert(reloadedState.getBlockVersion()) == 7;
        assert(reloadedState.getBigTexIndex() == 3674);
        assert(reloadedState.getSpecies() == 13);
        assert(reloadedState.getSimpleJoin().getIndex() == 14);
        assert(reloadedState.getCornerJoin() == CornerJoinBlockStateSelector.getJoinState(69));

       
        assert(reloadedState.getSurfacePainter(1) == SurfacePainter.NONE);
        assert(reloadedState.getSurfacePainter(2) == SurfacePainter.NONE);
        assert(reloadedState.getSurfacePainter(3) == SurfacePainter.NONE);
        
        
    }

}
