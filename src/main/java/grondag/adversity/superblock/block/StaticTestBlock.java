package grondag.adversity.superblock.block;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

public class StaticTestBlock extends SuperBlock
{
    
    public StaticTestBlock(BlockSubstance material, String styleName)
    {
        super(styleName, material);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.SOLID;
    }

    @Override
    public ModelState getDefaultModelState()
    {
        ModelState modelState = new ModelState();
        modelState.setShape(ModelShape.CUBE);
        modelState.setStatic(false);

        modelState.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(536));
        modelState.setLightingMode(PaintLayer.BASE, LightingMode.SHADED);
        modelState.setRenderLayer(PaintLayer.BASE, BlockRenderLayer.SOLID);
        modelState.setTexture(PaintLayer.BASE, Textures.BLOCK_RAW_FLEXSTONE);

        return modelState;
    }
}
