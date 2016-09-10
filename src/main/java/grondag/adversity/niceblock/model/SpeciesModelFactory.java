package grondag.adversity.niceblock.model;

import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import net.minecraft.client.Minecraft;

public class SpeciesModelFactory extends ColorModelFactory
{

    public SpeciesModelFactory(ModelFactory.ModelInputs modelInputs, ModelStateComponent<?, ?>... components)
    {
        super(modelInputs, components);
    }

    @Override
    protected CubeInputs getCubeInputs(ModelStateSetValue state)
    {
        CubeInputs result = new CubeInputs();
        ColorMap colorMap = state.getValue(colorComponent);
        result.color = colorMap.getColor(EnumColorMap.BASE);
        result.textureRotation = state.getValue(rotationComponent);
        result.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, state.getValue(speciesComponent), state.getValue(textureComponent)));
        result.isShaded = modelInputs.isShaded;
        result.u0 = 0;
        result.v0 = 0;
        result.u1 = 16;
        result.v1 = 16;
        
        return result;
    }
    
    @Override
    public String[] getAllTextureNames()
    {
        if(this.modelInputs.textureName == null) return new String[0];
        
        final String retVal[] = new String[(int) (this.textureComponent.getValueCount() * this.speciesComponent.getValueCount())];

        int counter = 0;
        for (int i = 0; i < this.speciesComponent.getValueCount(); i++)
        {
            for(int j = 0; j < this.textureComponent.getValueCount(); j++)
            {
                retVal[counter++] = buildTextureName(this.modelInputs.textureName, i, j);
            }
        }
        return retVal;
    }
    
    /** used by dispatched as default particle texture */
    @Override
    public String getDefaultParticleTexture() 
    { 
        return buildTextureName(modelInputs.textureName, 0, 0);
    }
    
    private String buildTextureName(String baseName, int species, int offset)
    {
        return "adversity:blocks/" + baseName + "_" + species + "_" + (offset >> 3) + "_" + (offset & 7);
    }
}
