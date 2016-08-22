package grondag.adversity.niceblock;

import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelFactory2;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelTextureComponent;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelRotationComponent;
import grondag.adversity.niceblock.modelstate.ModelState;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class ColorModelFactory2 extends ModelFactory2
{
    
    public ColorModelFactory2(ModelInputs modelInputs, ModelStateComponent<?,?>... components) 
    {
        super(modelInputs, components);
    }

    private CubeInputs getCubeInputs(ModelState modelState)
    {
        CubeInputs result = new CubeInputs();
        ColorMap colorMap = modelState.stateValue.getValue(colorComponent);
        result.color = colorMap.getColor(EnumColorMap.BASE);
        result.textureRotation = modelState.stateValue.getValue(rotationComponent);
        result.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, modelState.stateValue.getValue(textureComponent)));
        result.isShaded = modelInputs.isShaded;
        result.u0 = 0;
        result.v0 = 0;
        result.u1 = 16;
        result.v1 = 16;
        
        return result;
    }
    
	@Override
	public List<BakedQuad> getFaceQuads(ModelState modelState, EnumFacing face) 
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
        return getCubeInputs(modelState).makeFace(face);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState)
    {
        CubeInputs cubeInputs = getCubeInputs(modelState);
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = modelInputs.renderLayer != BlockRenderLayer.SOLID;

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
