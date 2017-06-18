package grondag.adversity.superblock.model.state;

import org.junit.Test;

import grondag.adversity.Log;
import grondag.adversity.library.render.LightingMode;
import grondag.adversity.library.world.CornerJoinBlockStateSelector;
import grondag.adversity.library.world.Rotation;
import grondag.adversity.superblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureScale;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class ModelStateTest
{

    @Test
    public void test()
    {
        Log.info("bits0 length = "  + ModelStateFactory.PACKER_0.bitLength());
        Log.info("bits1 length = "  + ModelStateFactory.PACKER_1.bitLength());
        Log.info("bits2 length = "  + ModelStateFactory.PACKER_2.bitLength());
        
        Log.info("bits3 block length = "  + ModelStateFactory.PACKER_3_BLOCK.bitLength());
        Log.info("bits3 flow length = "  + ModelStateFactory.PACKER_3_FLOW.bitLength());
        
        // sign bit on first long is used to store static indicator
        assert(ModelStateFactory.PACKER_0.bitLength() < 64);
        
        ModelState state = new ModelState();
        
        state.setShape(ModelShape.COLUMN_SQUARE);
        state.setStatic(true);

        state.setOverlayLayerEnabled(true);
        state.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(5));
        state.setColorMap(PaintLayer.DETAIL, BlockColorMapProvider.INSTANCE.getColorMap(7));
        state.setLightingMode(PaintLayer.BASE, LightingMode.FULLBRIGHT);
        state.setLightingMode(PaintLayer.OVERLAY, LightingMode.SHADED);
        state.setRenderLayer(PaintLayer.LAMP, BlockRenderLayer.SOLID);
        state.setRenderLayer(PaintLayer.BASE, BlockRenderLayer.TRANSLUCENT);
        state.setTexture(PaintLayer.BASE, Textures.BLOCK_NOISE_STRONG);
        state.setTexture(PaintLayer.OVERLAY, Textures.BORDER_SMOOTH_BLEND);
        state.setAxis(EnumFacing.Axis.Z);
        state.setTranslucency(Translucency.SHADED);
        state.setTextureRotation(Rotation.ROTATE_270, TextureScale.SINGLE);
        state.setBlockVersion(7, TextureScale.SINGLE);
        state.setPosX(3);
        state.setPosY(7);
        state.setPosZ(15);
        state.setSpecies(13);
        state.setCornerJoin(CornerJoinBlockStateSelector.getJoinState(69));
        state.setAxisInverted(true);
        state.setStaticShapeBits(879579585L);
        
        int persistedState[] = state.getBitsIntArray();
        
        ModelState reloadedState = new ModelState(persistedState);
        
        assert(state.equals(reloadedState));
        assert(state.hashCode() == reloadedState.hashCode());
        
        assert(reloadedState.getShape() == ModelShape.COLUMN_SQUARE);
        assert(reloadedState.isStatic());
        assert(reloadedState.isOverlayLayerEnabled());
        assert(!reloadedState.isDetailLayerEnabled());
        assert(reloadedState.isLayerShaded(BlockRenderLayer.TRANSLUCENT) == false);
        assert(reloadedState.getColorMap(PaintLayer.BASE) == BlockColorMapProvider.INSTANCE.getColorMap(5));
        assert(reloadedState.getColorMap(PaintLayer.DETAIL) == BlockColorMapProvider.INSTANCE.getColorMap(7));
        assert(reloadedState.getLightingMode(PaintLayer.BASE) == LightingMode.FULLBRIGHT);
        assert(reloadedState.getLightingMode(PaintLayer.DETAIL) == LightingMode.SHADED);
        assert(reloadedState.getRenderLayer(PaintLayer.LAMP) == BlockRenderLayer.SOLID);
        assert(reloadedState.getRenderLayer(PaintLayer.OVERLAY) == BlockRenderLayer.TRANSLUCENT);
        assert(reloadedState.getTexture(PaintLayer.BASE) == Textures.BLOCK_NOISE_STRONG);
        assert(reloadedState.getTexture(PaintLayer.OVERLAY) == Textures.BORDER_SMOOTH_BLEND);
        assert(reloadedState.getAxis()) == EnumFacing.Axis.Z;
        assert(reloadedState.getTranslucency()) == Translucency.SHADED;
        assert(reloadedState.getTextureRotation(TextureScale.SINGLE)) == Rotation.ROTATE_270;
        assert(reloadedState.getBlockVersion(TextureScale.SINGLE)) == 7;
        assert(reloadedState.getPosX() == 3);
        assert(reloadedState.getPosY() == 7);
        assert(reloadedState.getPosZ() == 15);
        assert(reloadedState.getSpecies() == 13);
        assert(reloadedState.getCornerJoin() == CornerJoinBlockStateSelector.getJoinState(69));
        assert(reloadedState.getSimpleJoin().getIndex() == CornerJoinBlockStateSelector.getJoinState(69).simpleJoin.getIndex());
        assert(reloadedState.isAxisInverted());
        assert(reloadedState.getStaticShapeBits() == 879579585L);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.SOLID) == true);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.CUTOUT) == false);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.CUTOUT_MIPPED) == false);
        assert(reloadedState.canRenderInLayer(BlockRenderLayer.TRANSLUCENT) == true);
        
        int flags = reloadedState.getRenderLayerShadedFlags();
        assert(ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(BlockRenderLayer.SOLID, flags));
        assert(!ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(BlockRenderLayer.TRANSLUCENT, flags));


        
        
    }

}
