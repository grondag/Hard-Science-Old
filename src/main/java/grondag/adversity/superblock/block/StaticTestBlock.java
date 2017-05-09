package grondag.adversity.superblock.block;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.superblock.model.painter.SurfacePainter;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

public class StaticTestBlock extends SuperBlock
{

    
    public StaticTestBlock(BaseMaterial material, String styleName)
    {
        super(material, styleName);
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

        modelState.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(536));
        modelState.setLightingMode(0, LightingMode.SHADED);
        modelState.setRenderLayer(0, BlockRenderLayer.SOLID);
        modelState.setRotationEnabled(0, true);
        modelState.setSurfacePainter(0, SurfacePainter.CUBIC_TILES);
  
        modelState.setSurface(0, ModelShape.CUBE.meshFactory().surfaces.get(0));
        modelState.setTexture(0, Textures.ALL_TEXTURES.get(0));

        return modelState;
    }
}
