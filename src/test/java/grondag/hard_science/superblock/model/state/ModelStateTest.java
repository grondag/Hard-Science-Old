package grondag.hard_science.superblock.model.state;

import org.junit.Test;

import grondag.hard_science.Log;
import grondag.hard_science.library.world.CornerJoinBlockStateSelector;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public class ModelStateTest
{

    @Test
    public void test()
    {
        Log.info("Max shapes within current format: " + MathHelper.smallestEncompassingPowerOfTwo(ModelShape.values().length));
        Log.info("bits0 length = "  + ModelStateFactory.PACKER_0.bitLength());
        Log.info("bits1 length = "  + ModelStateFactory.PACKER_1.bitLength());
        Log.info("bits2 length = "  + ModelStateFactory.PACKER_2.bitLength());
        
        Log.info("bits3 block length = "  + ModelStateFactory.PACKER_3_BLOCK.bitLength());
        Log.info("bits3 flow length = "  + ModelStateFactory.PACKER_3_FLOW.bitLength());
        
        // sign bit on third long is used to store static indicator
        assert(ModelStateFactory.PACKER_2.bitLength() < 64);
        
        ModelState state = new ModelState();
        
        state.setShape(ModelShape.COLUMN_SQUARE);
        state.setStatic(true);

        state.setOuterLayerEnabled(true);
        state.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(5));
        state.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(7));
        state.setFullBrightness(PaintLayer.LAMP, true);
        state.setTexture(PaintLayer.BASE, Textures.BLOCK_NOISE_STRONG);
        state.setTexture(PaintLayer.LAMP, Textures.BLOCK_COBBLE);
        state.setTexture(PaintLayer.MIDDLE, Textures.DECAL_DIAGONAL_CROSS_BARS);
        state.setTexture(PaintLayer.OUTER, Textures.BORDER_SMOOTH_BLEND);
        state.setAxis(EnumFacing.Axis.Z);
        state.setTranslucent(PaintLayer.MIDDLE, true);
        state.setTranslucency(Translucency.SHADED);
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
        assert(reloadedState.isOuterLayerEnabled());
        assert(reloadedState.isMiddleLayerEnabled());
        assert(reloadedState.getColorMap(PaintLayer.BASE) == BlockColorMapProvider.INSTANCE.getColorMap(5));
        assert(reloadedState.getColorMap(PaintLayer.OUTER) == BlockColorMapProvider.INSTANCE.getColorMap(7));
        assert(reloadedState.isFullBrightness(PaintLayer.LAMP));
        assert(reloadedState.getTexture(PaintLayer.BASE) == Textures.BLOCK_NOISE_STRONG);
        assert(reloadedState.getTexture(PaintLayer.LAMP) == Textures.BLOCK_COBBLE);
        assert(reloadedState.getTexture(PaintLayer.MIDDLE) == Textures.DECAL_DIAGONAL_CROSS_BARS);
        assert(reloadedState.getTexture(PaintLayer.OUTER) == Textures.BORDER_SMOOTH_BLEND);
        assert(reloadedState.getAxis()) == EnumFacing.Axis.Z;
        assert(reloadedState.getTranslucency()) == Translucency.SHADED;
        assert(reloadedState.getPosX() == 3);
        assert(reloadedState.getPosY() == 7);
        assert(reloadedState.getPosZ() == 15);
        assert(reloadedState.getSpecies() == 13);
        assert(reloadedState.getCornerJoin() == CornerJoinBlockStateSelector.getJoinState(69));
        assert(reloadedState.getSimpleJoin().getIndex() == CornerJoinBlockStateSelector.getJoinState(69).simpleJoin.getIndex());
        assert(reloadedState.isAxisInverted());
        assert(reloadedState.getStaticShapeBits() == 879579585L);
        RenderPassSet rps = reloadedState.getRenderPassSet();
        assert(rps.renderLayout.containsBlockRenderLayer(BlockRenderLayer.SOLID) == true);
        assert(rps.renderLayout.containsBlockRenderLayer(BlockRenderLayer.CUTOUT) == false);
        assert(rps.renderLayout.containsBlockRenderLayer(BlockRenderLayer.CUTOUT_MIPPED) == false);
        assert(rps.renderLayout.containsBlockRenderLayer(BlockRenderLayer.TRANSLUCENT) == true);
        

        assert(rps.renderLayout.containsRenderPass(RenderPass.SOLID_SHADED));
        assert(rps.renderLayout.containsRenderPass(RenderPass.SOLID_FLAT));
        assert(rps.renderLayout.containsRenderPass(RenderPass.TRANSLUCENT_SHADED));
        assert(!rps.renderLayout.containsRenderPass(RenderPass.TRANSLUCENT_FLAT));
        
        assert(rps.blockRenderMode == BlockRenderMode.TESR);

             
    }

}