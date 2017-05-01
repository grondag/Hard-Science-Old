package grondag.adversity.niceblock.model.shape.painter;


import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.model.shape.ModelShape;
import grondag.adversity.niceblock.model.texture.TextureProvider.Texture.TextureState;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class ColorModelFactory extends ModelFactory
{
    public ColorModelFactory(ModelStateComponent<?,?>... components) 
    {
        super(ModelShape.COLUMN_SQUARE, components);
    }

    protected CubeInputs getCubeInputs(TextureState texState, ModelStateSetValue state)
    {
        CubeInputs result = new CubeInputs();
        ColorMap colorMap = state.getValue(colorComponent);
        result.color = colorMap.getColor(EnumColorMap.BASE);
        result.textureRotation = state.getValue(rotationComponent);
        result.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                texState.buildTextureName(state.getValue(textureComponent)));
        result.lightingMode = texState.lightingMode;
        result.u0 = 0;
        result.v0 = 0;
        result.u1 = 16;
        result.v1 = 16;
        
        return result;
    }
    
	@Override
	public QuadContainer getFaceQuads(TextureState texState, ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
		if(renderLayer != texState.renderLayer) return QuadContainer.EMPTY_CONTAINER;
		CubeInputs cube = getCubeInputs(texState, state);
		QuadContainer.QuadContainerBuilder builder = new QuadContainer.QuadContainerBuilder();
		builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
		for(EnumFacing face : EnumFacing.values())
		{
			builder.setQuads(face, cube.makeFace(face));
		}
        return builder.build();
    }

    @Override
    public List<BakedQuad> getItemQuads(TextureState texState, ModelStateSetValue state)
    {
        CubeInputs cubeInputs = getCubeInputs(texState, state);
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = texState.renderLayer != BlockRenderLayer.SOLID;

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
