package grondag.adversity.niceblock;

import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelState;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class ColorModelFactory extends ModelFactory
{
    public ColorModelFactory(ColorController controller)
    {
        super(controller);
    }

	@Override
	public List<BakedQuad> getFaceQuads(ModelState modelState, IColorMapProvider colorProvider, EnumFacing face) 
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        CubeInputs cubeInputs = new CubeInputs();
        ColorMap colorMap = colorProvider.getColorMap(modelState.getColorIndex());
        ColorController controller = (ColorController)this.controller;
        
        cubeInputs.color = colorMap.getColor(controller.getRenderLayer() == BlockRenderLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        cubeInputs.textureRotation = controller.getTextureRotationFromShapeIndex(modelState.getShapeIndex(controller.getRenderLayer()));
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getAltTextureFromModelIndex(modelState.getShapeIndex(controller.getRenderLayer()))));
        cubeInputs.isShaded = controller.isShaded;
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        
        return cubeInputs.makeFace(face);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorMapProvider colorProvider)
    {
        CubeInputs cubeInputs = new CubeInputs();
        ColorController controller = (ColorController)this.controller;
        ColorMap colorMap = colorProvider.getColorMap(modelState.getColorIndex());
        
        cubeInputs.color = colorMap.getColor(controller.getRenderLayer() == BlockRenderLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getAltTextureFromModelIndex(modelState.getShapeIndex(controller.getRenderLayer()))));

        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = controller.getRenderLayer() != BlockRenderLayer.SOLID;

        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
        return itemBuilder.build();        
    }


}
