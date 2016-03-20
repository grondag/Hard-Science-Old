package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.newmodel.color.ColorMap;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class ColoredBlockModelFactory extends BakedModelFactory
{
    public ColoredBlockModelFactory(ColoredBlockController controller)
    {
        super(controller);
    }

    @SuppressWarnings("unchecked")
	@Override
	public IQuadProvider getBlockQuads(ModelState modelState, IColorProvider colorProvider) 
    {
        CubeInputs cubeInputs = new CubeInputs();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        ColoredBlockController controller = (ColoredBlockController)this.controller;
        
        cubeInputs.color = colorMap.getColorMap(controller.renderLayer == BlockRenderLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        cubeInputs.textureRotation = controller.getTextureRotationFromShapeIndex(modelState.getClientShapeIndex(controller.renderLayer.ordinal()));
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getTextureOffsetFromShapeIndex(modelState.getClientShapeIndex(controller.renderLayer.ordinal()))));

        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        
        List<BakedQuad>[] faceQuads = (List<BakedQuad>[]) new List[6];
        faceQuads[6] = new ImmutableList.Builder<BakedQuad>().build();

        faceQuads[EnumFacing.UP.ordinal()] = cubeInputs.makeFace(EnumFacing.UP);
        faceQuads[EnumFacing.DOWN.ordinal()] = cubeInputs.makeFace(EnumFacing.DOWN);
        faceQuads[EnumFacing.EAST.ordinal()] = cubeInputs.makeFace(EnumFacing.EAST);
        faceQuads[EnumFacing.WEST.ordinal()] = cubeInputs.makeFace(EnumFacing.WEST);
        faceQuads[EnumFacing.NORTH.ordinal()] = cubeInputs.makeFace(EnumFacing.NORTH);
        faceQuads[EnumFacing.SOUTH.ordinal()] = cubeInputs.makeFace(EnumFacing.SOUTH);
        
        return new SimpleQuadProvider(faceQuads);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        CubeInputs cubeInputs = new CubeInputs();
        ColoredBlockController controller = (ColoredBlockController)this.controller;
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        
        cubeInputs.color = colorMap.getColorMap(controller.renderLayer == BlockRenderLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getTextureOffsetFromShapeIndex(modelState.getClientShapeIndex(controller.renderLayer.ordinal()))));

        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = controller.renderLayer != BlockRenderLayer.SOLID;

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
