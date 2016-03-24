package grondag.adversity.niceblock;

import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

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
	public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face) 
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        CubeInputs cubeInputs = new CubeInputs();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        ColorController controller = (ColorController)this.controller;
        
        cubeInputs.color = colorMap.getColorMap(controller.getRenderLayer() == BlockRenderLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        cubeInputs.textureRotation = controller.getTextureRotationFromShapeIndex(modelState.getClientShapeIndex(controller.getRenderLayer().ordinal()));
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getTextureOffsetFromShapeIndex(modelState.getClientShapeIndex(controller.getRenderLayer().ordinal()))));
        cubeInputs.isShaded = controller.isShaded;
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        
        return cubeInputs.makeFace(face);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        CubeInputs cubeInputs = new CubeInputs();
        ColorController controller = (ColorController)this.controller;
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        
        cubeInputs.color = colorMap.getColorMap(controller.getRenderLayer() == BlockRenderLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getTextureOffsetFromShapeIndex(modelState.getClientShapeIndex(controller.getRenderLayer().ordinal()))));

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